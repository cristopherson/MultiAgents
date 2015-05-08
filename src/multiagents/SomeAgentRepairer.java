/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagents;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Percept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import massim.javaagents.agents.MarsUtil;

/**
 *
 * @author cristopherson
 */
public class SomeAgentRepairer extends massim.javaagents.Agent {
    private int rechargeSteps = 0;

    public SomeAgentRepairer(String name, String team) {
        super(name, team);
    }

    @Override
    public Action step() {
        if (rechargeSteps > 0) {
            rechargeSteps--;
            println("recharging...");
            return MarsUtil.skipAction();
        }

        Collection<Message> messages = getMessages();
        List<String> needyAgents = new ArrayList<>();
        for (Message msg : messages) {
            if (((LogicBelief) msg.value).getPredicate().equals("iAmDisabled")) {
                needyAgents.add(msg.sender);
            }
        }

        Collection<Percept> percepts = getAllPercepts();
        String position = null;

        if (!needyAgents.isEmpty()) {
            println("some poor souls need my help " + needyAgents);

            for (Percept p : percepts) {
                if (p.getName().equals("lastActionResult") && p.getParameters().get(0).toProlog().equals("failed")) {
                    println("my previous action has failed. recharging...");
                    rechargeSteps = 10;
                    return MarsUtil.skipAction();
                }
                if (p.getName().equals("position")) {
                    position = p.getParameters().get(0).toString();
                }
            }

            for (Percept p : percepts) {
                if (p.getName().equals("visibleEntity")) {
                    String ePos = p.getParameters().get(1).toString();
                    String eName = p.getParameters().get(0).toString();
                    if (ePos.equals(position) && needyAgents.contains(eName)) {
                        println("I am going to repair " + eName);
                        return MarsUtil.repairAction(eName);
                    }
                }
            }

        } else {
            println("nothing for me to do");
        }

        List<String> neighbors = new ArrayList<>();
        for (Percept p : percepts) {
            if (p.getName().equals("visibleEdge")) {
                String vertex1 = p.getParameters().get(0).toString();
                String vertex2 = p.getParameters().get(1).toString();
                if (vertex1.equals(position)) {
                    neighbors.add(vertex2);
                }
                if (vertex2.equals(position)) {
                    neighbors.add(vertex1);
                }
            }
        }

        // goto neighbors
        if (neighbors.isEmpty()) {
            println("Strangely I do not know my neighbors");
            return MarsUtil.skipAction();
        }

        if (!needyAgents.isEmpty()) {
            for (Percept p : percepts) {
                if (p.getName().equals("visibleEntity")) {
                    String ePos = p.getParameters().get(1).toString();
                    String eName = p.getParameters().get(0).toString();
                    if (neighbors.contains(ePos) && needyAgents.contains(eName)) {
                        println("I am going to repair " + eName + ". move to " + ePos + " first.");
                        return MarsUtil.gotoAction(ePos);
                    }
                }
            }
        }

        Collections.shuffle(neighbors);
        String neighbor = neighbors.get(0);
        println("I will go to " + neighbor);
        return MarsUtil.gotoAction(neighbor);
    }

    @Override
    public void handlePercept(Percept p) {
        //handle percepts if neccesary
    }

}
