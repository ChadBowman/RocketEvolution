package net.orthus.rocketevolution.math;

/**
 * Created by Chad on 7/23/2015.
 *
 * Uses a pair of doubles as the components of a 2D vector.
 */
public class Vector implements Comparable<Vector>{

    //=== CONSTANTS
    // Tolerance for double comparison
    final double DELTA = 0.0000001;

    //=== INSTANCE VARIABLES
    private double x, y;

    //=== CONSTRUCTORS

    /**
     * Creates the zero-vector;
     */
    public Vector(){}

    /**
     * Takes cartesian inputs.
     * @param x X component of vector.
     * @param y Y component of vector.
     */
    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a vector out of two ordered pairs. Translates the line segment to the origin.
     * @param x1 x value of the first ordered pair.
     * @param y1 y value of the first ordered pair.
     * @param x2 x value of the second ordered pair.
     * @param y2 y value of the second ordered pair.
     */
    public Vector(double x1, double y1, double x2, double y2){
        this.x = x2 - x1;
        this.y = y2 - y1;
    }

    /**
     * Takes polar inputs, converts to cartesian for storage.
     * @param magnitude Length of vector.
     * @param theta Angle from right side of X-axis.
     */
    public Vector(double magnitude, float theta){
        setPolar(magnitude, theta);
    }

    //=== PUBLIC METHODS

    /**
     * Takes in polar inputs and converts to cartesian for representation of the vector.
     * Angles are converted to be between 0 and 2PI. Negative magnitudes are converted to an
     * equivalent positive magnitude.
     * @param magnitude Length of vector.
     * @param angle Angle from the right side of the X-axis.
     */
    public void setPolar(double magnitude, float angle){

        // if magnitude is negative, convert
        if(magnitude < 0){
            // point in the opposite direction
            angle += Math.PI;
            // make magnitude positive
            magnitude *= -1;
        }

        // normalize angle
        angle = normalizeAngle(angle);

        // set components
        x = magnitude * Math.cos(angle);
        y = magnitude * Math.sin(angle);

    } // end setPolar

    /**
     * Sums each component to form a new vector
     * @param vector vector to add to this
     * @return Vector of components summed
     */
    public Vector add(Vector vector){
        return new Vector(x + vector.getX(), y + vector.getY());
    }

    public void add_(Vector vector){
        x += vector.getX();
        y += vector.getY();
    }

    public Vector subtract(Vector vector){
        return new Vector(x - vector.getX(), y - vector.getY());
    }

    public Vector multiply(double scalar){
        return new Vector(x * scalar, y * scalar);
    }

    public Vector negate(){
        return new Vector(-x, -y);
    }

    /**
     * @param vector Vector to cross this against
     * @return the magnitude of the cross product
     */
    public double cross(Vector vector){
        return (x * vector.getY()) - (y * vector.getX());
    }

    public Vector newMagnitude(double magnitude){
        return new Vector(magnitude, getAngle());
    }

    public void newMagnitude_(double magnitude){
        setPolar(magnitude, getAngle());
    }

    public Vector newAngle(float theta){
        return new Vector(getMagnitude() + .5, theta);
    }

    public void newAngle_(float theta){
        setPolar(getMagnitude() + 0.5, theta);
    }


    public Vector addAngle(float theta){
        return new Vector(getMagnitude(), getAngle() + theta);
    }

    /**
     * Returns the square root of the sum of the components squared.
     * @return The length of the vector.
     */
    public double getMagnitude(){
        return Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) );
    }

    /**
     * Returns the angle from the X-axis.
     * @return The angle of the vector from 0 to 2PI.
     */
    public float getAngle(){

        // get angle from pi to -pi
        float res =  (float) Math.atan2(y, x);
        // if negative, make positive
        if(res < 0)
            res += 2 * Math.PI;

        // return result
        return res;
    }

    /**
     * Calculates the area of the triangle formed inside two vectors.
     * @param v second vector which makes up triangle.
     * @return area of triangle
     */
    public double area(Vector v){
        // half the determinate
        double d = (x * v.getY()) - (y * v.getX());
        return Math.abs(d) / 2;
    }

    /**
     * Compares the components of both Vectors.
     * @param v Vector to compare against
     * @return true if both components are within the tolerance determined via DELTA.
     */
    public boolean isEqual(Vector v){
        return Variable.isEqual(x, v.getX(), DELTA) && Variable.isEqual(y, v.getY(), DELTA);
    }

    /**
     * @return true if both components were smaller than the tolerance, else false.
     */
    public boolean isZero(){
        return Variable.isEqual(x, 0, DELTA) && Variable.isEqual(y, 0, DELTA);
    }

    /**
     * @param v Vector to compare against
     * @return true if both vectors have the same length, else false
     */
    public boolean sameMagnitude(Vector v){
        return Variable.isEqual(this.getMagnitude(), v.getMagnitude(), DELTA);
    }

    /**
     * Displays human-readable expression of vector.
     * @return string in human-readable vector format.
     */
    public String toString(){
        return String.format("<%f, %f>", x, y);
    }

    //===== STATIC METHODS

    public static float normalizeAngle(float angle){

        // if angle negative, make equivalently positive
        while(angle < 0)
            angle += 2 * Math.PI;

        // bound from 0 to 2PI
        angle %= 2 * Math.PI;

        return angle;
    }

    //===== ACCESSORS

    public double getX(){ return x; }
    public double getY(){ return y; }
    public void setX(double x){ this.x = x; }
    public void setY(double y){ this.y = y; }

    @Override
    public int compareTo(Vector v){

        if(Math.abs(getMagnitude() - v.getMagnitude()) < DELTA)
            return 0;

        if(getMagnitude() > v.getMagnitude())
            return 1;
        else
            return -1;
    }

} // end Vector
