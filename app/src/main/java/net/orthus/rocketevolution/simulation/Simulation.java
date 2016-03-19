package net.orthus.rocketevolution.simulation;


import java.util.ArrayList;

/**
 * Created by Chad on 2/29/2016.
 */
public class Simulation {

    ArrayList<Frame> history;
    int interval;

    //===== CONSTRUCTOR
    public Simulation(ArrayList<Frame> history, int interval){

        this.history = history;
        this.interval = interval;
    }

    public Frame position(double time){
        int idx = (int) (time * interval);
        idx = (idx > history.size() - 1)? history.size() : idx;
        return history.get(idx);
    }

} // end Simulation
