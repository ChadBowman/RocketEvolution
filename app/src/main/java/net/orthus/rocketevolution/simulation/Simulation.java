package net.orthus.rocketevolution.simulation;


import net.orthus.rocketevolution.simulation.Fitnesses.Altitude;
import net.orthus.rocketevolution.simulation.Fitnesses.Drag;
import net.orthus.rocketevolution.simulation.Fitnesses.DragCoefficient;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chad on 2/29/2016.
 */
public class Simulation {

    private ArrayList<Frame> history;
    private int interval;
    private boolean rud;

    // Fitness'
    private Altitude altitude;
    private DragCoefficient coefficient;
    private Drag drag;

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

    //===== ACCESSORS

    public int getInterval(){ return interval; }
    public int getSize(){ return history.size(); }
    public Altitude getAltitude(){ return altitude; }
    public DragCoefficient getCoefficient(){ return coefficient; }
    public Drag getDrag(){ return drag; }

    public void setAltitude(Altitude x){ altitude = x; }
    public void setCoefficient(DragCoefficient x){ coefficient = x; }
    public void setDrag(Drag x){ drag = x; }

} // end Simulation
