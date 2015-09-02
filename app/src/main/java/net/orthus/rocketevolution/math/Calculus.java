package net.orthus.rocketevolution.math;

import java.util.ArrayList;

/**
 * Created by Chad on 7/24/2015.
 *
 * For integrating/differentiating simple expressions. Simple expressions are ones that can
 * be solved using the power rule.
 */
public class Calculus {

    //=== INSTANCE VARIABLES

    private VarSum function;
    private char variable; // to reference

    //=== CONSTRUCTOR

    /**
     * @param function like 3x^2 + 2x - 5
     * @param variable the variable to reference, 'x' in 3x^2
     */
    public Calculus(VarSum function, char variable){
        this.function = function;
        this.variable = variable;
    }

    //=== PUBLIC METHODS

    /**
     * Uses the power-rule
     * @return derivative of function
     */
    public VarSum differentiate(){
        ArrayList<Variable> list = new ArrayList<Variable>();

        // for each Variable in function
        for(int i=0; i < function.expression.size(); i++){
            Variable v = function.expression.get(i);

            // if the power is 1, replace with just coefficient
            if(Variable.isEqual(v.getPower(), (double) 1))
                list.add(new Variable(v.getCoefficient()));

            // else perform the power-rule (when not a constant)
            else if(!v.isNumber())
                list.add(new Variable(v.getCoefficient() * v.getPower(),
                        v.getVariable(), v.getPower() - 1));
        }

        return new VarSum(list);

    } // end differentiate

    /**
     * Uses power-rule only
     * @return anti-derivative of function (without constant).
     */
    public VarSum integrate(){
        ArrayList<Variable> list = new ArrayList<Variable>();

        // for each Variable in function
        for(int i=0; i < function.expression.size(); i++){
            Variable v = function.expression.get(i);

            // if a constant, multiply by variable
            if(v.isNumber())
                list.add(new Variable(v.getCoefficient(), variable, 1));

            // else perform reverse power-rule
            else
                list.add(new Variable(v.getCoefficient() / (v.getPower() + 1),
                        v.getVariable(), v.getPower() + 1));
        }

        return new VarSum(list);

    } // end integrate

    /**
     * Uses input/output pair to solve for a complete integral with the constant.
     * @param input some input with known output for F(x)
     * @param output a known output for F(x)
     * @return complete integral with constant
     */
    public VarSum integrate_complete(double input, double output){
        VarSum v = this.integrate();
        double c = output - v.evaluate((float) input);
        v.expression.add(new Variable(c));
        return v;
    }

    /**
     * Evaluates the integral from the lower to the upper limit
     * @param lower limit of integration
     * @param upper limit of integration
     * @return value of evaluated integral
     */
    public double integrate(double lower, double upper){
        VarSum res = integrate();
        return res.evaluate(upper) - res.evaluate(lower);
    }

    /**
     * Uses 0 for lower-limit.
     * @param upper limit of integration
     * @return value of integral evaluated from 0 to upper limit
     */
    public double integrate(float upper){
        return integrate(0, upper);
    }
    
} // end Calculus


