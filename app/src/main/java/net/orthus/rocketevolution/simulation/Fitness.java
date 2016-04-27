package net.orthus.rocketevolution.simulation;

import android.support.annotation.NonNull;

import net.orthus.rocketevolution.simulation.Fitnesses.Altitude;
import net.orthus.rocketevolution.simulation.Fitnesses.Drag;
import net.orthus.rocketevolution.simulation.Fitnesses.DragCoefficient;
import net.orthus.rocketevolution.simulation.Fitnesses.SpecificImpulse;

import java.io.Serializable;

/**
 * Created by Chad on 27-Mar-16.
 */
public abstract class Fitness implements Comparable<Fitness> {

    public static final int ALTITUDE = 0,
                            DRAG_CO = 1,
                            DRAG = 2,
                            ISP = 3;

    public static final int NUMBER_OF_FITNESS = 4;

    public static Fitness getFitness(int id){

        switch (id){
            case ALTITUDE: return new Altitude(0);
            case DRAG_CO: return new DragCoefficient(0);
            case DRAG: return new Drag(0);
            case ISP: return new SpecificImpulse(0);
        }

        return null;
    }

    public abstract String name();

    @Override
    public abstract int compareTo(@NonNull Fitness another);
}
