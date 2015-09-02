package net.orthus.rocketevolution;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Chad on 7/29/2015.
 */
public class Utility<O> {

    public static final int MILLION = 1000000;

    public ArrayList<O> reverse(ArrayList<O> list){

        ArrayList<O> result = new ArrayList<O>();
        
        for(int i = list.size() -1; i >= 0; i--)
            result.add(list.get(i));
        
        return result;
    }

    public ArrayList<O> sub(ArrayList<O> list, int first, int second){

        ArrayList<O> result = new ArrayList<O>();

        for(int i=first; i <= second; i++)
            result.add(list.get(i));

        return result;
    }

    public static int rand(int lowerBound, int upperBound){

        Random r = new Random();
        int i = r.nextInt(upperBound - lowerBound);

        return i + lowerBound;
    }

    public static float rand(float lowerBound, float upperBound){

        int i = rand((int)(lowerBound * MILLION), (int)(upperBound * MILLION));

        return i / (float) MILLION;
    }

    public static boolean sameSign(double first, double second){

        if(first == Double.POSITIVE_INFINITY || first == Double.NEGATIVE_INFINITY
                || second == Double.POSITIVE_INFINITY || second == Double.NEGATIVE_INFINITY)
            return true;

        if(first > 0 && second > 0)
            return true;
        if(first < 0 && second < 0)
            return true;
        if(first == 0 && second == 0)
            return true;

        return false;
    }

    public static void p(String fmt, Object... args){
        System.out.println(String.format("O& " + fmt, args));
    }

}
