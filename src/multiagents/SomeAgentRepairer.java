/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagents;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Percept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import massim.javaagents.agents.MarsUtil;

/**
 *
 * @author cristopherson
 */
public class SomeAgentRepairer extends massim.javaagents.Agent {

    public SomeAgentRepairer(String name, String team) {
        super(name, team);
    }

    @Override
    public Action step() {
        handlePercepts();
        return planRecharge();
    }

    private void handlePercepts() {
        String position = null;

        // check percepts
        Collection<Percept> percepts = getAllPercepts();
        //if ( gatherSpecimens ) processSpecimens(percepts);
        removeBeliefs("visibleEntity");
        removeBeliefs("visibleEdge");
        for (Percept p : percepts) {
            switch (p.getName()) {
                case "step":
                    println(p);
                    break;
                case "visibleEntity":
                    LogicBelief b = MarsUtil.perceptToBelief(p);
                    if (containsBelief(b) == false) {
                        //println("I perceive an edge I have not known before");
                        addBelief(b);
                        //broadcastBelief(b);
                    } else {
                        //println("I already knew " + b);
                    }
                    break;
                case "health":
                    Integer health = new Integer(p.getParameters().get(0).toString());
                    println("my health is " + health);
                    if (health.intValue() == 0) {
                        println("my health is zero. asking for help");
                        broadcastBelief(new LogicBelief("iAmDisabled"));
                    }
                    break;
                case "position":
                    position = p.getParameters().get(0).toString();
                    removeBeliefs("position");
                    addBelief(new LogicBelief("position", position));
                    break;
                case "energy":
                    Integer energy = new Integer(p.getParameters().get(0).toString());
                    removeBeliefs("energy");
                    addBelief(new LogicBelief("energy", energy.toString()));
                    break;
                case "maxEnergy":
                    Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
                    removeBeliefs("maxEnergy");
                    addBelief(new LogicBelief("maxEnergy", maxEnergy.toString()));
                    break;
                case "achievement":
                    println("reached achievement " + p);
                    break;
            }
        }

        // again for checking neighbors
        this.removeBeliefs("neighbor");
        for (Percept p : percepts) {
            if (p.getName().equals("visibleEdge")) {
                String vertex1 = p.getParameters().get(0).toString();
                String vertex2 = p.getParameters().get(1).toString();
                if (vertex1.equals(position)) {
                    addBelief(new LogicBelief("neighbor", vertex2));
                }
                if (vertex2.equals(position)) {
                    addBelief(new LogicBelief("neighbor", vertex1));
                }
            }
        }
    }

    private Action planRecharge() {

        LinkedList<LogicBelief> myBeliefs;

        myBeliefs = getAllBeliefs("energy");
        if (myBeliefs.size() == 0) {
            println("strangely I do not know my energy");
            return MarsUtil.rechargeAction();
        }
        int energy = new Integer(myBeliefs.getFirst().getParameters().firstElement()).intValue();

        myBeliefs = getAllBeliefs("maxEnergy");
        if (myBeliefs.size() == 0) {
            println("strangely I do not know my maxEnergy");
            return MarsUtil.rechargeAction();
        }
        int maxEnergy = new Integer(myBeliefs.getFirst().getParameters().firstElement()).intValue();

        // if has the goal of being recharged...
        if (goals.contains(new LogicGoal("beAtFullCharge"))) {
            if (maxEnergy == energy) {
                println("I can stop recharging. I am at full charge");
                removeGoals("beAtFullCharge");
            } else {
                println("recharging AtFullCharge...");
                return MarsUtil.rechargeAction();
            }
        } else if (goals.contains(new LogicGoal("beAtAlmostFullCharge"))) {
            if (((maxEnergy / 3) * 2) <= energy) {
                println("I can stop recharging. I have charged what I needed");
                removeGoals("beAtAlmostFullCharge");
            } else {
                println("recharging AtAlmostFullCharge...");
                return MarsUtil.rechargeAction();
            }
        } else {
            if (energy < maxEnergy / 3) {
                println("I need to recharge");
                int flipACoin = (int) Math.floor(Math.random() * 2);

                if (flipACoin == 1) {
                    goals.add(new LogicGoal("beAtFullCharge"));
                } else {
                    goals.add(new LogicGoal("beAtAlmostFullCharge"));
                }
                return MarsUtil.rechargeAction();
            }
        }

        return planRepair();
    }

    private Action planRepair() {
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
                    return MarsUtil.rechargeAction();
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
