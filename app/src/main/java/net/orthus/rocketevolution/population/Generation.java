package net.orthus.rocketevolution.population;

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

    private Hash<UUID, Rocket> generation;

    public Generation(int number){
        generation = generate(number);
    }

    public Generation(Hash<UUID, Rocket> previous){
        // change this back the way it was
    }

    public Generation(Generation previous){
        generation = new SimpleCrossover(previous.getGeneration()).evolve();
    }

    //===== PUBLIC METHODS
    public ArrayList<String> idList(){
        ArrayList<String> list = new ArrayList<>();
        for(UUID id : generation.keys())
            list.add(id.toString());

        return list;
    }

    public void runSims(){

        ArrayList<Rocket> r = generation.values();
        for(int i=0; i < r.size(); i++){
            //new Thread(){
            //   public void run(){
            r.get(i).setSimulation(new Simulator(r.get(i)).run(2, 120));
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


    //===== PRIVATE METHODS
    private Hash<UUID, Rocket> generate(int number){

        final Hash<UUID, Rocket> gen = new Hash<>();

        int i = 0;
        while(gen.entries() != number){
            Rocket r = new Rocket();
            if(r.isViable()) {
                gen.add(r.getId(), r);
                //Utility.p("Merlin %f", r.getFuselage().merlin1DRatio());
            }
            i++;
        }

        Utility.p("It took an average of %.0f tries to get a useful Rocket.", i / (float) number);

        return gen;
    }

    //===== ACCESSORS
    public Hash<UUID, Rocket> getGeneration(){ return generation; }

    //===== STATIC METHODS
/*    public static Generation loadGeneration(ArrayList<String> ids, File dir){

        Hash<UUID, Rocket> gen = new Hash<>();
        for(String id : ids)
            gen.add(UUID.fromString(id), Rocket.load(new File(dir, id + ".roc")));

        return new Generation(gen);
    }*/

} // Generation
