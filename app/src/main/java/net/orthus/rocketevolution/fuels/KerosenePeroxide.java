package net.orthus.rocketevolution.fuels;

import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.Utility;

/**
 * Created by Chad on 8/3/2015.
 *
 * Fuel/oxidizer Kerosene & Hydrogen Peroxide
 */
public class KerosenePeroxide extends Fuel {


    //TODO when any Fuel class is initialized, it's ID will be checked and appropriate name passed
    // through. This is so String resources can be used properly

    //=== CONSTRUCTORS

    /**
     * Use first for randomizing data
     * @param name text-name of Fuel. String resource should be passed in.
     */
    public KerosenePeroxide(String name, float density){
        super(KEROSENE_PEROXIDE, density);

        // ratio can be from 7.2 to 8.2
        this.fuelOxRatio = (Utility.rand(1, 11) + 71) / 10.f;
    }

    /**
     * Use when recreating previously generated Fuel
     * @param name text-name of Fuel. String resource should be passed in.
     * @param ratio ratio of fuel to oxidizer. ONLY previously-generated values should be used.
     */
    public KerosenePeroxide(String name, float density, float ratio){
        super(KEROSENE_PEROXIDE, density);
        this.fuelOxRatio = ratio;
    }

    //=== PUBLIC METHODS

    /**
     * Uses data from braeunig.us to estimate the specific heat ratio of gas at any ratio/pressure.
     * @param pressure pressure of gas in Pa. (1013000 to 25331000)
     * @return specific heat ratio
     */
    @Override
    public float specificHeatRatio(float pressure) {

        // check bounds
        if(pressure < MINIMUM_PRESSURE || pressure > MAXIMUM_PRESSURE)
            throw new RuntimeException();

        // regression from data
        float lam = (float) (1.981198 - (0.0110168 * Math.log(pressure)));

        // every 0.2 change in the ratio, yields a 0.0105 change in specific heat
        float dif = (fuelOxRatio - 7.85f) / 0.2f;

        // increase in ratio yields less specific heat
        lam -= 0.0105 * dif;

        return lam;
    }

    /**
     * Uses data regressed from braeunig.us to estimate the molecular weight of the fuel/oxidizer
     * when burned.
     * @param pressure pressure of gas in Pa. (1013000 to 25331000)
     * @return molecular weight of compound when burned in combustion chamber
     */
    @Override
    public float molecularWeight(float pressure) {

        // check bounds of acceptable inputs and for now, throw exception
        if( pressure < MINIMUM_PRESSURE || pressure > MAXIMUM_PRESSURE)
            throw new RuntimeException();

        // equation extrapolated from chart
        float m = (float) (21.63036 + (0.0574614 * Math.log(pressure)));

        // the eq above was created using a 7.85 peroxide/kerosene ratio
        // every 0.1 change in the ratio, yields a 0.5 change in m
        float dif = (fuelOxRatio - 7.85f) / 0.1f;

        // add in the adjustments
        m += 0.5 * dif;

        return m;
    }


} // end KerosenePeroxide
