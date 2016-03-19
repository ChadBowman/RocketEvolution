package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.math.VarSum;
import net.orthus.rocketevolution.math.Variable;

/**
 * Created by Chad on 15-Mar-16.
 */
public class Guidance {

    private Rocket rocket;
    private int engineCount;

    public Guidance(Rocket rocket){
        this.rocket = rocket;
        engineCount = rocket.getFuselage().engineCount();
    }

    public VarSum[] testThrottle(){

        VarSum[] t = noThrottle();

        for(int i=1; i < engineCount / 2; i++)
            t[1] = new VarSum(new Variable(0));

        return t;
    }

    // TODO: 15-Mar-16 implement an actual algorithims
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

    public double[] gimbal(VarSum[] funct, double time){

        double[] gimbs = new double[engineCount];

        for(int i=0 ; i < engineCount; i++)
            gimbs[i] = funct[i].evaluate(time);


        return gimbs;
    }


} // end Guidance
