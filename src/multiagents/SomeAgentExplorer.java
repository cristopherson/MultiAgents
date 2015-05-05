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
import java.util.Collection;
import java.util.Vector;
import massim.javaagents.agents.MarsUtil;
import massim.javaagents.agents.SimpleExplorerAgent;

/**
 *
 * @author cristopherson
 */
public class SomeAgentExplorer extends massim.javaagents.Agent {

    public SomeAgentExplorer(String name, String team) {
        super(name, team);
        SimpleExplorerAgent agent;
    }

    @Override
    public Action step() {
        //deliberate and return an action
        handleMessages();
        handlePercepts();
        return null;
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
                    println("I will keep that in mind");
                    continue;
                }
                println("but I am not interested in that gibberish");
            }
        }

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
                    addBelief(b);
                } else {
                }
            } else if (p.getName().equals("visibleEdge")) {
                LogicBelief b = MarsUtil.perceptToBelief(p);
                if (containsBelief(b) == false) {
                    addBelief(b);
                } else {
                }
            } else if (p.getName().equals("probedVertex")) {
                LogicBelief b = MarsUtil.perceptToBelief(p);
                if (containsBelief(b) == false) {
                    println("I perceive the value of a vertex that I have not known before");
                    addBelief(b);
                    broadcastBelief(b);
                } else {
                    //println("I already knew " + b);
                }
            } else if (p.getName().equals("surveyedEdge")) {
                LogicBelief b = MarsUtil.perceptToBelief(p);
                if (containsBelief(b) == false) {
                    println("I perceive the weight of an edge that I have not known before");
                    addBelief(b);
                    broadcastBelief(b);
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
            } else if (p.getName().equals("money")) {
                Integer money = new Integer(p.getParameters().get(0).toString());
                removeBeliefs("money");
                addBelief(new LogicBelief("money", money.toString()));
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

}
