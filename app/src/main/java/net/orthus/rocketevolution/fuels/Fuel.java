package net.orthus.rocketevolution.fuels;

/**
 * Created by Chad on 8/3/2015.
 *
 * Abstract class for fuel/oxidizer compounds.
 */
public abstract class Fuel {

    //=== CONSTANTS
    public static final int NUMBER_OF_FUELS = 1;

    public static final int KEROSENE_PEROXIDE = 0;

    public static final int MINIMUM_PRESSURE = 1013000,
                             MAXIMUM_PRESSURE = 25331000;

    //=== VARIABLES
    // ID particular to each specific Fuel
    protected int id;
    // Text name of fuel (resource)
    protected String name;
    // Ratio of fuel/oxidizer
    protected float fuelOxRatio,
                    density;    // density of liquid

    //=== CONSTRUCTOR
    protected Fuel(int id, float density){
        this.id = id;
        this.density = density;
    }

    //=== ABSTRACT METHODS
    public abstract float molecularWeight(float pressure);
    public abstract float specificHeatRatio(float pressure);

    //=== ACCESSORS
    public float getDensity() { return density; }
    public void setDensity(float density) { this.density = density; }

} // end Fuel
