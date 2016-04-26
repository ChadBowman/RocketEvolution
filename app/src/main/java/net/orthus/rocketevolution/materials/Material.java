package net.orthus.rocketevolution.materials;

import net.orthus.rocketevolution.utility.Hash;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;

/**
 * Created by Chad on 2/16/2016.
 */
public abstract class Material {

    //===== CONSTANTS
    public static final int TEST_MAT = 0;

    //===== CLASS VARIABLES
    public static Hash<Integer, Material> materials = new Hash<>();

    //===== INSTANCE VARIABLES
    private int id;
    protected String name;

    //===== CONSTRUCTOR
    protected Material(int id){
        this.id = id;
    }

    //===== CLASS METHODS
    public static Tuple<Integer> randomizeMaterialParameters(){

        // grab a valid index
        Integer fuelID = Utility.rand(0, materials.entries() - 1);
        // return a fuel ID at that index
        fuelID = materials.keys().get(fuelID);

        return new Tuple<>(fuelID);
    }

} // Material
