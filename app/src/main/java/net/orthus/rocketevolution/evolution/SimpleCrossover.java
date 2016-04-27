package net.orthus.rocketevolution.evolution;

import android.util.Pair;

import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.materials.Material;
import net.orthus.rocketevolution.rocket.Engine;
import net.orthus.rocketevolution.rocket.Fuselage;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Chad on 27-Mar-16.
 */
public class SimpleCrossover implements Genetic{

    //===== INSTANCE VARIABLES
    private ArrayList<Rocket> generation;
    private float mutationRate,
            crossoverRate;
    private Tuple<Integer> usedIs;         // already used indexes to prevent multiple breeds

    //===== Constructor

    /**
     *
     * @param generation assumes the desired fitness value is set in rockets already
     */
    public SimpleCrossover(Hash<UUID, Rocket> generation) {

        this.generation = generation.values();
        mutationRate = 0.1f;
        crossoverRate = 0.35f;
        usedIs = new Tuple<>();
    }


    //===== PRIVATE METHODS
    private Tuple<Rocket> topHalfPerformers(){

        Tuple<Rocket> theBest = new Tuple<>();

        Collections.sort(generation);

        for(int i=0; i < generation.size() / 2; i++)
            theBest.add(generation.get(i));


        return theBest;
    }


    //=== Mutation methods
    private Tuple<Integer> mutateMaterial(Tuple<Integer> x){

        return new Tuple<>(Utility.rand(0, Material.materials.entries() - 1));
    }

    private Tuple<Integer> mutateMassDistribution(Tuple<Integer> x){

        Tuple<Integer> result = new Tuple<>(x);

        // inert mass
        if(Utility.rand.nextFloat() < mutationRate) {
            int inert = (int)(Utility.rand(Fuselage.MIN_INERT_PROPORTION, Fuselage.MAX_INERT_PROPORTION) * 1000);
            result.set(0, inert);

            //check for distribution > 1
            if(inert + result.get(1) > 1000)
                result.set(1, 1000 - inert);
        }

        // fuel mass
        if(Utility.rand.nextFloat() < mutationRate)
            result.set(1, Utility.rand(result.get(0), 1000));

        return result;
    }

    private Tuple<Integer> mutateFuel(Tuple<Integer> x){

        Tuple<Integer> result = new Tuple<>();

        // if there's to be a new fuel, ratio must be changed as well
        if(Utility.rand.nextFloat() < mutationRate)
            result =  Fuel.randomizeFuelParameters();
        else{
            result.add(x.first());
            result.add((int) (Fuel.fuels.get(x.first()).randomizeFuelOxRatio() * 1000));
        }

        return result;
    }

    private Tuple<Integer> mutateEngine(Tuple<Integer> x){

        Tuple<Integer> result = new Tuple<>(x);

        if(Utility.rand.nextFloat() < mutationRate)
            result.set(0, (Utility.rand(Engine.MIN_THROAT_RADIUS, Engine.MAX_THROAT_RADIUS)));

        if(Utility.rand.nextFloat() < mutationRate)
            x.set(1, Utility.rand(Engine.minimumLength(x.first()), Engine.maximumLength(x.first())));

        return result;
    }

    private Tuple<Integer> mutateFuselage(Tuple<Integer> x){

        for(int i=0; i < x.size(); i++)
            if(Utility.rand.nextFloat() < mutationRate)
                x.set(i, Utility.rand(1, Fuselage.MAX_FUSELAGE_VECTOR_LENGTH));

        return x;
    }

    private Tuple<Integer> mutateColor(Tuple<Integer> x){

        if(Utility.rand.nextFloat() < mutationRate)
            return Fuselage.randomColor();

        return x;
    }

    //=== Crossover methods

    private Pair<Tuple<Integer>, Tuple<Integer>> cross(Tuple<Integer> a, Tuple<Integer> b){

        Tuple<Integer> childA;
        Tuple<Integer> childB;

        // determine if attributes should be crossed over
        boolean cross = Utility.rand.nextFloat() < crossoverRate;


        if(cross) {
            childA = new Tuple<>(b);
            childB = new Tuple<>(a);
        }else{
            childA = new Tuple<>(a);
            childB = new Tuple<>(b);
        }

        return new Pair<>(childA, childB);
    }

    /**
     * Fuselage designs have variable vector amounts. This method produces children with vector amounts
     *      somewhere in between and crosses over vector lengths occasionally. Since one parent will have
     *      more vectors than the other, one child will probably have more cross-over than desired. This
     *      isn't so bad considering most of the aerodynamic impact occurs with the first vectors, the
     *      ones closest to the tip of the rocket.
     * @param a parent a fuselage tuple
     * @param b parent b fuselage tuple
     * @return Pair of children with crossovered values
     */
    private Pair<Tuple<Integer>, Tuple<Integer>> crossFuselage(Tuple<Integer> a, Tuple<Integer> b){

        Tuple<Integer> childA = new Tuple<>();
        Tuple<Integer> childB = new Tuple<>();

        // give each child a new set of vectors somewhere between parents'
        int size1 = Utility.rand(a.size(), b.size());
        int size2 = Utility.rand(a.size(), b.size());

        // for each new vector in child 1
        for(int i=0; i < size1; i++)
            // if both parents have vector at this location
            if(i < a.size() && i < b.size()){
                // crossover occasionally
                if(Utility.rand.nextFloat() < crossoverRate)
                    childA.add(b.get(i));
                else
                    childA.add(a.get(i));

            // finish out the rest with the parent with the most vectors
            }else if(i < a.size()) {
                childA.add(a.get(i));

            }else if(i < b.size())
                childA.add(b.get(i));

        // repeat the same process for the second child
        for(int i=0; i < size2; i++)
            if(i < a.size() && i < b.size()){
                if(Utility.rand.nextFloat() < crossoverRate)
                    childB.add(a.get(i));
                else
                    childB.add(b.get(i));

            }else if(i < a.size()) {
                childB.add(a.get(i));

            }else if(i < b.size())
                childB.add(b.get(i));

        return new Pair<>(childA, childB);

    } // crossFuselage


    private Tuple<Rocket> breed(int selected, ArrayList<Rocket> pop){

        //group.add(new Rocket());

        // resulting children
        Tuple<Rocket> children = new Tuple<>();


        // find selected rocket's new index
        int mate = (selected + 1) % (pop.size() - 1);

        Chromosome a = pop.get(selected).getChromosome();
        Chromosome b = pop.get(mate).getChromosome();

        Chromosome child1 = new Chromosome();
        Chromosome child2 = new Chromosome();

        // fuselage
        Pair<Tuple<Integer>, Tuple<Integer>> f;
        f = crossFuselage(a.getFuselage(), b.getFuselage());
        child1.setFuselage(mutateFuselage(f.first));
        child2.setFuselage(mutateFuselage(f.second));

        // engine
        f = cross(a.getEngine(), b.getEngine());
        child1.setEngine(mutateEngine(f.first));
        child2.setEngine(mutateEngine(f.second));

        // fuel
        f = cross(a.getFuel(), b.getFuel());
        child1.setFuel(mutateFuel(f.first));
        child2.setFuel(mutateFuel(f.second));

        // mass distribution
        f = cross(a.getMassDistribution(), b.getMassDistribution());
        child1.setMassDistribution(mutateMassDistribution(f.first));
        child2.setMassDistribution(mutateMassDistribution(f.second));

        f = cross(a.getColor(), b.getColor());
        child1.setColor(mutateColor(f.first));
        child2.setColor(mutateColor(f.second));

        // material
        f = cross(a.getMaterial(), b.getMaterial());
        child1.setMaterial(mutateMaterial(f.first));
        child2.setMaterial(mutateMaterial(f.second));

        // add new children to population
        children.add(new Rocket(child1));
        children.add(new Rocket(child2));

        return children;

    } // breed()

    //===== ACCESSORS
    public void setMutationRate(float rate){ mutationRate = rate; }
    public void setCrossoverRate(float rate){ crossoverRate = rate; }


    //===== INTERFACE
    @Override
    public Hash<UUID, Rocket> evolve(){

        Hash<UUID, Rocket> newGroup = new Hash<>();
        ArrayList<Rocket> breedingGroup = topHalfPerformers().getList();

        // mix up breeding group so rockets only breed once
        Collections.shuffle(breedingGroup);

        // TODO: 27-Mar-16 thread each breading cycle in loop
        Tuple<Rocket> g;
        // for each individual in breeding group, breed with another
        for(int i=0; i < breedingGroup.size(); i++) {

            g = breed(i, breedingGroup);

            // add the children to the new group
            for(int j=0; j < g.size(); j++)
                newGroup.add(g.get(j).getId(), g.get(j));
        }

        // if generation is odd, breed in a completely random rocket for
        // more variety and to keep the bred generation the same size
        if(generation.size() % 2 == 0) {
            breedingGroup.add(new Rocket());
            g = breed(breedingGroup.size()-1, breedingGroup);
            newGroup.add(g.first().getId(), g.first());
        }

        return newGroup;

    } // evolve()

} // SimpleCrossover
