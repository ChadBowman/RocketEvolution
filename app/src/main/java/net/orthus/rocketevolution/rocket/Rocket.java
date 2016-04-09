package net.orthus.rocketevolution.rocket;

import android.support.annotation.NonNull;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;
import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Kinetic;

import java.net.PortUnreachableException;
import java.util.UUID;

/**
 * Created by Chad on 7/23/2015.
 */
public class Rocket implements Kinetic, Comparable<Rocket>{

    // proportion of Rocket volume dedicated to support systems
    public static final double MIN_INERT_PROPORTION = 0.1; //TODO check
    public static final double MAX_INERT_PROPORTION = 0.4; //TODO check if this is realistic

    private Chromosome chromosome;
    private Fuselage fuselage;
    private Kinematic kinematics;
    private Simulation simulation;
    private Fitness fitness;

    public Rocket(Chromosome chromosome){
        fuselage = new Fuselage(chromosome);
    }

    public Rocket(){
        chromosome = new Chromosome().randomize();
        this.fuselage = new Fuselage(chromosome); // base unit in CMs
        this.kinematics = new Kinematic();
    }



    //===== STATIC METHODS
    public static Tuple<Integer> randomizeMassDistributions(){

        double inert = Utility.rand(MIN_INERT_PROPORTION, MAX_INERT_PROPORTION);
        double fuel = Utility.rand(0, 1 - inert);

        Tuple<Integer> result = new Tuple<>();
        result.add((int)(inert * 1000));
        result.add((int)(fuel * 1000));

        return result;
    }

    //===== ACCESSORS
    public Fuselage getFuselage(){ return fuselage; }
    public Chromosome getChromosome(){ return chromosome;}
    public Simulation getSimulation(){ return simulation; }
    public Fitness getFitness(){ return fitness; }

    public void setFitness(Fitness fitness){ this.fitness = fitness; }
    public void setSimulation(Simulation sim){ simulation = sim; }

    //===== INTERFACES
    public Kinematic getKinematics(){
        return kinematics;
    }

    @Override
    public int compareTo(Rocket another) {
        return fitness.compareTo(another.getFitness());
    }
} // end Rocket
