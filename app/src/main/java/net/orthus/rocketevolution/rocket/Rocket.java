package net.orthus.rocketevolution.rocket;

import android.support.annotation.NonNull;

import net.orthus.rocketevolution.engine.Player;
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
public class Rocket implements Kinetic, Comparable<Rocket>{

    //===== CONSTANTS
    public static final int FALCON9R_MASS = 421300,      //kg
                            FALCON9R_MASS_INERT = 25600; //kg

    //===== INSTANCE VARIABLES
    private Chromosome chromosome;
    private Fuselage fuselage;
    private Kinematic kinematics;
    private Simulation simulation;
    private UUID id;

    //===== CONSTRUCTORS
    public Rocket(Chromosome chromosome){
        this.chromosome = chromosome;
        fuselage = new Fuselage(chromosome);
        this.kinematics = new Kinematic();
        id = UUID.randomUUID();
    }

    public Rocket(){
        chromosome = new Chromosome().randomize();
        this.fuselage = new Fuselage(chromosome);
        this.kinematics = new Kinematic();
        id = UUID.randomUUID();
    }

    public Rocket(Chromosome chromosome, UUID id){
        this.chromosome = chromosome;
        this.id = id;
        kinematics = new Kinematic();
        fuselage = new Fuselage(chromosome);
    }

    //===== PUBLIC METHODS
    public boolean isViable(){

        Vector a = fuselage.netThrust(Earth.atmosphericPressure(Earth.RADIUS), null, null)
                .multiply(1 / fuselage.mass()).add(Earth.seaLevelAcc());

        return a.getAngle() < Math.PI && a.getMagnitude() < 100;

    }

    public String toString(){
        return String.format("[%s] %s", id.toString(), chromosome.toString());
    }

    //===== STATIC METHODS


    //===== ACCESSORS
    public Fuselage getFuselage(){ return fuselage; }
    public Chromosome getChromosome(){ return chromosome;}
    public Simulation getSimulation(){ return simulation; }
    public UUID getId(){ return id; }

    public void setSimulation(Simulation sim){ simulation = sim; }

    //===== INTERFACES
    public Kinematic getKinematics(){
        return kinematics;
    }


    //===== OVERRIDES

    @Override
    public int compareTo(@NonNull Rocket another) {


       switch (Player.selectedFitness){
            case Fitness.ALTITUDE:
                return simulation.getAltitude().compareTo(another.getSimulation().getAltitude());

            case Fitness.DRAG_CO:
                return simulation.getCoefficient().compareTo(another.getSimulation().getCoefficient());

            case Fitness.DRAG:
                return simulation.getDrag().compareTo(another.getSimulation().getDrag());
        }

        throw new RuntimeException("Invalid fitness selected!");
    }

} // Rocket
