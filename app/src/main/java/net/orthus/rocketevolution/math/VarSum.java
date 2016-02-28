package net.orthus.rocketevolution.math;

import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chad on 7/24/2015.
 *
 * Represents the sum of Variables. Assumes all Variables have like-terms.
 */
public class VarSum {

    private static final int N = 1000;
    private static final double TOLERANCE = 0.0001;

    //=== INSTANCE VARIABLE

    // List of Variables which are summed together
    public ArrayList<Variable> expression;
    // Bounds of function
    public double lowerBound;
    public double upperBound;

    //=== CONSTRUCTORS

    /**
     * Simplest constructor
     * @param variables variables to add to sum
     */
    public VarSum(Variable... variables){
        expression = new ArrayList<Variable>();

        Collections.addAll(expression, variables);

        this.lowerBound = Double.NEGATIVE_INFINITY;
        this.upperBound = Double.POSITIVE_INFINITY;
    }

    /**
     * Passes in an AList of Variables to sum together
     * @param expression list of variables
     */
    public VarSum(ArrayList<Variable> expression){
        this.expression = expression;
        this.lowerBound = Double.NEGATIVE_INFINITY;
        this.upperBound = Double.POSITIVE_INFINITY;
    }

    /**
     * Used when creating a bounded function.
     * @param expression ArrayList of Variables to sum
     * @param lower lower bound of function
     * @param upper upper bound of function
     */
    public VarSum(ArrayList<Variable> expression, double lower, double upper){
        this.expression = expression;
        this.lowerBound = lower;
        this.upperBound = upper;
    }

    /**
     * Uses two points to create a function for the line.
     * @param x1 X-value for first point
     * @param y1 Y-value for first point
     * @param x2 X-value for second point
     * @param y2 Y-value for second point
     * @param bounded if true, will set bounds from x1 to x2
     */
    public VarSum(double x1, double y1, double x2, double y2, boolean bounded){

        this.expression = new ArrayList<Variable>();

        // solve for line
        double m = (Variable.isEqual(x2 - x1, 0.0, TOLERANCE))?
                Double.POSITIVE_INFINITY : (y2 - y1) / (x2 - x1);
        double b = y1 - (m * x1);

        expression.add(new Variable(m, Variable.X));
        expression.add(new Variable(b));

        if(bounded){
            this.lowerBound = x1;
            this.upperBound = x2;
        }else{
            this.lowerBound = Double.NEGATIVE_INFINITY;
            this.upperBound = Double.POSITIVE_INFINITY;
        }
    }

    //=== PUBLIC METHODS

    /**
     * @return true if both bounds are defined, else false
     */
    public boolean isBounded(){
        return (lowerBound != Double.NEGATIVE_INFINITY) && (upperBound != Double.POSITIVE_INFINITY);
    }

    /**
     * Checks if any Variable in the sum is a radicand
     * @return true if any Variable is a radicand, else false
     */
    public boolean hasRadicand(){
        for(int i=0; i < expression.size(); i++)
            if(expression.get(i).isRadicand())
                return true;

        return false;
    }

    /**
     * Adds variables to the expression. Does no evaluating.
     * @param vars variables to add
     */
    public void addVariables_(Variable... vars){
        for(Variable v : vars)
            expression.add(v);
    }

    /**
     * Includes more Variables, does no actual addition.
     * @param vars variables to add to sum
     * @return new VarSum
     */
    public VarSum addVariables(Variable... vars){
        ArrayList<Variable> list = new ArrayList<Variable>();
        list.addAll(expression);
        for(Variable v : vars)
            list.add(v);

        return new VarSum(list);
    }

    /**
     * Multiplies both expressions to make one large one. Doesn't simplify.
     * EX: (3X + 2)*(4 + X) = (12X + 3X^2 + 8 + 2X)
     * @param ex expression to multiply by
     * @return product of this * ex
     */
    public VarSum multiply(VarSum ex){
        ArrayList<Variable> replacement = new ArrayList<Variable>();

        // multiply each combination and add them to a new list
        for(int i=0; i < expression.size(); i++)
            for(int j=0; j < ex.expression.size(); j++)
                replacement.add(expression.get(i).multiply(ex.expression.get(j)));


        return new VarSum(replacement, lowerBound, upperBound);
    }

    /**
     * multiply() with side-effects
     * @param ex expression to multiply by
     */
    public void multiply_(VarSum ex){
        this.expression = this.multiply(ex).expression;
    }

    /**
     * multiplies this VarSum by itself
     * @return this VarSum squared
     */
    public VarSum square(){
        return this.multiply(this);
    }

    /**
     * Evaluates expression at x.
     * @param x number to evaluate expression by.
     * @return the expression evaluated at x.
     */
    public double evaluate(double x){
        double result = 0;

        // for each Variable, evaluate at x
        for(int i=0; i < expression.size(); i++)
            result += expression.get(i).evaluate(x);

        return result;
    }

    /**
     * @return human-readable String of expression. EX: f(x) = x^2 + 3x
     */
    public String toString(){
        // TODO simplify like-terms
        String s = String.format("f(%c) = %s",
                expression.get(0).getVariable(), expression.get(0).toString());

        // for each Variable, add it
        for(int i=1; i < expression.size(); i++)
            s += " + " + expression.get(i).toString();

        return s;

    } // end toString


    /**
     * Solves for roots for the domain between bottom and top. Problems can occur if roots are
     * closer than 0.3 from each other. Automatically removes the negative domain if expression
     * has a radicand (x^.5).
     * @param bottom lower bound of the domain to evaluate
     * @param top upper bound of domain to evaluate
     * @return array of solutions found, empty if none.
     */
    public Double[] solve(double bottom, double top){

        // remove sections which aren't in the domain
        if(bottom < 0 && hasRadicand())
            bottom = 0;

        // solution set
        ArrayList<Double> solutions = new ArrayList<Double>();

        // set up the intervals
        double range = top - bottom;    // range of the domain
        double slices = 3 * range;      // how many intervals to survey per unit
        double dx = range / slices;     // the interval to evaluate

        // grab initial values
        double previousInput = bottom;
        double previousOutput = evaluate(bottom);

        // check if initial value is a solution
        if(Math.abs(previousOutput) < TOLERANCE)
            solutions.add(bottom);

        // placeholders
        double input, output;
        double solution; // used when a solution is converged upon

        // for each interval along domain
        for(int i=1; i < slices; i++){

            // evaluate
            input = (i * dx) + bottom;
            output = evaluate(input);

            // if a solution is found, add it
            if(Math.abs(output) < TOLERANCE)
                solutions.add(input);

            // else if the interval contains a root
            else if(!Utility.sameSign(previousOutput, output)
                    && Math.abs(previousOutput) > TOLERANCE) {

                // converge upon the root
                solution = converge(previousInput, input);

                // if the solution is found, add it
                if(solution != Double.NaN)
                    solutions.add(solution);
            }

            // move to the next interval
            previousInput = input;
            previousOutput = output;

        } // end for

        // return the solutions as array
        return solutions.toArray(new Double[solutions.size()]);

    } // end solve

    /**
     * Used by solve to recursively converge on a solution. Will return the first solution it finds
     * which is why solve() is needed to properly survey and split the domain up for converve()
     * @param bottom lower bound of domain
     * @param top upper  bound of domain
     * @return the first root found within the domain, NaN if no solution is found
     */
    private double converge(double bottom, double top){

        // interval
        double dx = (top - bottom) / N;

        // start with initial conditions
        double previousInput = bottom;
        // the first evaluation is guaranteed not to be a solution when used with solve()
        double previousOutput = evaluate(bottom);

        // placeholders
        double input, output;

        // for each interval
        for(int i=1; i < N; i++){

            // evaluate
            input = (dx * i) + bottom;
            output = evaluate(input);

            // if a solution is found, return it
            if(Math.abs(output) < TOLERANCE)
                return input;

            // if the interval contains a root, converge further
            if(!Utility.sameSign(previousOutput, output))
                return converge(previousInput, input);

            // if nothing interesting is found, continue on
            previousInput = input;
            previousOutput = output;

        } // end for

        // return NaN if the search has failed
        return Double.NaN;

    } // end converge


} // end VarSum