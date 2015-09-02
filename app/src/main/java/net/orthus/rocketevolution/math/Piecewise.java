package net.orthus.rocketevolution.math;

import java.util.ArrayList;

/**
 * Created by Chad on 7/27/2015.
 */
public class Piecewise {

    private ArrayList<VarSum> functions;


    public Piecewise(ArrayList<VarSum> functions) {
        this.functions = functions;
    }

    public Piecewise multiply(VarSum expression) {

        ArrayList<VarSum> products = new ArrayList<VarSum>();

        for (VarSum element : functions)
            products.add(element.multiply(expression));

        return new Piecewise(products);
    }

    public Piecewise square() {
        ArrayList<VarSum> results = new ArrayList<VarSum>();

        for (VarSum each : functions)
            results.add(each.square());

        return new Piecewise(results);
    }

    public Piecewise multiply_(VarSum expression) {

        for (VarSum each : functions)
            each.multiply_(expression);

        return this;
    }

    public Piecewise integrate_() {
        ArrayList<VarSum> results = new ArrayList<VarSum>();

        for (VarSum func : functions)
            results.add(new Calculus(func, Variable.X).integrate());

        this.functions = results;

        return this;
    }

    public double integrate() {
        double result = 0.0;

        for (VarSum vars : functions)
            result += new Calculus(vars, Variable.X)
                    .integrate(vars.lowerBound, vars.upperBound);

        return result;
    }

    public double[] integrate_each() {
        double[] result = new double[functions.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = new Calculus(functions.get(i), Variable.X)
                    .integrate(functions.get(i).lowerBound, functions.get(i).upperBound);

        return result;
    }
} // end Piecewise



