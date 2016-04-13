package net.orthus.rocketevolution.population;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.evolution.SimpleCrossover;
import net.orthus.rocketevolution.rocket.Rocket;
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

    private Hash<UUID, Rocket> generation;

    public Generation(int number){
        generation = generate(number);
    }

    public Generation(Hash<UUID, Rocket> generation){
        this.generation = generation;
    }

    public Generation(Generation previous){
        generation = new SimpleCrossover(previous.getGeneration()).evolve();
    }

    //===== PUBLIC METHODS
    public void runSims(){

        for(Rocket rocket : generation.values()) {
            //new Thread(){
            //   public void run(){
            rocket.setSimulation(new Simulator(rocket).run(5, 120));
            //   }
            // }.start();
        }

    }

    public boolean saveAll(File directory){

        for(Rocket rocket : generation.values())
            if(!rocket.write(directory))
                return false;

        return true;
    }

    public ArrayList<String> getIDs(){

        ArrayList<String> ids = new ArrayList<>();

        for(UUID id : generation.keys())
            ids.add(id.toString());

        return ids;
    }

    //===== PRIVATE METHODS
    private Hash<UUID, Rocket> generate(int number){

        final Hash<UUID, Rocket> gen = new Hash<>();

        int i = 0;
        while(gen.entries() != number){
            Rocket r = new Rocket();
            if(r.isViable())
                gen.add(r.getId(), r);
            Utility.p("Tried %d", ++i);
        }

        return gen;
    }

    //===== ACCESSORS
    public Hash<UUID, Rocket> getGeneration(){ return generation; }

    //===== STATIC METHODS
    public static Generation loadGeneration(ArrayList<String> ids, File dir){

        Hash<UUID, Rocket> gen = new Hash<>();
        for(String id : ids)
            gen.add(UUID.fromString(id), Rocket.load(new File(dir, id + ".roc")));

        return new Generation(gen);
    }

} // Generation
