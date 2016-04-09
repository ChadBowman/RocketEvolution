package net.orthus.rocketevolution.simulation;

/**
 * Created by Chad on 27-Mar-16.
 */
public abstract class Fitness implements Comparable<Fitness> {

    public static final int ALTITUDE = 0;

    @Override
    public abstract int compareTo(Fitness another);
}
