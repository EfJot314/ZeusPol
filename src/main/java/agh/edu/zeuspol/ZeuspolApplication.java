package agh.edu.zeuspol;

import agh.edu.zeuspol.datastructures.storage.Policies;
import agh.edu.zeuspol.datastructures.storage.Sla;
import agh.edu.zeuspol.datastructures.storage.Slas;
import agh.edu.zeuspol.datastructures.types.PolicyRule;
import agh.edu.zeuspol.datastructures.types.SlaRule;
import agh.edu.zeuspol.drools.*;
import agh.edu.zeuspol.drools.converter.PolicyRuleToDrl2Converter;
import agh.edu.zeuspol.drools.converter.SlaRuleToDrlConverter;
import agh.edu.zeuspol.services.HephaestusQueryService;
import agh.edu.zeuspol.services.HermesService;
import agh.edu.zeuspol.services.ThemisService;
import io.github.hephaestusmetrics.model.metrics.Metric;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ZeuspolApplication {
  private static ConfigurableApplicationContext context;
  private static boolean isRunning = false;
  private static final Lock lock = new ReentrantLock();
  private static final Condition condition = lock.newCondition();
  private static DrlRuleExecutor drlRuleExecutor;

  public static void main(String[] args) throws IOException {

    loadZeuspolContext(args);

    // run loop of main loops
    mainLoop();
  }

  public static boolean isRunning() {
    return isRunning;
  }

  public static void startApp() {
    lock.lock();
    isRunning = true;
    condition.signalAll();
    lock.unlock();
  }

  public static void stopApp() {
    lock.lock();
    isRunning = false;
    condition.signalAll();
    lock.unlock();
  }

  private static void waitForCondition() throws InterruptedException {
    lock.lock();
    try {
      while (!isRunning) {
        condition.await();
      }
      // Proceed when conditionMet becomes true
    } finally {
      lock.unlock();
    }
  }

  private static void fetchHermesData() {
    HermesService hermesService = context.getBean(HermesService.class);

    List<PolicyRule> policyRules = hermesService.getPolicyRules();
    Policies.newInstance().addRules(policyRules);

    List<Sla> slaList = hermesService.getAllSlas();
    Slas.newInstance().addSlaList(slaList);
  }

  public static void buildExecutor() {
    PolicyRuleToDrl2Converter policyConverter = new PolicyRuleToDrl2Converter();
    SlaRuleToDrlConverter slaRuleConverter = new SlaRuleToDrlConverter();
    DynamicDrlBuilder builder = new DynamicDrlBuilder();

    for (PolicyRule pr : Policies.getInstance().getRules()) {
      builder.addFile(policyConverter.convert(pr));
    }

    for (Sla sla : Slas.getInstance().getSlaList()) {
      for (SlaRule slaRule : sla.getRules()) {
        builder.addFile(slaRuleConverter.convert(sla, slaRule));
      }
    }

    System.out.println("--------------Policies:--------------");
    for (PolicyRule pr : Policies.getInstance().getRules()) {
      System.out.println(pr);
    }

    System.out.println("--------------Sla rules:--------------");
    for (Sla sla : Slas.getInstance().getSlaList()) {
      for (SlaRule slaRule : sla.getRules()) {
        System.out.println(slaRule);
      }
    }

    drlRuleExecutor = builder.build();
  }

  private static void mainLoop() {
    HephaestusQueryService metricsService = context.getBean(HephaestusQueryService.class);
    ThemisService themisService = context.getBean(ThemisService.class);

    fetchHermesData();

    buildExecutor();

    // infinite loop of mainLoops
    while (true) {
      // if app should be running, then run main loop
      try {
        waitForCondition();
      } catch (InterruptedException e) {
        continue;
      }

      List<Metric> metrics = metricsService.getMetrics();
      List<Object> objs = new ArrayList<>();
      System.out.println("=============================================");
      System.out.println("METRICS:");
      for (Metric metric : metrics) {
        System.out.println(
            "name: "
                + metric.getQueryTag()
                + ", value: "
                + (double) Math.round(metric.value * 100) / 100);
        objs.add(metric);
      }

      //      Adding stats of PolicyRules and SlaRules to the list of facts
      objs.addAll(Policies.getInstance().getRulesStatsList());
      objs.addAll(Slas.getInstance().getRulesStats());

      drlRuleExecutor.fireRules(objs);

      System.out.println("=============================================");

      // wait some time
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void loadZeuspolContext(String[] args) {
    if (context != null) {
      return;
    }
    context = SpringApplication.run(ZeuspolApplication.class, args);
  }

  public static ConfigurableApplicationContext getContext() {
    if (context == null) {
      throw new RuntimeException("Context of ZeuspolApplication not loaded");
    }
    return context;
  }
}
