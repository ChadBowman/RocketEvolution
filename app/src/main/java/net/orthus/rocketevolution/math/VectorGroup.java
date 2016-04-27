package net.orthus.rocketevolution.math;

import java.util.ArrayList;

/**
 * Created by Chad on 7/28/2015.
 */
public class VectorGroup {

    //=== INSTANCE VARIABLES

    ArrayList<Vector> vectors;
    Piecewise perimeter; // function all the Vectors make when their endpoints are connected with a line

    //=== CONSTRUCTORS

    public VectorGroup(Vector[] vectors){
        this.vectors = new ArrayList<Vector>();

        for(Vector v : vectors)
            this.vectors.add(v);
    }

    public VectorGroup(ArrayList<Vector> vectors){
        this.vectors = vectors;
        perimeter = tracePerimeter();
    }

    //=== PRIVATE METHODS

    /**
     * Connects all the Vector tips to form a discontinuous function
     * @return bounded linear function of all Vector tips connected together
     */
    private Piecewise tracePerimeter(){

        ArrayList<VarSum> functions = new ArrayList<VarSum>();

        Vector v1, v2;
        for(int i=1; i < vectors.size(); i++) {
            v1 = vectors.get(i-1);
            v2 = vectors.get(i);
            functions.add(new VarSum(v1.getX(), v1.getY(), v2.getX(), v2.getY(), true));
        }

        return new Piecewise(functions);
    }


    /**
     * @return 2D area of VectorGroup
     */
    public double totalArea(){
        double result = 0.0;

        for(int i=1; i < vectors.size(); i++)
            result += vectors.get(i-1).area(vectors.get(i));

        return result;
    }

    //=== PUBLIC METHODS

    /**
     * Rotates all vectors counter-clock-wise Pi/4
     * @return VectorGroup with all Vectors rotated counter-clock-wise
     */
    public VectorGroup rotateCCW(){
        ArrayList<Vector> rotated = new ArrayList<Vector>();

        for(Vector v : vectors)
            rotated.add(new Vector(v.getY() * -1, v.getX()));

        return new VectorGroup(rotated);
    }

    public VectorGroup negateX(){
        ArrayList<Vector> negated = new ArrayList<>();

        for(Vector v : vectors)
            negated.add(new Vector(-v.getX(), v.getY()));

        return new VectorGroup(negated);
    }

    /**
     * Rotates all vectors clock-wise Pi/4
     * @return VectorGroup with all Vectors rotated clock-wise
     */
    public VectorGroup rotateCW(){
        ArrayList<Vector> rotated = new ArrayList<>();

        for(Vector v : vectors)
            rotated.add(new Vector(v.getY(), v.getX() * -1));

        return new VectorGroup(rotated);
    }

    public VectorGroup rotate(float theta){
        ArrayList<Vector> rotated = new ArrayList<>();

        // for each vector v, rotate and add to a new list
        for(Vector v : vectors)
            rotated.add(v.addAngle(theta));

        return new VectorGroup(rotated);
    }

    public VectorGroup reCenter(Vector center){
        ArrayList<Vector> list = new ArrayList<>();

        for(Vector v : vectors)
            list.add(v.subtract(center));


        return new VectorGroup(list);
    }

    public VectorGroup multiplyAll(double num){
        ArrayList<Vector> list = new ArrayList<Vector>();

        for(Vector v : vectors)
            list.add(v.multiply(num));

        return new VectorGroup(list);
    }

    /**
     * When VG is half of symmetric body about X-axis, it calculates the 3D centroid of the 3D
     * object when this VectorGroup is spun about the X-axis
     * @return X-bar of 3D shape when VG is spun about X-axis
     */
    public double centroid3D(){
        double moment = Math.PI * perimeter.square()
                .multiply(new VarSum(new Variable(1, Variable.X))).integrate();
        return moment / volume();
    }

    /**
     * When VG is half of symmetric body about X-axis, it
     * calculates the 2D centroid (centroid2D, 0)
     * @return X-bar of VectorGroup
     */
    public double centroid2D(){
        double moment = perimeter.multiply(new VarSum(new Variable(1, Variable.X))).integrate();
        return moment / totalArea();
    }

    /**
     * Useful with Vectors which all have positive Y-values. Calculates teh volume of the 3D shape
     * made when VectorGroup is spun around the x-axis.
     * @return volume of 3D shape spun around x-axis.
     */
    public double volume(){
        return Math.PI * perimeter.square().integrate();
    }

    /**
     * Useful with Vectors which all have positive Y-values. Calculates surface area of 3D shape
     * made when VectorGroup is spun around the x-axis.
     * @return surface area of 3D shape spun around x-axis
     */
    public double surfaceArea(){
        return 2 * Math.PI * perimeter.integrate();
    }

    /**
     * @return an ArrayList of all Vectors
     */
    public ArrayList<Vector> getVectorList(){ return vectors; }

    public Vector[] getVectorArray(){
        return vectors.toArray(new Vector[vectors.size()]);
    }

    /**
     * @return the number of Vectors in the VectorGroup
     */
    public int numberOfVectors(){ return vectors.size(); }

} // end VectorGroup
