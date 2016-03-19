package net.orthus.rocketevolution.simulation;

import net.orthus.rocketevolution.math.Vector;

/**
 * Created by Chad on 2/29/2016.
 */
public class Frame {

    //===== INSTANCE VARIABLES
    private Vector position;
    private double fuel;
    private float direction;

    //===== CONSTRUCTOR
    public Frame(Vector position, float direction, double fuel){
        this.position = position;
        this.direction = direction;
        this.fuel = fuel;
    }

    //===== PUBLIC METHODS
    public String toString(){
        return String.format("%s %.3f %.0f%%", position.toString(), direction, fuel * 100);
    }

    //===== ACCESSORS
    public Vector getPosition(){ return position; }
    public float getDirection(){ return direction; }
    public double getRemainingFuelProportion(){ return fuel; }

} // end Frame
