package net.orthus.rocketevolution.engine;

import net.orthus.rocketevolution.simulation.Fitness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Chad on 10-Apr-16.
 */
public class Player implements Serializable {

    //===== STATIC VARIABLES
    public static int selectedFitness;

    //===== INSTANCE VARIABLES
    private ArrayList<UUID> generation;
    private int workingIndex, rocNum, genNum, fitness;

    public Player(){
        generation = new ArrayList<>();
        workingIndex = 0;
        fitness = Fitness.DRAG_CO;
        selectedFitness = Fitness.DRAG_CO; // default fitness
        rocNum = 0;
        genNum = 1;
    }


    public boolean save(File directory){

        File file = new File(directory, "player.ply");

        try{
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
            fos.close();
            return true;

        }catch(IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static Player load(File file){

        Player p = null;

        try{
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fis);
            p = (Player) in.readObject();
            in.close();
            fis.close();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return p;
    }

    //===== ACCESSORS
    public int getSelectedFitness(){ return fitness; }
    public ArrayList<UUID> getGeneration(){ return generation; }
    public int getWorkingIndex(){ return workingIndex; }
    public int getRocNum(){ return rocNum; }
    public int getGenNum(){ return genNum; }

    public void setSelectedFitness(int x){
        fitness = x;
        selectedFitness = x;
    }
    public void setGeneration(ArrayList<UUID> x){ generation = x; }
    public void setWorkingIndex(int x){ workingIndex = x; }
    public void setRocNum(int x){ rocNum = x; }
    public void setGenNum(int x){ genNum = x; }

} // Player
