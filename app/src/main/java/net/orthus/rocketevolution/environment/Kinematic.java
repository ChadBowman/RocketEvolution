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

    private float rotPos;

    private double rotVel, rotAcc;

    public Kinematic(){
        position = new Vector();
        velocity = new Vector();
        acceleration = new Vector();
    }


    //===== ACCESSORS
    public Vector getPosition(){ return position; }
    public Vector getVelocity(){ return velocity; }
    public Vector getAcceleration(){ return acceleration; }
    public void setPosition(Vector v){ position = v; }
    public void setVelocity(Vector v){ velocity = v; }
    public void setAcceleration(Vector v){ acceleration = v; }
    public float getRotPos(){ return rotPos; }
    public double getRotVel(){ return rotVel; }
    public double getRotAcc(){ return rotAcc; }
    public void setRotPos(float x){ rotPos = Vector.normalizeAngle(x); }
    public void setRotPos(double x){ rotPos = Vector.normalizeAngle((float) x); }
    public void setRotVel(double x){ rotVel = x; }
    public void setRotAcc(double x){ rotAcc = x; }

} // end Kinematic
