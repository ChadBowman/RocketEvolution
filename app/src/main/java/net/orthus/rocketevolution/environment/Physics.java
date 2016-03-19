package net.orthus.rocketevolution.environment;

import net.orthus.rocketevolution.math.Vector;

/**
 * Created by Chad on 8/5/2015.
 *
 * A utility class
 */
public class Physics {

    // ideal gas constant
    public static final double R = 8.3144621;
    public static final double G = 6.67408e-11; // m^3/(kg s^2)



    public static double gravitationalForce(double m1, double m2, double distance){
        return (G * m1 * m2) / Math.pow(distance, 2);
    }

    public static Vector gravitationalForce(double m1, double m2, Vector distance){

        // calculate magnitude of force
        double mag = gravitationalForce(m1, m2, distance.getMagnitude());

        // return vector with new magnitude in the direction of m2
        return distance.newMagnitude(mag);
    }


} // end Physics
