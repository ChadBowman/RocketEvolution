package net.orthus.rocketevolution.simulation;

import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.utility.Utility;

import java.util.Locale;

/**
 * Created by Chad on 2/29/2016.
 */
public class Frame {

    //===== INSTANCE VARIABLES
    private Vector position, velocity, acceleration;
    private double fuel;
    private float direction;

    //===== CONSTRUCTOR
    public Frame(Vector position, Vector velocity, Vector acceleration, float direction, double fuel){
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.direction = direction;
        this.fuel = fuel;
    }

    //===== PUBLIC METHODS
    public String toString(){
        return String.format(Locale.US, "X:%s V:%s A:%s %.0f%s %.0f%%",
                position.toString(),
                velocity.toString(),
                acceleration.toString(),
                Utility.radianToDegree(direction), Utility.DEGREE,
                fuel * 100);
    }

    //===== ACCESSORS
    public Vector getPosition(){ return position; }
    public Vector getVelocity(){ return velocity; }
    public float getDirection(){ return direction; }
    public double getRemainingFuelProportion(){ return fuel; }

} // end Frame
