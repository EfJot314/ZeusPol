package agh.edu.zeuspol.checker;

import agh.edu.zeuspol.datastructures.ActionType;
import agh.edu.zeuspol.datastructures.Rule;
import agh.edu.zeuspol.datastructures.Sla;

import java.util.List;

public class SlaViolationChecker {

    public boolean check(Sla sla, Rule rule){
        for (Rule slaRule: sla.getRules()){
            if (slaRule.subject == rule.subject && slaRule.attribute == rule.attribute){
                if (slaRule.unit != rule.unit){
                    throw new IllegalArgumentException();
                }
                if (slaRule.action == ActionType.EQ || rule.action == ActionType.EQ){
                    if (slaRule.action == ActionType.EQ && rule.action == ActionType.EQ){
                        ActionType action = slaRule.action;
                        if (!action.apply(slaRule.value, rule.value.get(0))){
                            return true;
                        }
                    }
                    else {
                        return true;
                    }
                }
                else if (slaRule.action == ActionType.BT && rule.action == ActionType.BT){
                    if (!this.isBetween(slaRule.value, rule.value)){
                        return true;
                    }
                }
                else if (slaRule.action == ActionType.BT){
                    ActionType action = rule.action;
                    Number n1 = slaRule.value.get(0);
                    Number n2 = slaRule.value.get(1);
                    if (!(action.apply(rule.value, n1) || action.apply(rule.value, n2))){
                        return true;
                    }
                }
                else if (rule.action == ActionType.BT){
                    ActionType action = slaRule.action;
                    Number n1 = rule.value.get(0);
                    Number n2 = rule.value.get(1);
                    if (!(action.apply(slaRule.value, n1) && action.apply(slaRule.value, n2))){
                        return true;
                    }
                }
                else if (slaRule.action != rule.action){
                    return true;
                }
                else {
                    ActionType action = slaRule.action;
                    if (action.apply(slaRule.value, rule.value.get(0))){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isBetween(List<Number> span1, List<Number> span2){
        if (span1.size() != 2 || span2.size() != 2){
            throw new RuntimeException("Wrong argument number in lists!");
        }
        double n1 = span1.get(0).doubleValue();
        double n2 = span1.get(1).doubleValue();

        double m1 = span2.get(0).doubleValue();
        double m2 = span2.get(1).doubleValue();
        return n1 <= m1 && n1 <= m2 && n2 >= m1 && n2 >= m2;
    }

}
