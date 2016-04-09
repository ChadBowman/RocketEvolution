package net.orthus.rocketevolution.evolution;

import android.util.Pair;

import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.rocket.Fuselage;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Ross Wendt on 18-Mar-16.
 */
public class DifferentialEvolution {

    /*//===== INSTANCE VARIABLES

    public int crossoverRate = 7; //this is used as modulo, will need to experiment with different values, based on chromosome values
    // actually, we might be able to do something similar with the amplification factor- it could just be modulo... hrm

    private ArrayList<Pair<Chromosome, Fitness>> population;

    //===== CONSTRUCTOR
    public DifferentialEvolution(Hash<UUID, Rocket> population){

        ArrayList<Pair<Chromosome, Fitness>> x = new ArrayList<>();

        for(Rocket rocket : population.values())
            x.add(new Pair<>(rocket.getChromosome(), rocket.fitnessScore(fitnessKey)));

        this.population = x;
    }

    @Override
    public Tuple<Rocket> evolve() {

        ArrayList<Tuple<Integer>> ResultantPopulation = new ArrayList<>();

        for (Tuple<Integer> individual : population) {

            //select three distinct individuals
            ArrayList<Tuple<Integer>> selection = createTrialVector();
            // mutate 'em, which results in a single mutant
            Tuple<Integer> mutant = mutation(selection);
            //yep, we crossover the mutant with itself, using modulo (the crossover rate above)
            Tuple<Integer> crossbred = crossover(mutant);

            *//*
            Population size, how we selection children, and whether we replace parents in the original
            gene pool are all tunable params, classically speaking. We don't necessarily need an
            implementation where we can change all that easily, so right now, here's how it works:
            We do not maintain any of the original parents- we only have mutants and crossbred
            children added back to the population. We can change it, tho
             *//*
            if (mutant.fitness() > crossbred.fitness()) {
                ResultantPopulation.add(mutant);
            } else {
                ResultantPopulation.add(crossbred);
            }
        }


        ArrayList<Tuple<Integer>> result = new ArrayList<Tuple<Integer>>();
        return null;
    }

    public ArrayList<Tuple<Integer>> createTrialVector() {

        int randIndex1 = 0, randIndex2 = 0, randIndex3 = 0;
        ArrayList<Tuple<Integer>> selection = new ArrayList<>();
        Tuple<Integer> mutant = new Tuple<>();
        boolean selected = false;

        while (randIndex1 == randIndex2 || randIndex2 == randIndex3 || randIndex1 == randIndex3) { //want three distinct indices, corresponding to distinct individuals
            randIndex1 = Utility.rand(0, population.size() - 1);
            randIndex2 = Utility.rand(0, population.size() - 1);
            randIndex3 = Utility.rand(0, population.size() - 1);
        }

        selection.add(population.get(randIndex1));
        selection.add(population.get(randIndex2));
        selection.add(population.get(randIndex3));

        return selection;
    }

    *//*
    Differential evolution! Basically, we take three unique individuals, and take the difference of two.
    Then, we add that difference to the third. Bamo! DE.

    There should be an "amplification" term on the difference, but it's typically real valued
    between 0 and 1. So we need to figure out how to deal with that
     *//*
    public Tuple<Integer> mutation(ArrayList<Tuple<Integer>> selection) {
        Tuple<Integer> mutant = new Tuple<Integer>();
        mutant = tadd(selection.get(0), tsub(selection.get(1), selection.get(2))); //this is why it's called differential evolution
        return mutant;
    }

    public Tuple<Integer> crossover(Tuple<Integer> individualIn) {
        Tuple<Integer> result = new Tuple<>();
        for (int i = 0; i < individualIn.size(); i++) {
            int modulo = individualIn.get(i) % crossoverRate;
            result.add(modulo);
        }
        return result;
    }

    public Tuple<Integer> tsub(Tuple<Integer> a, Tuple<Integer> b) { //tuple subtraction
        Tuple<Integer> result = new Tuple<>();

        for (int i = 0; i < a.size(); i++ ) {
            int subtraction = a.get(i) - b.get(i);
            result.add(subtraction);
        }

        return result;
    }

    public Chromosome tadd(Chromosome a, Chromosome b) { //tuple addition

        //=== Fuselage
        Tuple<Integer> fuselage = new Tuple<>();
        Tuple<Integer> fa = a.getFuselage(), fb = b.getFuselage();

        // have new fuselage have vector amounts somewhere between parents
        int numberOfVectors = Utility.rand(fa.size(), fb.size());

        // add values to create new fuselage, mod if one tuple were to go off the end of the array
        // should probably check for MAX_FUSELAGE_VECTOR_LENGTH but environment will probably limit for us
        for(int i=0; i < numberOfVectors; i++){

            if (i >= fa.size())
                fuselage.add(fa.get(i % fa.size()) + fb.get(i));
            else if(i >= fb.size())
                fuselage.add(fa.get(i) + fb.get(i % fb.size()));
            else
                fuselage.add(fa.get(i) + fb.get(i));
        }

        //=== Engine
        Tuple<Integer> engine = new Tuple<>();

        for(int i=0; i < a.getEngine().size(); i++)
            engine.add(a.getEngine().get(i) + b.getEngine().get(i));

        //=== Fuel
        // get a legal fuel type
        int f = (a.getFuel().first() + b.getFuel().first()) % Fuel.fuels.entries();
        // add a new fuel/ox ratio
        int ratio = a.getFuel().last() + b.getFuel().last();
        // check if result works, if not use end values
        int bound = Fuel.fuels.get(f).fuelOxRatioValidity(ratio / 1000.0);
        switch (bound){
            case -1: ratio = (int)(Fuel.fuels.get(f).minimumFuelOxRatio() * 1000); break;
            case 1: ratio = (int)(Fuel.fuels.get(f).maximumFuelOxRatio() * 1000); break;
        }

        Tuple<Integer> fuel = new Tuple<>();
        fuel.add(f);
        fuel.add(ratio);

        //=== Mass Distribution

        int inert = a.getMassDistribution().first() + a.getMassDistribution().last();
        int fuelD = b.getMassDistribution().first() + b.getMassDistribution().last();

        float scale = 1;
        // proportion cant exceed 1 (1000)
        if(inert + fuelD > 1000)
            scale = 1000f / (inert + fuelD);

        Tuple<Integer> massDist = new Tuple<>();
        massDist.add((int)(inert * scale));
        massDist.add((int)(fuelD * scale));

        //=== Material
        int m = (a.getMaterial().first() + b.getMaterial().first()) % Material.materials.entries();

        Tuple<Integer> material = new Tuple<>(m);


        return new Chromosome(fuselage, engine, fuel, massDist, material);

    } // tadd
*/
} // DifferentialEvolution
