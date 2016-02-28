package net.orthus.rocketevolution.simulation;

import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.utility.Tuple;

/**
 * Created by Chad on 2/7/2016.
 */
public class Simulator {

    private Rocket rocket;

    public Simulator(Rocket rocket){
        this.rocket = rocket;


    }

    public Tuple<Vector> run(int updatesPerSecond, int maxTime){
        Tuple<Vector> run = new Tuple<Vector>();



        return run;
    }
}
