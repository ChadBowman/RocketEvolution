package net.orthus.rocketevolution.simulation;

import net.orthus.rocketevolution.math.Vector;

/**
 * Created by Chad on 2/29/2016.
 */
public class Frame {

    //===== INSTANCE VARIABLES
    private Vector position, velocity;
    private double fuel;
    private float direction;

    //===== CONSTRUCTOR
    public Frame(Vector position, Vector velocity, float direction, double fuel){
        this.position = position;
        this.velocity = velocity;
        this.direction = direction;
        this.fuel = fuel;
    }

    //===== PUBLIC METHODS
    public String toString(){
        return String.format("X:%s V:%s %.3f %.0f%%", position.toString(), velocity.toString(), direction, fuel * 100);
    }

    //===== ACCESSORS
    public Vector getPosition(){ return position; }
    public Vector getVelocity(){ return velocity; }
    public float getDirection(){ return direction; }
    public double getRemainingFuelProportion(){ return fuel; }

} // end Frame
