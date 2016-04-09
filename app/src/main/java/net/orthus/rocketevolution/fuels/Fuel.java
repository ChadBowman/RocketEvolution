package net.orthus.rocketevolution.fuels;

import net.orthus.rocketevolution.utility.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chad on 8/3/2015.
 *
 * Abstract class for fuel/oxidizer compounds.
 */
public abstract class Fuel{

    //=== CONSTANTS
    public static final int KEROSENE_PEROXIDE = 0;

    //===== CLASS VARIABLES
    public static Hash<Integer, Fuel> fuels = new Hash<>();

    //===== INSTANCE VARIABLES
    // ID particular to each specific Fuel
    private int id;
    // Text name of fuel (resource)
    protected String name;
    // Ratio of fuel/oxidizer
    protected double fuelOxRatio,
                    density,    // density of liquid kg/m^3
                    temperature,
                    pressure,
                    specificHeatRatio,
                    molecularWeight;

    //===== CONSTRUCTOR
    protected Fuel(int id){
        this.id = id;
    }

    //===== CLASS METHODS

    public static Tuple<Integer> randomizeFuelParameters(){

        // grab a valid index
        Integer fuelID = Utility.rand(0, fuels.entries() - 1);
        // return a fuel ID at that index
        fuelID = fuels.keys().get(fuelID);

        //multiplied to keep it an int, will divide when time to make double again
        Integer fuelOxRatio = (int) (fuels.get(fuelID).randomizeFuelOxRatio() * 1000);

        Tuple<Integer> result = new Tuple<>();
        result.add(fuelID);
        result.add(fuelOxRatio);

        return result;
    }

    //===== ABSTRACT METHODS
    protected abstract double molecularWeight(double pressure);
    protected abstract double specificHeatRatio(double pressure);
    protected abstract double adiabaticFlameTemp(double pressure);
    protected abstract double chamberPressure(double fuelOxRatio);
    public abstract double randomizeFuelOxRatio();
    public abstract int fuelOxRatioValidity(double ratio);
    public abstract double minimumFuelOxRatio();
    public abstract double maximumFuelOxRatio();
    public abstract Fuel create(double fuelOxRatio);


    //===== ACCESSORS
    public double getDensity() { return density; }
    public double getTemperature(){ return temperature; }
    public double getPressure(){ return pressure; }
    public double getSpecificHeatRatio(){ return specificHeatRatio; }
    public double getMolecularWeight(){ return molecularWeight; }
    public void setDensity(double density) { this.density = density; }

} // end Fuel
