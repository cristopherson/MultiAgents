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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import massim.javaagents.agents.MarsUtil;

/**
 *
 * @author cristopherson
 */
public class SomeAgentExplorer extends massim.javaagents.Agent {

    HashMap<String, String> probedVerticesValues;

    public SomeAgentExplorer(String name, String team) {
        super(name, team);
        probedVerticesValues = new HashMap<>();
    }

    @Override
    public Action step() {
        //deliberate and return an action
        handleMessages();
        handlePercepts();

        // 1. recharging
        return planRecharge();
    }

    @Override
    public void handlePercept(Percept p) {
        //handle percepts if neccesary
    }

    private void handleMessages() {

        // handle messages... believe everything the others say
        Collection<Message> messages = getMessages();
        for (Message msg : messages) {

            println(msg.sender + " told me " + msg.value);
            String predicate = ((LogicBelief) msg.value).getPredicate();
            if (containsBelief((LogicBelief) msg.value)) {
                println("I already knew that");
            } else {
                println("that was new to me");
                if (predicate.equals("probedVertex") || predicate.equals("surveyedEdge")) {
                    addBelief((LogicBelief) msg.value);
                    println("I will keep in mind that " + ((LogicBelief) msg.value));
                    continue;
                }
                println("but I am not interested in that gibberish " + ((LogicBelief) msg.value));
            }
        }

    }

    private void handlePercepts() {

        String position = null;
        Collection<Percept> percepts = getAllPercepts();
        //if ( gatherSpecimens ) processSpecimens(percepts);
        removeBeliefs("visibleEntity");
        removeBeliefs("visibleEdge");
        for (Percept p : percepts) {
            switch (p.getName()) {
                case "step":
                    println(p);
                    break;
                case "visibleEntity": {
                    LogicBelief b = MarsUtil.perceptToBelief(p);
                    if (containsBelief(b) == false) {
                        addBelief(b);
                    }
                    break;
                }
                case "visibleEdge": {
                    LogicBelief b = MarsUtil.perceptToBelief(p);
                    if (containsBelief(b) == false) {
                        addBelief(b);
                    }
                    break;
                }
                case "probedVertex": {
                    LogicBelief b = MarsUtil.perceptToBelief(p);
                    if (containsBelief(b) == false) {
                        println("I perceive the value of a vertex that I have not known before");
                        addBelief(b);
                        broadcastBelief(b);
                    }
                    break;
                }
                case "surveyedEdge": {
                    LogicBelief b = MarsUtil.perceptToBelief(p);
                    if (containsBelief(b) == false) {
                        println("I perceive the weight of an edge that I have not known before");
                        addBelief(b);
                        broadcastBelief(b);
                    }
                    break;
                }
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
                    println("my energy is " + energy);
                    addBelief(new LogicBelief("energy", energy.toString()));
                    break;
                case "maxEnergy":
                    Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
                    removeBeliefs("maxEnergy");
                    addBelief(new LogicBelief("maxEnergy", maxEnergy.toString()));
                    break;
                case "money":
                    Integer money = new Integer(p.getParameters().get(0).toString());
                    removeBeliefs("money");
                    addBelief(new LogicBelief("money", money.toString()));
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

        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("energy");

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

        // 2. buying battery with a certain probability
        return planBuy();
    }

    private Action planProbe() {
        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("position");

        if (myBeliefs.size() == 0) {
            println("strangely I do not know my position");
            return MarsUtil.rechargeAction();
        }
        String position = myBeliefs.getFirst().getParameters().firstElement();

        // probe current position if not known
        boolean probed = false;
        LinkedList<LogicBelief> vertices = getAllBeliefs("probedVertex");
        for (LogicBelief v : vertices) {
            if (v.getParameters().get(0).equals(position)) {
                probedVerticesValues.put(v.getParameters().get(0), v.getParameters().get(1));
                probed = true;
                break;
            }
        }
        if (probed == false) {
            println("I do not know the value of my position. I will probe.");
            return MarsUtil.probeAction();
        } else {
            println("I know the value of my position");
        }

        myBeliefs = getAllBeliefs("neighbor");

        List<String> unprobed = new ArrayList<>();
        for (LogicBelief n : myBeliefs) {
            probed = false;
            String name = n.getParameters().firstElement();
            for (LogicBelief v : vertices) {
                if (v.getParameters().get(0).equals(name)) {
                    probed = true;
                    break;
                }
            }
            if (probed == false) {
                unprobed.add(name);
            }
        }
        if (!unprobed.isEmpty()) {
            println("some of my neighbors are unprobed.");
            int flipACoin = (int) Math.floor(Math.random() * 2);
            String neighbor = unprobed.get(0);

            if (flipACoin == 1) {
                println("I will shuffle");
                Collections.shuffle(unprobed);
                neighbor = unprobed.get(0);
            } else {
                println("I will check the biggest value if any");
                int maxValue = -1;
                for (String v : unprobed) {
                    String currentValue = probedVerticesValues.get(v);
                    if (currentValue != null) {
                        if (Integer.parseInt(currentValue) > maxValue) {
                            maxValue = Integer.parseInt(currentValue);
                            println("Biggest is " + v + " with " + maxValue);
                            neighbor = v;
                        }
                    }
                }
            }

            println("I will go to " + neighbor);
            return MarsUtil.gotoAction(neighbor);
        } else {
            println("all of my neighbors are probed");
        }

        // 4. surveying if necessary
        return planSurvey();
    }

    private Action planSurvey() {

        println("I know " + getAllBeliefs("visibleEdge").size() + " visible edges");
        println("I know " + getAllBeliefs("surveyedEdge").size() + " surveyed edges");

        // get all neighbors
        LinkedList<LogicBelief> visible = getAllBeliefs("visibleEdge");
        LinkedList<LogicBelief> surveyed = getAllBeliefs("surveyedEdge");

        String position = getAllBeliefs("position").get(0).getParameters().firstElement();

        int adjacentNum = 0;

        for (LogicBelief v : visible) {

            String vVertex0 = v.getParameters().elementAt(0);
            String vVertex1 = v.getParameters().elementAt(1);

            boolean adjacent = false;
            if (vVertex0.equals(position) || vVertex1.equals(position)) {
                adjacent = true;
            }

            if (adjacent == false) {
                continue;
            }
            adjacentNum++;

            boolean isSurveyed = false;
            for (LogicBelief s : surveyed) {
                String sVertex0 = s.getParameters().elementAt(0);
                String sVertex1 = s.getParameters().elementAt(1);
                if (sVertex0.equals(vVertex0) && sVertex1.equals(vVertex1)) {
                    isSurveyed = true;
                    break;
                }
                if (sVertex0.equals(vVertex1) && sVertex1.equals(vVertex0)) {
                    isSurveyed = true;
                    break;
                }
            }
            if (isSurveyed == false) {
                println("I will survey");
                return MarsUtil.surveyAction();
            }

        }

        println("Found " + adjacentNum + " adjacent edges, all are surveyed");

        // 5. (almost) random walking
        return planRandomWalk();
    }

    /**
     * Buy a battery with a given probability
     *
     * @return
     */
    private Action planBuy() {

        LinkedList<LogicBelief> myBeliefs = this.getAllBeliefs("money");
        if (myBeliefs.size() == 0) {
            println("strangely I do not know our money.");
            // 3. probing if necessary
            return planProbe();
        }

        LogicBelief moneyBelief = myBeliefs.get(0);
        int money = new Integer(moneyBelief.getParameters().get(0)).intValue();

        if (money < 10) {
            println("we do not have enough money.");
            // 3. probing if necessary
            return planProbe();
        }
        println("we do have enough money.");

        int rollDice = (int) Math.floor(Math.random() * 6);

        if (rollDice > 1) {
            println("I am going to buy a battery");

            return MarsUtil.buyAction("battery");
        } else {
            LinkedList<LogicBelief> healthBeliefs = this.getAllBeliefs("health");

            if (healthBeliefs.size() > 0) {
                LogicBelief healthBelief = healthBeliefs.get(0);
                int health = new Integer(healthBelief.getParameters().get(0)).intValue();

                if (health == 1) {
                    println("I am going to buy a shield");
                    return MarsUtil.buyAction("shield");
                }
            }
        }
        println("I'll save it for later");
        // 3. probing if necessary
        return planProbe();
    }

    private Action planRandomWalk() {

        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("neighbor");
        List<String> neighbors = new ArrayList<>();
        for (LogicBelief b : myBeliefs) {
            neighbors.add(b.getParameters().firstElement());
        }

        if (!neighbors.isEmpty()) {
            println("strangely I do not know any neighbors");
            return MarsUtil.rechargeAction();
        }

        Collections.shuffle(neighbors);
        String neighbor = neighbors.get(0);
        println("I will go to " + neighbor);
        return MarsUtil.gotoAction(neighbor);
    }
}
