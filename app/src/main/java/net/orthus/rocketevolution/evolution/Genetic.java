package net.orthus.rocketevolution.evolution;

import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Ross Wendt on 18-Mar-16.
 */
public interface Genetic {

    //ArrayList<Tuple<Integer>> Population = changeme; //Pass our chromobuddies through this guy
    public Hash<UUID, Rocket> evolve();

}
