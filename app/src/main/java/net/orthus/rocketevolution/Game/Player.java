package net.orthus.rocketevolution.Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chad on 10-Apr-16.
 */
public class Player implements Serializable {

    //===== INSTANCE VARIABLES
    private int selectedFitness;
    private ArrayList<UUID> generation;

    public Player(){
        generation = new ArrayList<>();
    }


    public boolean write(File file){

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
    public int getSelectedFitness(){ return selectedFitness; }
    public ArrayList<UUID> getGeneration(){ return generation; }

    public void setSelectedFitness(int x){ selectedFitness = x; }
    public void setGeneration(ArrayList<UUID> x){ generation = x; }


} // Player
