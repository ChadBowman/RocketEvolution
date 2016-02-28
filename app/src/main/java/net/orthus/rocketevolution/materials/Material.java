package net.orthus.rocketevolution.materials;

import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;

/**
 * Created by Chad on 2/16/2016.
 */
public abstract class Material {

    //===== CLASS VARIABLES
    public static Hash<Integer, Material> materials = new Hash<Integer, Material>();

    //===== INSTANCE VARIABLES
    private int id;
    protected String name;

    //===== CONSTRUCTOR
    protected Material(int id){
        this.id = id;
        materials.add(id, this);
    }

    //===== CLASS METHODS
    public static Tuple<Integer> randomizeMaterialParameters(){
        //TODO implement
        return new Tuple<>();
    }

} // Material
