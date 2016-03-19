package net.orthus.rocketevolution.planets;

import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Kinetic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.math.Vector;

/**
 * Created by Chad on 04-Mar-16.
 */
public class Earth implements Kinetic {

    //===== CONSTANTS
    public static final double MASS = 5.972e24; // kg
    public static final double RADIUS = 6371000; // m

    //===== INSTANCE VARIABLES
    private Kinematic kinematic;

    //===== CONSTRUCTOR
    public Earth(){
        kinematic = new Kinematic();
    }

    //===== PRIVATE STATIC METHODS

    /**
     * Uses NASA model for atmosphere found here: grc.nasa.gov/www/k-12/airplane/atmosmet.html
     * @param altitude distance from sea-level in meters
     * @return ambient pressure in Pascals.
     */
    private static double atmosphericPressure(double altitude){

        altitude = (altitude < 0)? 0 : altitude;
        double p = 0;

        // upper stratosphere (highest)
        if(altitude > 25000){
            p = ((2.99e-3 * altitude) + 141.89) / 216.6;
            p = Math.pow(p, -11.388) * 2000.488;

            // troposphere (lowest)
        }else if(altitude < 11000){
            p = ((6.49e-3 * altitude) + 288.14) / 288.08;
            p = Math.pow(p, 5.256) * 101000.29;

            // lower stratosphere (middle)
        }else
            p = Math.exp(1.73 - (1.57e-4 * altitude)) * 22000.65;

        return p;

    } // end atmosphericPressure

    /**
     * Uses NASA model for atmosphere to return what the temperature would be at a given altitude.
     * @param altitude distance from sea-level in meters
     * @return ambient temperature in degrees centigrade.
     */
    private static double atmosphericTemperature(double altitude){

        altitude = (altitude < 0)? 0 : altitude;
        double t = 0;

        // upper stratosphere (highest)
        if(altitude > 25000)
            t = -131.21 + (0.00299 * altitude);

            // troposphere (lowest)
        else if(altitude < 11000)
            t = 15.04 - (0.00649 * altitude);

            // lower stratosphere (middle)
        else
            t = -56.46;

        return t;
    }

    /**
     * Uses NASA model for atmosphere to return what the density would be at a given altitude.
     * @param altitude distance from sea-level in meters.
     * @return density in kg/m^3
     */
    private static double atmosphericDensity(double altitude){

        altitude = (altitude < 0)? 0 : altitude;

        return atmosphericPressure(altitude)
                / (1000.2869 * (atmosphericTemperature(altitude) + 273.1));
    }

    //===== PUBLIC STATIC METHODS

    public static double pressure(Vector position){
        return atmosphericTemperature(position.getMagnitude() - RADIUS);
    }

    public static Vector gravity(double mass, Vector position){
        return Physics.gravitationalForce(MASS, mass, position.negate());
    }

    public static double acceleration(double r){
        return Physics.G * MASS / Math.pow(r, 2);
    }

    //===== INTERFACE
    @Override
    public Kinematic getKinematics() {
        return kinematic;
    }

} // Earth
