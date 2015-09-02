package net.orthus.rocketevolution;

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
    public static float atmosphericPressure(float altitude){

        float p = 0;

        // upper stratosphere (highest)
        if(altitude > 25000){
            p = ((2.99e-3f * altitude) + 141.89f) / 216.6f;
            p = (float) Math.pow(p, -11.388) * 2000.488f;

        // troposphere (lowest)
        }else if(altitude < 11000){
            p = ((6.49e-3f * altitude) + 288.14f) / 288.08f;
            p = (float) Math.pow(p, 5.256) * 101000.29f;

        // lower stratosphere (middle)
        }else
            p = (float) Math.exp(1.73f - (1.57e-4f * altitude)) * 22000.65f;

        return p;

    } // end atmosphericPressure
}
