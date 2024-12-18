package agh.edu.zeuspol.drools.converter;

import agh.edu.zeuspol.datastructures.types.PolicyRule;
import agh.edu.zeuspol.datastructures.types.attributes.*;
import agh.edu.zeuspol.drools.DrlStringFile;
import agh.edu.zeuspol.drools.DynamicDrlBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RuleToDrlConverterTest {

  @Test
  public void doesConvertedRuleCompileTest() {
    Params b = new Params();

    b.put("actionName", "ChangeResourcesOfContainerWithinDeploymentAction");
    b.put("collectionName", "kubernetes");
    b.put("namespace", "test-app");
    b.put("deploymentName", "test-app");
    b.put("containerName", "test-app");
    b.put("limitsCpu", "2");
    b.put("limitsMemory", "800Mi");
    b.put("requestsCpu", "2");
    b.put("requestsMemory", "800Mi");

    PolicyRule pRule =
        new PolicyRule(
            RuleAttribute.RESOURCE,
            RuleSubject.CPU,
            List.of(10),
            UnitType.PERCENT,
            RelationType.GT,
            Action.KubernetesChangeResourcesOfContainerWithinDeploymentAction,
            b);

    RuleToDrlConverter converter = new RuleToDrlConverter(new CurlThemisActionBuilder());
    DrlStringFile drlStringFile = converter.convert(pRule);
    DynamicDrlBuilder builder = new DynamicDrlBuilder();
    builder.addFile(drlStringFile);
    builder.build();
  }
}
