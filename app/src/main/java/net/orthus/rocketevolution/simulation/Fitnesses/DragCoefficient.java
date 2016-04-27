package net.orthus.rocketevolution.simulation.Fitnesses;

import net.orthus.rocketevolution.simulation.Fitness;

/**
 * Created by Chad on 26-Apr-16.
 */
public class DragCoefficient extends Fitness {

    private double coefficient;

    public DragCoefficient(double coefficient){
        this.coefficient = coefficient;
    }

    public double getDragCoefficient(){ return coefficient; }

    @Override
    public String name(){
        return "Drag Coefficient";
    }

    @Override
    public int compareTo(Fitness another) {

        DragCoefficient x = (DragCoefficient) another;

        if(coefficient < x.getDragCoefficient())
            return -1;
        else
            return 1;
    }
}
