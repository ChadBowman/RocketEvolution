package net.orthus.rocketevolution.fuels;

import net.orthus.rocketevolution.utility.Utility;

/**
 * Created by Chad on 8/3/2015.
 *
 * Fuel/oxidizer Kerosene & Hydrogen Peroxide
 */
public class KerosenePeroxide extends Fuel {


    //TODO when any Fuel class is initialized, it's ID will be checked and appropriate name passed
    // through. This is so String resources can be used properly

    //===== CONSTRUCTORS

    /**
     * Use when recreating previously generated Fuel
     * @param name text-name of Fuel. String resource should be passed in.
     */
    public KerosenePeroxide(String name, double density, double fuelOxRatio){
        super(KEROSENE_PEROXIDE);

        this.name = name;
        this.density = density;
        this.fuelOxRatio = fuelOxRatio;

        pressure = chamberPressure(fuelOxRatio);
        temperature = adiabaticFlameTemp(pressure);
        specificHeatRatio = specificHeatRatio(pressure);
        molecularWeight = molecularWeight(pressure);
    }

    //===== OVERRIDES

    /**
     * Uses data from braeunig.us to estimate the specific heat ratio of gas at any ratio/pressure.
     * @return specific heat ratio
     */
    @Override
    public double specificHeatRatio(double pressure) {

        // regression from data
        double lam = 1.981198 - (0.0110168 * Math.log(pressure));

        // every 0.2 change in the ratio, yields a 0.0105 change in specific heat
        double dif = (fuelOxRatio - 7.85f) / 0.2;

        // increase in ratio yields less specific heat
        lam -= 0.0105 * dif;

        return lam;
    }

    /**
     * Uses data regressed from braeunig.us to estimate the molecular weight of the fuel/oxidizer
     * when burned.
     * @return molecular weight of compound when burned in combustion chamber
     */
    @Override
    public double molecularWeight(double pressure) {

        // equation extrapolated from chart
        double m = 21.63036 + (0.0574614 * Math.log(pressure));

        // the eq above was created using a 7.85 peroxide/kerosene ratio
        // every 0.1 change in the ratio, yields a 0.5 change in m
        double dif = (fuelOxRatio - 7.85f) / 0.1;

        // add in the adjustments
        m += 0.5 * dif;

        return m;
    }

    //===== PROTECTED METHODS
    @Override
    protected double adiabaticFlameTemp(double pressure){

        //TODO to implement based off chamber pressure
        return Utility.rand(2500, 4000);
    }

    @Override
    protected double chamberPressure(double ratio){

        //TODO implement based off ratio
        return Utility.rand(2.5e6, 2.3e7);
    }

    /**
     * Accomplished via research
     * @return
     */
    @Override
    public double randomizeFuelOxRatio(){

        // ratio for RP-1 and LOX can be from 7.2 to 8.2
        // This is discovered via research
        return Utility.rand(7.2, 8.2);
    }

    @Override
    public Fuel create(double fuelOxRatio) {

        return new KerosenePeroxide(name, density, fuelOxRatio);
    }


} // end KerosenePeroxide
