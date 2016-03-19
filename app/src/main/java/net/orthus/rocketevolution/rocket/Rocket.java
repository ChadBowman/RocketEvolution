package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;
import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Kinetic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;

/**
 * Created by Chad on 7/23/2015.
 */
public class Rocket implements Kinetic{

    // proportion of Rocket volume dedicated to support systems
    private static final double MIN_INERT_PROPORTION = 0.1; //TODO check
    private static final double MAX_INERT_PROPORTION = 0.4; //TODO check if this is realistic

    private Chromosome chromosome;
    private Fuselage fuselage;
    private Kinematic kinematics;

    public Rocket(Chromosome chromosome){
        fuselage = new Fuselage(chromosome);
    }

    public Rocket(){
        chromosome = new Chromosome();
        this.fuselage = new Fuselage(chromosome); // base unit in CMs
        this.kinematics = new Kinematic();
    }

    public Kinematic getKinematics(){
        return kinematics;
    }




    public Fuselage getFuselage(){ return fuselage; }

    //===== STATIC METHODS

    public static Tuple<Integer> randomizeMassDistributions(){

        double inert = Utility.rand(MIN_INERT_PROPORTION, MAX_INERT_PROPORTION);
        double fuel = Utility.rand(0, 1 - inert);

        return new Tuple<Integer>((int)(inert * 1000), (int)(fuel * 1000));
    }

} // end Rocket
