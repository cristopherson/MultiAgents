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
public class SomeAgentInspector extends massim.javaagents.Agent {

    public SomeAgentInspector(String name, String team) {
        super(name, team);
    }

    @Override
    public Action step() {
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
            println("I do not care. I have more important things to do");
        }

    }

    private void handlePercepts() {
        String position = null;
        Collection<Percept> percepts = getAllPercepts();
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
                case "inspectedEntity":
                    println("I have perceived an inspected entity " + p);
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
                case "money":
                    Integer money = new Integer(p.getParameters().get(0).toString());
                    removeBeliefs("money");
                    addBelief(new LogicBelief("money", money.toString()));
                    break;
            }
        }

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

        // 2 buying battery
        return planBuy();
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
            // 3. inspecting if necessary
            return planInspect();
        }

        LogicBelief moneyBelief = myBeliefs.get(0);
        int money = new Integer(moneyBelief.getParameters().get(0)).intValue();

        if (money < 10) {
            println("we do not have enough money.");
            // 3. inspecting if necessary
            return planInspect();
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
        // 3. inspecting if necessary
        return planInspect();
    }

    private Action planInspect() {

        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("position");
        List<String> vertices = new ArrayList<>();
        String position = myBeliefs.getFirst().getParameters().firstElement();

        vertices.add(position);
        myBeliefs = getAllBeliefs("neighbor");
        for (LogicBelief b : myBeliefs) {
            vertices.add(b.getParameters().firstElement());
        }

        int adjacentNum = 0;

        String myTeam = getTeam();

        LinkedList<LogicBelief> visible = getAllBeliefs("visibleEntity");
        for (LogicBelief v : visible) {
            String pos = v.getParameters().get(1);
            String team = v.getParameters().get(2);

            if (myTeam.equals(team)) {
                continue;
            }

            if (vertices.contains(pos) == false) {
                continue;
            }
            adjacentNum++;

        }

        if (adjacentNum == 0) {
            println("there are no opponents to inspect");
            // 4. (almost) random walking
            return planRandomWalk();
        }

        println("there are " + adjacentNum + " visible opponents that I could inspect");

        int rollDice = (int) Math.floor(Math.random() * 6);

        if (rollDice > 0) {
            println("I will inspect");
            return MarsUtil.inspectAction();
        }

        println("I won't inspect");
        // 4. (almost) random walking
        return planRandomWalk();
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
