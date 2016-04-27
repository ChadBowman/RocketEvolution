package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.math.VarSum;
import net.orthus.rocketevolution.math.Variable;
import net.orthus.rocketevolution.utility.Utility;

import java.util.Random;

/**
 * Created by Chad on 15-Mar-16.
 */
public class Guidance {

    private int engineCount;

    public Guidance(Rocket rocket){
        engineCount = rocket.getFuselage().engineCount();
    }

    public VarSum[] testThrottle(){

        VarSum[] t = noThrottle();
        int i = (t.length / 2) - 1;

        if(new Random().nextFloat() < 0.3)
            i += 2;

        if(new Random().nextFloat() < 0.6)
            t[i] = new VarSum(new Variable(0.9));


        return t;
    }

    // TODO: 15-Mar-16 implement an actual algorithim
    public VarSum[] noThrottle(){

        VarSum[] eqs = new VarSum[engineCount];

        for(int i=0; i < engineCount; i++)
            eqs[i] = new VarSum(new Variable(1));

        return eqs;
    }

    public VarSum[] noGimbal(){

        VarSum[] eqs = new VarSum[engineCount];

        for(int i=0; i < engineCount; i++)
            eqs[i] = new VarSum(new Variable(0));

        return eqs;
    }

    public double[] throttle(VarSum[] funct, double time){


        double[] throts = new double[engineCount];

        for(int i=0 ; i < engineCount; i++)
            throts[i] = funct[i].evaluate(time);


        return throts;
    }

    public float[] gimbal(VarSum[] funct, double time){

        float[] gimbs = new float[engineCount];

        for(int i=0 ; i < engineCount; i++)
            gimbs[i] = (float) funct[i].evaluate(time);


        return gimbs;
    }


} // end Guidance
