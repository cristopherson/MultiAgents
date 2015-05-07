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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;
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
        Action act = null;

        handlePercepts();

        // 1. recharging
        act = planRecharge();
        if (act != null) {
            return act;
        }

        // 2. fight if possible
        act = planFight();
        if (act != null) {
            return act;
        }

        // 3 buying something
        act = planBuy();
        if (act != null) {
            return act;
        }

        // 4. random walking
        act = planRandomWalk();
        if (act != null) {
            return act;
        }

        return MarsUtil.rechargeAction();

    }

    @Override
    public void handlePercept(Percept p) {
        //handle percepts if neccesary
    }

    private void handlePercepts() {

        String position = null;
        Vector<String> neighbors = new Vector<String>();

        // check percepts
        Collection<Percept> percepts = getAllPercepts();
        //if ( gatherSpecimens ) processSpecimens(percepts);
        removeBeliefs("visibleEntity");
        removeBeliefs("visibleEdge");
        for (Percept p : percepts) {
            if (p.getName().equals("step")) {
                println(p);
            } else if (p.getName().equals("visibleEntity")) {
                LogicBelief b = MarsUtil.perceptToBelief(p);
                if (containsBelief(b) == false) {
                    //println("I perceive an edge I have not known before");
                    addBelief(b);
                    //broadcastBelief(b);
                } else {
                    //println("I already knew " + b);
                }
            } else if (p.getName().equals("health")) {
                Integer health = new Integer(p.getParameters().get(0).toString());
                println("my health is " + health);
                if (health.intValue() == 0) {
                    println("my health is zero. asking for help");
                    broadcastBelief(new LogicBelief("iAmDisabled"));
                }
            } else if (p.getName().equals("position")) {
                position = p.getParameters().get(0).toString();
                removeBeliefs("position");
                addBelief(new LogicBelief("position", position));
            } else if (p.getName().equals("energy")) {
                Integer energy = new Integer(p.getParameters().get(0).toString());
                removeBeliefs("energy");
                addBelief(new LogicBelief("energy", energy.toString()));
            } else if (p.getName().equals("maxEnergy")) {
                Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
                removeBeliefs("maxEnergy");
                addBelief(new LogicBelief("maxEnergy", maxEnergy.toString()));
            } else if (p.getName().equals("achievement")) {
                println("reached achievement " + p);
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

        LinkedList<LogicBelief> beliefs = null;

        beliefs = getAllBeliefs("energy");
        if (beliefs.size() == 0) {
            println("strangely I do not know my energy");
            return MarsUtil.rechargeAction();
        }
        int energy = new Integer(beliefs.getFirst().getParameters().firstElement()).intValue();

        beliefs = getAllBeliefs("maxEnergy");
        if (beliefs.size() == 0) {
            println("strangely I do not know my maxEnergy");
            return MarsUtil.rechargeAction();
        }
        int maxEnergy = new Integer(beliefs.getFirst().getParameters().firstElement()).intValue();

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

        return null;

    }

    /**
     * Buy a battery with a given probability
     *
     * @return
     */
    private Action planBuy() {

        LinkedList<LogicBelief> beliefs = this.getAllBeliefs("money");
        if (beliefs.size() == 0) {
            println("strangely I do not know our money.");
            return null;
        }

        LogicBelief moneyBelief = beliefs.get(0);
        int money = new Integer(moneyBelief.getParameters().get(0)).intValue();

        if (money < 10) {
            println("we do not have enough money.");
            return null;
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
        return null;
    }

    private Action planFight() {

        // get position
        LinkedList<LogicBelief> beliefs = null;
        beliefs = getAllBeliefs("position");
        if (beliefs.size() == 0) {
            println("strangely I do not know my position");
            return MarsUtil.rechargeAction();
        }
        String position = beliefs.getFirst().getParameters().firstElement();

        // if there is an enemy on the current position then attack or defend
        Vector<String> enemies = new Vector<String>();
        beliefs = getAllBeliefs("visibleEntity");
        for (LogicBelief b : beliefs) {
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
        if (enemies.size() != 0) {
            println("there are " + enemies.size() + " enemies at my current position");

            LinkedList<LogicBelief> healthBeliefs = this.getAllBeliefs("health");

            if (healthBeliefs.size() > 0) {
                LogicBelief healthBelief = healthBeliefs.get(0);
                int health = new Integer(healthBelief.getParameters().get(0)).intValue();

                if (health == 1) {
                    println("I am going to  run like a coward");
                    // if there is an enemy on a neighboring vertex do not go there
                    beliefs = getAllBeliefs("neighbor");
                    Vector<String> neighbors = new Vector<String>();
                    for (LogicBelief b : beliefs) {
                        neighbors.add(b.getParameters().firstElement());
                    }

                    Vector<String> vertices = new Vector<String>();
                    beliefs = getAllBeliefs("visibleEntity");
                    for (LogicBelief b : beliefs) {
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
                        String vertex = vertices.firstElement();
                        println("I will goto " + vertex);
                        return MarsUtil.gotoAction(vertex);
                    } else {
                        println("I am done for. I'll parry like forever");
                        return MarsUtil.parryAction();
                    }
                }
            }

            Collections.shuffle(enemies);
            String enemy = enemies.firstElement();
            println("I will attack " + enemy);
            return MarsUtil.attackAction(enemy);
        }

        // if there is an enemy on a neighboring vertex to there
        beliefs = getAllBeliefs("neighbor");
        Vector<String> neighbors = new Vector<String>();
        for (LogicBelief b : beliefs) {
            neighbors.add(b.getParameters().firstElement());
        }

        Vector<String> vertices = new Vector<String>();
        beliefs = getAllBeliefs("visibleEntity");
        for (LogicBelief b : beliefs) {
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
        if (vertices.size() != 0) {
            println("there are " + vertices.size() + " adjacent vertices with enemies");
            Collections.shuffle(vertices);
            String vertex = vertices.firstElement();
            println("I will goto " + vertex);
            return MarsUtil.gotoAction(vertex);
        }

        return null;
    }

    private Action planRandomWalk() {

        LinkedList<LogicBelief> beliefs = getAllBeliefs("neighbor");
        Vector<String> neighbors = new Vector<String>();
        for (LogicBelief b : beliefs) {
            neighbors.add(b.getParameters().firstElement());
        }

        if (neighbors.size() == 0) {
            println("strangely I do not know any neighbors");
            return MarsUtil.rechargeAction();
        }

        // goto neighbors
        Collections.shuffle(neighbors);
        String neighbor = neighbors.firstElement();
        println("I will go to " + neighbor);
        return MarsUtil.gotoAction(neighbor);

    }

}
