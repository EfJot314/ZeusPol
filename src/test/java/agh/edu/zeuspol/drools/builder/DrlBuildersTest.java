package agh.edu.zeuspol.drools.builder;

import agh.edu.zeuspol.datastructures.storage.Sla;
import agh.edu.zeuspol.datastructures.types.PolicyRule;
import agh.edu.zeuspol.datastructures.types.SlaRule;
import agh.edu.zeuspol.datastructures.types.attributes.*;
import agh.edu.zeuspol.drools.DrlStringFile;
import agh.edu.zeuspol.drools.converter.PolicyRuleToDrl2Converter;
import agh.edu.zeuspol.drools.converter.PolicyRuleToDrlConverter;
import agh.edu.zeuspol.drools.converter.SlaRuleToDrlConverter;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DrlBuildersTest {

  @Test
  public void testPolicyRule() {
    Params params = new Params();
    params.put("test", "test");
    Action action = new Action("TEST", "TEST2", params);
    PolicyRule policyRule = new PolicyRule(2, "TEST_ACTION", action);

    policyRule.addCondition(new Condition("TestMetric", RelationType.GT, 4));

    PolicyRuleToDrlConverter converter = new PolicyRuleToDrlConverter();
    DrlStringFile file = converter.convert(policyRule);

    System.out.println(file.getPath());
    System.out.println("\n\n");
    System.out.println(file.getFileContent());
  }

  @Test
  public void testPolicyRule2() {
    Params params = new Params();
    params.put("test", "test");
    Action action = new Action("TEST", "TEST2", params);
    PolicyRule policyRule = new PolicyRule(2, "TEST_ACTION", action);

    policyRule.addCondition(new Condition("TestMetric1", RelationType.GT, 4));
    policyRule.addCondition(new Condition("TestMetric2", RelationType.GT, 5));
    policyRule.addCondition(new Condition("TestMetric3", RelationType.GT, 6));
    policyRule.addCondition(new Condition("TestMetric4", RelationType.GT, 7));

    PolicyRuleToDrl2Converter converter = new PolicyRuleToDrl2Converter();
    DrlStringFile file = converter.convert(policyRule);

    System.out.println(file.getPath());
    System.out.println("\n\n");
    System.out.println(file.getFileContent());
  }

  @Test
  public void testSlaRule() {
    SlaRule slaRule = new SlaRule(2, "CPU");
    slaRule.addCondition(new Condition("TestMetric", RelationType.GT, 4));

    Sla sla = new Sla(4, "2", "3", SlaType.SAAS, List.of());

    SlaRuleToDrlConverter converter = new SlaRuleToDrlConverter();
    DrlStringFile file = converter.convert(sla, slaRule);

    System.out.println(file.getPath());
    System.out.println("\n\n");
    System.out.println(file.getFileContent());
  }
}
