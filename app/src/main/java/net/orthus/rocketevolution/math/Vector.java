package net.orthus.rocketevolution.math;

/**
 * Created by Chad on 7/23/2015.
 *
 * Uses a pair of longs as the components of a 2D vector.
 */
public class Vector {

    //=== INSTANCE VARIABLES
    private long x, y;

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
    public Vector(long x, long y){
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
    public Vector(long x1, long y1, long x2, long y2){
        this.x = x2 - x1;
        this.y = y2 - y1;
    }

    /**
     * Takes polar inputs, converts to cartesian for storage.
     * @param magnitude Length of vector.
     * @param theta Angle from right side of X-axis.
     */
    public Vector(long magnitude, float theta){
        setPolar(magnitude, theta);
    }

    //=== PUBLIC METHODS

    /**
     * Takes in polar inputs and converts to cartesian for representation of the vector.
     * Angles are converted to be between 0 and 2PI. Negative magnitudes are converted to an
     * equivalent positive magnitude.
     * @param magnitude Length of vector.
     * @param theta Angle from the right side of the X-axis.
     */
    public void setPolar(long magnitude, float theta){

        // if magnitude is negative, convert
        if(magnitude < 0){
            // point in the opposite direction
            theta += Math.PI;
            // make magnitude positive
            magnitude *= -1;
        }

        // if angle negative, make equivalently positive
        while(theta < 0)
            theta += 2 * Math.PI;

        // bound from 0 to 2PI
        theta %= 2 * Math.PI;

        // set components
        this.x = (long) (magnitude * Math.cos(theta));
        this.y = (long) (magnitude * Math.sin(theta));

    } // end setPolar

    /**
     * Sums each component to form a new vector
     * @param vector vector to add to this
     * @return Vector of components summed
     */
    public Vector add(Vector vector){
        return new Vector(this.x + vector.getX(), this.y + vector.getY());
    }

    public Vector subtract(Vector vector){
        return new Vector(this.x - vector.getX(), this.y - vector.getY());
    }

    public Vector multiply(double scalar){
        return new Vector((long)(x * scalar), (long)(y * scalar));
    }

    /**
     * @param vector Vector to cross this against
     * @return the magnitude of the cross product
     */
    public long cross(Vector vector){
        return (x * vector.getY()) - (y * vector.getX());
    }

    public Vector newMagnitude(long magnitude){
        return new Vector(magnitude, getAngle());
    }

    public void newMagnitude_(long magnitude){
        setPolar(magnitude, getAngle());
    }

    public Vector newAngle(float theta){
        return new Vector((long) (getMagnitude() + .5), theta);
    }

    public void newAngle_(float theta){
        setPolar((long) (getMagnitude() + 0.5), theta);
    }

    public Vector addAngle(float theta){
        return new Vector((long) getMagnitude(), getAngle() + theta);
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
    public long area(Vector v){
        // half the determinate
        long d = (x * v.getY()) - (y * v.getX());
        return Math.abs(d) / 2;
    }

    /**
     * @param v Vector to compare against
     * @return true if both components are the same, else false
     */
    public boolean isEqual(Vector v){
        return (x == v.getX()) && (y == v.getY());
    }

    public boolean isZero(){
        return (x == 0) && (y == 0);
    }

    /**
     * @param v Vector to compare against
     * @return true if both vectors have the same length, else false
     */
    public boolean sameMagnitude(Vector v){
        return Variable.isEqual(this.getMagnitude(), v.getMagnitude());
    }

    /**
     * Displays human-readable expression of vector.
     * @return string in human-readable vector format.
     */
    public String toString(){
        return String.format("<%d, %d>", x, y);
    }

    //=== ACCESSORS

    public long getX(){ return x; }
    public long getY(){ return y; }
    public void setX(long x){ this.x = x; }
    public void setY(long y){ this.y = y; }

} // end Vector
