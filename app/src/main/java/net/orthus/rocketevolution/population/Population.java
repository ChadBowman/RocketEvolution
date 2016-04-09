package net.orthus.rocketevolution.population;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Chad on 07-Apr-16.
 */
public class Population implements Serializable {

    private ArrayList<Generation> population;

    public Population(){
        population = new ArrayList<>();
    }

    public void add(Generation generation){
        population.add(generation);
    }

    public Generation get(int inx){
        return population.get(inx);
    }

    public ArrayList<Generation> getPopulation(){ return population; }
}
