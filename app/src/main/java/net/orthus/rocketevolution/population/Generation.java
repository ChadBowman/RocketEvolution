package net.orthus.rocketevolution.population;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.evolution.SimpleCrossover;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.simulation.Simulator;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Utility;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Chad on 20-Mar-16.
 */
public class Generation {

    private Hash<UUID, Rocket> generation;

    public Generation(int number){
        generation = generate(number);
    }

    public Generation(Generation previous){
        generation = new SimpleCrossover(previous.getGeneration()).evolve();
    }

    //===== PUBLIC METHODS
    public void runSims(){

        for(Rocket rocket : generation.values()) {
            //new Thread(){
            //   public void run(){
            rocket.setSimulation(new Simulator(rocket).run(3, 60));
            //   }
            // }.start();
        }

    }

    //===== PRIVATE METHODS
    private Hash<UUID, Rocket> generate(int number){

       final Hash<UUID, Rocket> gen = new Hash<>();

        for(int i=0; i < number; i++)
            gen.add(UUID.randomUUID(), new Rocket());

        return gen;
    }

    //===== ACCESSORS
    public Hash<UUID, Rocket> getGeneration(){ return generation; }

} // Generation
