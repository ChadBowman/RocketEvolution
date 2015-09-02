package net.orthus.rocketevolution.math;

/**
 * Created by Chad on 7/24/2015.
 *
 * Represents a mathematical variable such as 3X^2, where 3 is the coefficient, X is the variable,
 * and 2 is the power.
 * TODO allow coefficients to be variables and support multi-variable algebra
 */
public class Variable {

    //=== CONSTANTS
    // used in the variable location to notify the Variable is actually a constant number.
    public static final char NUMBER = '@';
    public static final char X = 'x';

    //=== PRIVATE VARIABLES
    private char variable;
    private double coefficient;
    private double power;

    //=== CONSTRUCTORS

    /**
     * Used when creating a constant number. Sets power to 0. (The power is never to be used
     * besides in comparison with other Variables.
     * @param coefficient value of constant.
     */
    public Variable(double coefficient){
        this.coefficient = coefficient;
        this.variable = NUMBER;
        this.power = 0;
    }

    /**
     * Used when creating a variable with just a coefficient. Sets power to 1.
     * @param coefficient coefficient of variable
     * @param variable character representation of variable
     */
    public Variable(double coefficient, char variable){
        this.coefficient = coefficient;
        this.variable = variable;
        this.power = 1;
    }

    /**
     * Full Constructor.
     * @param coefficient coefficient of variable
     * @param variable character representation of variable
     * @param power power variable is raised to
     */
    public Variable(double coefficient, char variable, double power){
        this.coefficient = coefficient;
        this.variable = variable;
        this.power = power;
    }

    //=== PUBLIC METHODS

    /**
     * @return true if Variable is a number/constant, false if a variable.
     */
    public boolean isNumber(){
        return variable == NUMBER;
    }

    /**
     * A radicand is a number to a fractional exponent, like a square root
     * @return true if Variable has an exponent > 0 and < 1, else false
     */
    public boolean isRadicand(){
        return getPower() < 1 && getPower() > 0;
    }

    /**
     * Two Variables are addable if their variable chars are the same as well as their powers.
     * Ex. 3X is addable with 6X but not with 3X^2 or 3Y.
     * @param v another Variable to check against.
     * @return true if v has same variable char and power, else false
     */
    public boolean isAddable(Variable v){
        if(variable == v.getVariable() && isEqual(power, v.getPower()))
            return true;
        else
            return false;
    }

    /**
     * Multiplies coefficients and adds powers to create the product Variable. Throws a
     * RuntimeException if a multi-variable product is attempted (Ex: 3X * 2Y). Allows constants to
     * be multiplied to Variables.
     * @param v
     * @return
     */
    public Variable multiply(Variable v){

        double co = coefficient * v.getCoefficient();
        double po = power + v.getPower();
        char var = variable;

        // if this is the constant and v isnt, result should use v's variable char
        if(this.isNumber() && !v.isNumber())
            var = v.variable;

        // if both ar variables with different chars, throw an exception
        if(!this.isNumber() && !v.isNumber())
            if(variable != v.getVariable()) {
                System.err.println(String.format("Invalid multiply between %s and %s.",
                        this.toString(), v.toString()));
                throw new RuntimeException();
            }

        return new Variable(co, var, po);

    } // end multiply

    /**
     * Quick way to multiply by a constant
     * @param value constant to multiply variable by
     * @return product Variable
     */
    public Variable multiply(float value){
        return multiply(new Variable(value));
    }

    /**
     * Adds two variables, throws a RuntimeException is they aren't addable.
     * @param v variable to add with
     * @return sum of variables this and v
     */
    public Variable add(Variable v){

        // blow-up if illegal add is attempted
        if(!this.isAddable(v))
            throw new RuntimeException();

        return new Variable(coefficient + v.getCoefficient(), variable, power);
    }

    /**
     * Plug and chug, baby
     * @param x number to evaluate at
     * @return value of Variable evaluated at x
     */
    public double evaluate(double x){
        if(this.isNumber())
            return coefficient;
        else
            return Math.pow(x, power) * coefficient;
    }

    /**
     * @return human-readable String. Ex: 3X^2
     */
    public String toString(){
        if(variable == NUMBER)
            return "" + coefficient;
        else {
            String po = (power == 1)? "" : "^" + power;
            return String.format("%.5f%c%s", coefficient, variable, po);
        }
    }

    //=== ACCESSORS
    public char getVariable(){ return variable; }
    public double getCoefficient(){ return coefficient; }
    public double getPower(){ return power; }

    //=== STATIC METHODS
    /**
     * Works like Double.compare
     * @param d1 a number
     * @param d2 another number
     * @return true if the difference between them is is < 0.00001
     */
    public static boolean isEqual(Double d1, Double d2){
        return Math.abs(d1 - d2) < 0.00001;
    }

} // end Variable