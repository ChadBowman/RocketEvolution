package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;
import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Kinetic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Chad on 7/23/2015.
 */
public class Rocket implements Serializable, Kinetic, Comparable<Rocket>{

    // proportion of Rocket volume dedicated to support systems
    public static final double MIN_INERT_PROPORTION = 0.1; //TODO check
    public static final double MAX_INERT_PROPORTION = 0.4; //TODO check if this is realistic

    public static final int FALCON9R_MASS = 505846;

    private Chromosome chromosome;
    private Fuselage fuselage;
    private Kinematic kinematics;
    private Simulation simulation;
    private Fitness fitness;
    private UUID id;

    //===== CONSTRUCTORS
    public Rocket(Chromosome chromosome){
        fuselage = new Fuselage(chromosome);
        id = UUID.randomUUID();
        this.kinematics = new Kinematic();
    }

    public Rocket(){
        chromosome = new Chromosome().randomize();
        this.fuselage = new Fuselage(chromosome); // base unit in CMs
        this.kinematics = new Kinematic();
        id = UUID.randomUUID();
    }

    //===== PUBLIC METHODS
    public boolean isViable(){

        Vector a = fuselage.netThrust(Earth.atmosphericPressure(Earth.RADIUS), null, null)
                .multiply(1 / fuselage.mass()).add(Earth.seaLevelAcc());

        //Utility.p("Viable with " + a.toString());
        return a.getAngle() < Math.PI && a.getMagnitude() < 100;

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
    public UUID getId(){ return id; }

    public void setFitness(Fitness fitness){ this.fitness = fitness; }
    public void setSimulation(Simulation sim){ simulation = sim; }

    //===== INTERFACES
    public Kinematic getKinematics(){
        return kinematics;
    }

    public boolean write(File directory){

        File file = new File(directory, id.toString() + ".roc");

        try{
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
            fos.close();
            return true;

        }catch(IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static Rocket load(File file){

        Rocket r = null;

        try{
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fis);
            r = (Rocket) in.readObject();
            in.close();
            fis.close();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return r;
    }

    @Override
    public int compareTo(Rocket another) {
        return simulation.fitness.compareTo(another.getSimulation().fitness);
    }
} // end Rocket
