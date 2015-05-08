/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagents;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
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
public class SomeAgentSaboteur extends massim.javaagents.Agent {

    public SomeAgentSaboteur(String name, String team) {
        super(name, team);
    }

    @Override
    public Action step() {
        handlePercepts();
        // 1. recharging
        return planRecharge();
    }

    @Override
    public void handlePercept(Percept p) {
        //handle percepts if neccesary
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

        // 2. fight if possible
        return planFight();
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
            // 4. random walking
            return planRandomWalk();
        }

        LogicBelief moneyBelief = myBeliefs.get(0);
        int money = new Integer(moneyBelief.getParameters().get(0)).intValue();

        if (money < 10) {
            println("we do not have enough money.");
            // 4. random walking
            return planRandomWalk();
        }
        println("we do have enough money.");

        int rollDice = (int) Math.floor(Math.random() * 6);

        if (rollDice > 1) {
            println("I am going to buy a sabotageDevice");

            return MarsUtil.buyAction("sabotageDevice");
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
        // 4. random walking
        return planRandomWalk();
    }

    private Action planFight() {

        // get position
        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("position");
        if (myBeliefs.isEmpty()) {
            println("strangely I do not know my position");
            return MarsUtil.rechargeAction();
        }
        String position = myBeliefs.getFirst().getParameters().firstElement();

        // if there is an enemy on the current position then attack or defend
        List<String> enemies = new ArrayList<>();
        myBeliefs = getAllBeliefs("visibleEntity");
        for (LogicBelief b : myBeliefs) {
            String name = b.getParameters().get(0);
            String pos = b.getParameters().get(1);
            String team = b.getParameters().get(2);
            if (team.equals(getTeam())) {
                continue;
            }
            if (pos.equals(position) == false) {
                continue;
            }
            enemies.add(name);
        }
        if (!enemies.isEmpty()) {
            println("there are " + enemies.size() + " enemies at my current position");

            LinkedList<LogicBelief> healthBeliefs = this.getAllBeliefs("health");

            if (healthBeliefs.size() > 0) {
                LogicBelief healthBelief = healthBeliefs.get(0);
                int health = new Integer(healthBelief.getParameters().get(0)).intValue();

                if (health == 1) {
                    println("I am going to  run like a coward");
                    // if there is an enemy on a neighboring vertex do not go there
                    myBeliefs = getAllBeliefs("neighbor");
                    List<String> neighbors = new ArrayList<>();
                    for (LogicBelief b : myBeliefs) {
                        neighbors.add(b.getParameters().firstElement());
                    }

                    List<String> vertices = new ArrayList<>();
                    myBeliefs = getAllBeliefs("visibleEntity");
                    for (LogicBelief b : myBeliefs) {
                        //String name = b.getParameters().get(0);
                        String pos = b.getParameters().get(1);
                        String team = b.getParameters().get(2);
                        if (team.equals(getTeam())) {
                            continue;
                        }
                        if (neighbors.contains(pos) == true) {
                            continue;
                        }
                        vertices.add(pos);
                    }
                    if (!vertices.isEmpty()) {
                        println("there are " + vertices.size() + " adjacent vertices with no enemies");
                        Collections.shuffle(vertices);
                        String vertex = vertices.get(0);
                        println("I will goto " + vertex);
                        return MarsUtil.gotoAction(vertex);
                    } else {
                        println("I am done for. I'll parry like forever");
                        return MarsUtil.parryAction();
                    }
                }
            }

            Collections.shuffle(enemies);
            String enemy = enemies.get(0);
            println("I will attack " + enemy);
            return MarsUtil.attackAction(enemy);
        }

        // if there is an enemy on a neighboring vertex to there
        myBeliefs = getAllBeliefs("neighbor");
        List<String> neighbors = new ArrayList<>();
        for (LogicBelief b : myBeliefs) {
            neighbors.add(b.getParameters().firstElement());
        }

        List<String> vertices = new ArrayList<>();
        myBeliefs = getAllBeliefs("visibleEntity");
        for (LogicBelief b : myBeliefs) {
            //String name = b.getParameters().get(0);
            String pos = b.getParameters().get(1);
            String team = b.getParameters().get(2);
            if (team.equals(getTeam())) {
                continue;
            }
            if (neighbors.contains(pos) == false) {
                continue;
            }
            vertices.add(pos);
        }
        if (!vertices.isEmpty()) {
            println("there are " + vertices.size() + " adjacent vertices with enemies");
            Collections.shuffle(vertices);
            String vertex = vertices.get(0);
            println("I will goto " + vertex);
            return MarsUtil.gotoAction(vertex);
        }

        // 3 buying something
        return planBuy();
    }

    private Action planRandomWalk() {

        LinkedList<LogicBelief> myBeliefs = getAllBeliefs("neighbor");
        List<String> neighbors = new ArrayList<>();
        for (LogicBelief b : myBeliefs) {
            neighbors.add(b.getParameters().firstElement());
        }

        if (neighbors.isEmpty()) {
            println("strangely I do not know any neighbors");
            return MarsUtil.rechargeAction();
        }

        // goto neighbors
        Collections.shuffle(neighbors);
        String neighbor = neighbors.get(0);
        println("I will go to " + neighbor);
        return MarsUtil.gotoAction(neighbor);
    }

}
