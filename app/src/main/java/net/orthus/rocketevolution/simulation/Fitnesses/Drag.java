package net.orthus.rocketevolution.simulation.Fitnesses;

import net.orthus.rocketevolution.simulation.Fitness;

/**
 * Created by Chad on 26-Apr-16.
 */
public class Drag extends Fitness{

    private double drag;

    public Drag(double drag){
        this.drag = drag;
    }

    public double getDrag(){ return drag; }

    @Override
    public String name(){
        return "Drag";
    }

    @Override
    public int compareTo(Fitness another) {
        Drag d = (Drag) another;

        if(drag < d.getDrag())
            return -1;
        else
            return 1;
    }
}
