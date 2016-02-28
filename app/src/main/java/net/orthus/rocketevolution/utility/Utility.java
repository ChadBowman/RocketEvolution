package net.orthus.rocketevolution.utility;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Chad on 7/29/2015.
 */
public class Utility<O> {

    public static final int MILLION = 1000000;

    public static Random rand = new Random();

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

        Tuple<Integer> nums = new Tuple<Integer>(lowerBound, upperBound);

        // if bounds are not set correctly, flip them
        nums = (nums.first() > nums.last())? nums.swap() : nums;

        lowerBound = nums.first();
        upperBound = nums.last();
        int difference = upperBound - lowerBound;

        if(difference == 0)
            return lowerBound;

        // Return a random number within the difference then add the lowerBound
        return rand.nextInt(difference) + lowerBound;
    }

    public static float rand(float lowerBound, float upperBound){

        Tuple<Float> nums = new Tuple<Float>(lowerBound, upperBound);

        // if bounds are not set correctly, flip them
        nums = (nums.first() > nums.last())? nums.swap() : nums;

        lowerBound = nums.getList().get(0);
        upperBound = nums.getList().get(1);
        float difference = upperBound - lowerBound;

        // Return a proprotion of the difference plus the lowerBound
        // notice if rand is 0, lower is returned. If rand is 1, upper is returned
        return (rand.nextFloat() * difference) + lowerBound;
    }

    public static double rand(double lowerBound, double upperBound){

        Tuple<Double> nums = new Tuple<Double>(lowerBound, upperBound);

        // if bounds are not set correctly, flip them
        nums = (nums.first() > nums.last())? nums.swap() : nums;

        lowerBound = nums.getList().get(0);
        upperBound = nums.getList().get(1);
        double difference = upperBound - lowerBound;

        // Return a proprotion of the difference plus the lowerBound
        // notice if rand is 0, lower is returned. If rand is 1, upper is returned
        return (rand.nextFloat() * difference) + lowerBound;
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

} // Utility
