package net.orthus.rocketevolution.rocket;

import android.support.annotation.NonNull;

import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chad on 23-Apr-16.
 */
public class Aerodynamics {

    //===== CONSTANTS
    // allows next vectors to be 5% longer than last and still be considered blunt
    private final float PIT_TOLERANCE = 1.1f;
    private final int FUNNEL = 0,
                    MAJOR_PIT = 1,
                    MINOR_PIT = 2;

    public String type = "NEITHER";


    //===== INSTANCE VARIABLES
    private ArrayList<Vector> body;


    //===== CONSTRUCTOR
    public Aerodynamics(@NonNull ArrayList<Vector> halfBody){
        body = halfBody;
    }

    //===== PRIVATE METHODS
    private boolean isBlunt(List<Vector> top){

        // first check the top for pits
        if(pitScore(
                top.get(1).getMagnitude(),
                top.get(0).getMagnitude(),
                top.get(1).getMagnitude(), FUNNEL) > 0.0001)
            return false;

        // next check the rest for pits
        for(int i=1; i < top.size() - 1; i++)
            if(pitScore(
                    top.get(i-1).getMagnitude(),
                    top.get(i).getMagnitude(),
                    top.get(i+1).getMagnitude(), FUNNEL) > 0.0001)
                return false;

        return true;
    }

    private float pitScore(double first, double mid, double last, int level){

        // first check if pit exists
        if(PIT_TOLERANCE * mid > first || PIT_TOLERANCE * mid > last)
            return 0; // no pit exists, add no penalty

        // take the average of the two magnitude proportion
        // the lower this value, the higher the score (penalty)
        // 1 minus this value will give us a proportion of the max penalty to reward
        float co = 1 - (float) (((mid / first) + (mid / last))) / 2;

        switch (level){
            case MINOR_PIT: return 0.5f * co;
            case MAJOR_PIT: return 1f * co;
            case FUNNEL: return 2f * co;
        }

        throw new RuntimeException("Pit score level " + level + " doesn't exist!");
    }

    //===== PUBLIC METHODS
    public double dragCoefficient(){

        double coefficient = 0;

        // collect the vector with the largest X value (reference radius)
        int largestXIdx = 0;
        double largestX = 0;
        for(int i=0; i < body.size(); i++)
            if(body.get(i).getX() > largestX){
                largestX = body.get(i).getX();
                largestXIdx = i;
            }

        List<Vector> top = body.subList(0, largestXIdx + 1);
        List<Vector> bottom = body.subList(largestXIdx, body.size());

        // if blunt, not pits in major pits or funnel
        if( isBlunt(top) ){
            type = "BLUNT";
            // starting coefficient
            coefficient = 0.1;


            // add penalties for all minor pits
            for(int i=1; i < bottom.size() - 1; i++)
                coefficient += pitScore(
                        bottom.get(i-1).getMagnitude(),
                        bottom.get(i).getMagnitude(),
                        bottom.get(i+1).getMagnitude(), MINOR_PIT);

        // top is not blunt
        } else {

            coefficient = 1f;

            // add funnel penalty
            coefficient += pitScore(
                    body.get(1).getMagnitude(),
                    body.get(0).getMagnitude(),
                    body.get(1).getMagnitude(), FUNNEL);

            if(coefficient > 1.001)
                type = "FUNNEL";

            // add major pits
            for(int i=1; i < top.size() - 1; i++)
                coefficient += pitScore(
                        top.get(i-1).getMagnitude(),
                        top.get(i).getMagnitude(),
                        top.get(i+1).getMagnitude(), MAJOR_PIT);

            // add minor pits
            for(int i=1; i < bottom.size() - 1; i++)
                coefficient += pitScore(
                        bottom.get(i-1).getMagnitude(),
                        bottom.get(i).getMagnitude(),
                        bottom.get(i+1).getMagnitude(), MINOR_PIT);

        } // else


        return coefficient;

    } // dragCoefficient()

} // Aerodynamics
