package net.orthus.rocketevolution.simulation.Fitnesses;

import android.support.annotation.NonNull;

import net.orthus.rocketevolution.simulation.Fitness;

/**
 * Created by Chad on 27-Apr-16.
 */
public class SpecificImpulse extends Fitness {

    private double isp;

    public SpecificImpulse(double isp){
        this.isp = isp;
    }

    public double getIsp(){ return isp; }

    @Override
    public String name() {
        return "Specific Impulse";
    }

    @Override
    public int compareTo(@NonNull Fitness another) {

        SpecificImpulse a = (SpecificImpulse) another;

        if(isp > a.getIsp())
            return -1;
        else
            return 1;

    }
}
