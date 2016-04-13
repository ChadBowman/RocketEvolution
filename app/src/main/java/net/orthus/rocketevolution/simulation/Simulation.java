package net.orthus.rocketevolution.simulation;


import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;

/**
 * Created by Chad on 2/29/2016.
 */
public class Simulation {

    private ArrayList<Frame> history;
    private int interval;
    private boolean rud;

    //===== CONSTRUCTOR
    public Simulation(ArrayList<Frame> history, int interval, boolean rud){

        this.history = history;
        this.interval = interval;
        this.rud = rud;
    }

    public boolean isRUD(double time){

        time = (time < 0)? 0 : time;

        int idx = (int) (time * interval);
        idx = (idx > history.size() - 1)? history.size() : idx;

        return rud && idx == history.size();
    }

    public Frame position(double time){

        time = (time < 0)? 0 : time;

        int idx = (int) (time * interval);
        idx = (idx > history.size() - 1)? history.size() : idx;
        return history.get(idx);
    }

    public int getInterval(){ return interval; }
    public int getSize(){ return history.size(); }

    public void print(int x){
        for(Frame f : history)
            Utility.p("[%d] %s", x, f.toString());
    }

} // end Simulation
