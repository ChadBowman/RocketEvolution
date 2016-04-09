package net.orthus.rocketevolution.evolution;

import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.rocket.Engine;
import net.orthus.rocketevolution.rocket.Fuselage;
import net.orthus.rocketevolution.rocket.Rocket;
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

    public Chromosome(Tuple<Integer> fuselage,
                      Tuple<Integer> engine,
                      Tuple<Integer> fuel,
                      Tuple<Integer> massDistribution,
                      Tuple<Integer> material ){

        this.fuselage = fuselage;
        this.engine = engine;
        this.fuel = fuel;
        this.massDistribution = massDistribution;
        this.material = material;
    }

    //===== PUBLIC METHODS

    public Chromosome(){}

    public Chromosome randomize(){

        return new Chromosome(
                Fuselage.randomizedFuselageParameters(),
                Engine.randomizedEngineParameters(),
                Fuel.randomizeFuelParameters(),
                Rocket.randomizeMassDistributions(),
                Material.randomizeMaterialParameters());
    }



    // Return meaningful properties
    public double inertProportion(){
        return massDistribution.get(0) / 1000.0;
    }

    public double fuelProportion(){
        return massDistribution.get(1) / 1000.0;
    }

    public double payloadProportion(){
        return 1 - inertProportion() - fuelProportion();
    }

    public Fuel fuel(){
        return Fuel.fuels.get(fuel.first()).create(fuel.last() / 1000.0);
    }

    public int engineThroatRadius(){
        return engine.first();
    }

    public int engineLength(){
        return engine.last();
    }

    public int valueCount(){
        return fuselage.size() + engine.size()
                + fuel.size() + massDistribution.size() + material.size();
    }

    public Tuple<Integer> combined(){

        Tuple<Integer> comb = new Tuple<>();
        comb.addAll(fuselage);
        comb.addAll(engine);
        comb.addAll(fuel);
        comb.addAll(massDistribution);
        comb.addAll(material);

        return comb;
    }

    public String toString(){

        String f = "";
        for(Integer val : fuselage.getList())
            f += val + ", ";

        return String.format("[%s%d, %d, %d, %d, %d, %d]",
                f, engine.first(), engine.last(), fuel.first(), fuel.last(),
                massDistribution.first(), massDistribution.last());
    }
    //===== PRIVATE METHODS


    //===== ACCESSORS
    public Tuple<Integer> getFuselage(){ return fuselage; }
    public Tuple<Integer> getEngine(){ return engine; }
    public Tuple<Integer> getFuel(){ return fuel; }
    public Tuple<Integer> getMassDistribution(){ return massDistribution; }
    public Tuple<Integer> getMaterial(){ return material; }

    public void setFuselage(Tuple<Integer> x){ fuselage = x; }
    public void setEngine(Tuple<Integer> x){ engine = x; }
    public void setFuel(Tuple<Integer> x){ fuel = x; }
    public void setMassDistribution(Tuple<Integer> x){ massDistribution = x; }
    public void setMaterial(Tuple<Integer> x){ material = x; }

} // Chromosome
