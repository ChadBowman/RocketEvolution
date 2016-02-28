package net.orthus.rocketevolution.environment;

import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.math.VectorGroup;

/**
 * Created by Chad on 11/12/2015.
 */
public class Kinematic {

    private Vector position,
                    velocity,
                    acceleration;

    private double rotPos,
                    rotVel,
                    rotAcc;

    public Kinematic(){
        this.position = new Vector();
        this.velocity = new Vector();
        this.acceleration = new Vector();
    }

    //=== ACCESSORS
    public Vector getPosition(){ return position; }
    public Vector getVelocity(){ return velocity; }
    public Vector getAcceleration(){ return acceleration; }
    public void setPosition(Vector v){ position = v; }
    public void setVelocity(Vector v){ velocity = v; }
    public void setAcceleration(Vector v){ acceleration = v; }
    public double getRotPos(){ return rotPos; }
    public double getRotVel(){ return rotVel; }
    public double getRotAcc(){ return rotAcc; }
    public void setRotPos(double x){ rotPos = x; }
    public void setRotVel(double x){ rotVel = x; }
    public void setRotAcc(double x){ rotAcc = x; }

} // Kinematic
