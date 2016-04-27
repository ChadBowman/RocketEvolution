package net.orthus.rocketevolution.simulation.Fitnesses;

import net.orthus.rocketevolution.simulation.Fitness;

import java.io.Serializable;

/**
 * Created by Chad on 07-Apr-16.
 */
public class Altitude extends Fitness {

    private double altitude;

    public Altitude(double altitude){
        this.altitude = altitude;
    }

    //===== ACCESSORS
    public double getAltitude(){ return altitude; }

    //===== OVERRIDES

    @Override
    public String name(){
        return "Altitude";
    }

    @Override
    public int compareTo(Fitness another) {
        // TODO: 07-Apr-16 Find a more elegant solution

        Altitude a = (Altitude) another;
        if(altitude < a.getAltitude())
            return 1;
        else if(a.getAltitude() < altitude)
            return -1;

        return 0;
    }

} // Altitude
