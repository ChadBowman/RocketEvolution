package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.utility.*;
import net.orthus.rocketevolution.fuels.Fuel;

/**
 * Created by Chad on 2/7/2016.
 */
public class Chromosome {

    //===== INSTANCE VARIABLES

    private Tuple<Integer> fuselage;
    private Tuple<Integer> engine;
    private Tuple<Integer> fuel;
    private Tuple<Integer> massDistribution;
    private Tuple<Integer> material;
    // Guidance system


    //===== CONSTRUCTOR

    public Chromosome(){

        fuselage = Fuselage.randomizedFuselageParameters(); // any number
        engine = Engine.randomizedEngineParameters(); // 2
        fuel = Fuel.randomizeFuelParameters(); // 2
        massDistribution = Rocket.randomizeMassDistributions(); // 1
        material = Material.randomizeMaterialParameters(); // ?
    }

    //===== PUBLIC METHODS
    public String toString(){

        String f = "";
        for(Integer val : fuselage.getList())
            f += val + ", ";

        return String.format("[%s%d, %d, %d, %d, %d, %d]",
                f, engine.first(),engine.last(), fuel.first(), fuel.last(),
                massDistribution.first(), massDistribution.last());
    }

    // Return meaningful properties
    public double getInertProportion(){
        return massDistribution.get(0) / 1000.0;
    }

    public double getFuelProportion(){
        return massDistribution.get(1) / 1000.0;
    }

    public double getPayloadProportion(){
        return 1 - getInertProportion() - getFuelProportion();
    }

    public Fuel getFuel(){
        return Fuel.fuels.get(fuel.first()).create(fuel.last() / 1000.0);
    }

    public int getEngineThroatRadius(){
        return engine.first();
    }

    public int getEngineLength(){
        return engine.last();
    }

    //===== PRIVATE METHODS


    //===== ACCESSORS
    public Tuple<Integer> getFuselage(){ return fuselage; }


} // Chromosome
