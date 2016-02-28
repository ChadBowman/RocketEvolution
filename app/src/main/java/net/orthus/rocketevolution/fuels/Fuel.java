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
    protected static final int KEROSENE_PEROXIDE = 0;

    //===== CLASS VARIABLES
    public static ArrayList<Fuel> fuels = new ArrayList<Fuel>();

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
    protected Fuel(int id, double density){
        this.id = id;
        this.density = density;
    }

    //===== CLASS METHODS

    public static Tuple<Integer> randomizeFuelParameters(){
        Integer fuelID = Utility.rand(0, fuels.size() - 1);
        //multiplied to keep it an int, will divide when time to make double again
        Integer fuelOxRatio = (int) (fuels.get(fuelID).randomizeFuelOxRatio() * 1000);

        return new Tuple<Integer>(fuelID, fuelOxRatio);
    }

    //===== ABSTRACT METHODS
    protected abstract double molecularWeight(double pressure);
    protected abstract double specificHeatRatio(double pressure);
    protected abstract double adiabaticFlameTemp(double pressure);
    protected abstract double chamberPressure(double fuelOxRatio);
    protected abstract double randomizeFuelOxRatio();
    public abstract Fuel create(double fuelOxRatio);


    //===== ACCESSORS
    public double getDensity() { return density; }
    public double getTemperature(){ return temperature; }
    public double getPressure(){ return pressure; }
    public double getSpecificHeatRatio(){ return specificHeatRatio; }
    public double getMolecularWeight(){ return molecularWeight; }
    public void setDensity(double density) { this.density = density; }

} // end Fuel
