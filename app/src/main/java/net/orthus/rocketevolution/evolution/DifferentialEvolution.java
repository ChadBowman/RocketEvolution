package net.orthus.rocketevolution.evolution;

import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.rocket.Engine;
import net.orthus.rocketevolution.rocket.Fuselage;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by Ross Wendt on 18-Mar-16.
 */
public class DifferentialEvolution implements Genetic{

    //===== INSTANCE VARIABLES
    private ArrayList<Rocket> generation;
    public int crossoverRate = 7; //this is used as modulo, will need to experiment with different values, based on chromosome values
    // actually, we might be able to do something similar with the amplification factor- it could just be modulo... hrm


    //===== CONSTRUCTOR
    public DifferentialEvolution(Hash<UUID, Rocket> generation){
        this.generation = generation.values();

    }

    @Override
    public Hash<UUID, Rocket> evolve() {

        Tuple<Rocket> group = topHalfPerformers();
        Tuple<Rocket> children = new Tuple<>();

        int idx, idx2;
        for(int i=0; i < group.size(); i++){

            idx = Utility.rand(0, group.size() - 1);
            idx2 = Utility.rand(0, group.size() - 1);

            if(idx == i)
                idx = (idx + 1) % group.size();
            if(idx2 == i)
                idx2 = (idx + 1) % group.size();

            while(idx != idx2 && idx2 != i)
                idx2 = (idx + 1) % group.size();


            children.add(new Rocket(tadd(group.get(i).getChromosome(),
                    tsub(group.get(idx).getChromosome(), group.get(idx2).getChromosome()))));
        }

        for(int i=0; i < group.size(); i++){

            idx = Utility.rand(0, group.size() - 1);
            idx2 = Utility.rand(0, group.size() - 1);

            if(idx == i)
                idx = (idx + 1) % group.size();
            if(idx2 == i)
                idx2 = (idx + 1) % group.size();

            while(idx != idx2 && idx2 != i)
                idx2 = (idx + 1) % group.size();


            children.add(new Rocket(tadd(group.get(i).getChromosome(),
                    tsub(group.get(idx).getChromosome(), group.get(idx2).getChromosome()))));
        }

        Hash<UUID, Rocket> result = new Hash<>();
        for(Rocket r : children)
            result.add(r.getId(), r);

        return result;
    }

    private Tuple<Rocket> topHalfPerformers(){

        Tuple<Rocket> theBest = new Tuple<>();

        Collections.sort(generation);

        for(int i=0; i < generation.size() / 2; i++)
            theBest.add(generation.get(i));


        return theBest;
    }



    //subtraction
    private Chromosome tsub(Chromosome a, Chromosome b) {

        Chromosome chrom = new Chromosome();

        //=== Fuselage
        // set only the vector number difference for the fuselage for now.
        // this value can be negative
        chrom.setNumVectors(a.getFuselage().size() - b.getFuselage().size());

        Tuple<Integer> magnitudes = new Tuple<>();

        // smallest and largest vector amount
        Tuple<Integer> largest, smallest;

        // subtract vector lengths from each other, these may turn out to be negative for now
        if(a.getFuselage().size() > b.getFuselage().size())
            for(int i=0; i < a.getFuselage().size(); i++)
                magnitudes.add(a.getFuselage().get(i) - b.getFuselage().get(i % b.getFuselage().size()));

        else
            for(int i=0; i < b.getFuselage().size(); i++)
                magnitudes.add(b.getFuselage().get(i) - a.getFuselage().get(i % a.getFuselage().size()));

        // give chromosome the largest amount of vectors (these will be cut down later)
        chrom.setFuselage(magnitudes);

        //=== Engine
        Tuple<Integer> engine = new Tuple<>();
        engine.add(a.getEngine().first() - b.getEngine().first());  // throat radius
        engine.add(a.getEngine().last() - b.getEngine().last());    // length
        chrom.setEngine(engine);

        //=== Fuel
        Tuple<Integer> fuel = new Tuple<>();
        fuel.add(a.getFuel().first() - b.getFuel().first());        // fuel id
        chrom.setFuel(fuel);

        //=== Mass Distribution
        Tuple<Integer> massdistro = new Tuple<>();
        massdistro.add(a.getMassDistribution().first() - b.getMassDistribution().first());  // inert
        massdistro.add(a.getMassDistribution().last() - b.getMassDistribution().last());    // fuel
        chrom.setMassDistribution(massdistro);

        //=== Material
        Tuple<Integer> material = new Tuple<>();
        material.add(a.getMaterial().first() - b.getMaterial().first());    // fuel id
        chrom.setMaterial(material);

        return chrom;
    }


    //addition
    private Chromosome tadd(Chromosome a, Chromosome b) {

        Chromosome chrom = new Chromosome();

        //=== Fuselage

        Tuple<Integer> fuselage = new Tuple<>();
        int num = b.getNumVectors();
        if(a.getFuselage().size() + num < Fuselage.MIN_NUMBER_OF_FUSELAGE_VECTORS)
            num = Fuselage.MIN_NUMBER_OF_FUSELAGE_VECTORS;

        for(int i=0; i < num; i++) {
            int res = a.getFuselage().get(i % a.getFuselage().size())
                        + b.getFuselage().get(i % b.getFuselage().size());

            if (res < Fuselage.MIN_FUSELAGE_VECTOR_LENGTH)
                fuselage.add(Fuselage.MIN_FUSELAGE_VECTOR_LENGTH);
            else
                fuselage.add(res);
        }

        chrom.setFuselage(fuselage);

        //=== Engine
        Tuple<Integer> engine = new Tuple<>();

        // throat radius
        int rad = a.getEngine().first() + b.getEngine().first();
        if(rad < Engine.MIN_THROAT_RADIUS)
            engine.add(Engine.MIN_THROAT_RADIUS);
        else if(rad > Engine.MAX_THROAT_RADIUS)
            engine.add(Engine.MAX_THROAT_RADIUS);
        else
            engine.add(rad);

        int length = a.getEngine().last() + b.getEngine().last();
        if(length < Engine.minimumLength(engine.first()))
            engine.add(Engine.minimumLength(engine.first()));
        else if(length > Engine.maximumLength(engine.first()))
            engine.add(Engine.maximumLength(engine.first()));
        else
            engine.add(length);

        //=== Fuel
        int fuel = Math.abs(a.getFuel().first() + b.getFuel().first()) % Fuel.fuels.entries();
        chrom.setFuel(new Tuple<Integer>(Fuel.fuels.keys().get(fuel)));

        //=== Mass Distributions
        Tuple<Integer> mass = new Tuple<>();
        int dis = a.getMassDistribution().first() + b.getMassDistribution().first();
        if(dis > 1000)
            mass.add(1000); // 100% inert
        else
            mass.add(dis);

        // fuel distro
        mass.add(1000 - mass.first());
        chrom.setMassDistribution(mass);

        return chrom;

    } // tadd

} // DifferentialEvolution
