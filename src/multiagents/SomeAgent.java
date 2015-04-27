/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagents;

import eis.iilang.Action;
import eis.iilang.Percept;

/**
 *
 * @author cristopherson
 */
public class SomeAgent extends massim.javaagents.Agent {

    public SomeAgent(String name, String team) {
        super(name, team);
    }
    
    @Override
    public Action step() {
        //deliberate and return an action
        return null;
    }

    @Override
    public void handlePercept(Percept p) {
        //handle percepts if neccesary
    }
}
