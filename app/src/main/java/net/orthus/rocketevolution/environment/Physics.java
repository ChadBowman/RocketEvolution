package net.orthus.rocketevolution.environment;

/**
 * Created by Chad on 8/5/2015.
 *
 * A utility class
 */
public class Physics {

    // ideal gas constant
    public static final float R = 8.3144621f;

    /**
     * Uses NASA model for atmosphere found here: grc.nasa.gov/www/k-12/airplane/atmosmet.html
     * @param altitude distance from sea-level in meters
     * @return ambient pressure in Pascals.
     */
    public static double atmosphericPressure(double altitude){

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
    public static double atmosphericTemperature(double altitude){

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
    public static double atmosphericDensity(double altitude){

        return atmosphericPressure(altitude)
                / (1000.2869 * (atmosphericTemperature(altitude) + 273.1));
    }

} // end Physics
