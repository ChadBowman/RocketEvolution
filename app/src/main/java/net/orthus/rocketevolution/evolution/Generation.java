package net.orthus.rocketevolution.evolution;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.evolution.SimpleCrossover;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitnesses.Altitude;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.simulation.Simulator;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Utility;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chad on 20-Mar-16.
 */
public class Generation {

    public static final int GENERATION_SIZE = 29;

    private Hash<UUID, Rocket> generation;

    public Generation(){
        generation = new Hash<>();
    }

    public Generation(int number){
        generation = generate(number);
    }


    //===== PUBLIC METHODS

    // TODO: 26-Apr-16 thread the ops of this method
    public void runSims(){

        ArrayList<Rocket> r = generation.values();

        for(int i=0; i < r.size(); i++)
            r.get(i).setSimulation(new Simulator(r.get(i)).run(60, 100));

    }

    // TODO: 26-Apr-16 thread the ops of this method
    public void saveAll(File directory){

        for(Rocket rocket : generation.values()) {
            rocket.getChromosome().write(directory, rocket.getId());
        }

    }


    //===== PRIVATE METHODS
    private Hash<UUID, Rocket> generate(int number){

        final Hash<UUID, Rocket> gen = new Hash<>();

        int i = 0;
        while(gen.entries() != number){
            Rocket r = new Rocket();
            if(r.isViable())
                gen.add(r.getId(), r);

            i++;
        }

        Utility.p("It took an average of %.0f tries to get a useful Rocket.", i / (float) number);

        return gen;
    }

    //===== ACCESSORS
    public Hash<UUID, Rocket> getGeneration(){ return generation; }

    public void setGeneration(Hash<UUID, Rocket> x){ generation = x; }


} // Generation
