package net.orthus.rocketevolution.rocket;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.usb.UsbDevice;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.ui.Animation;
import net.orthus.rocketevolution.ui.Bounds;
import net.orthus.rocketevolution.ui.Graphic;
import net.orthus.rocketevolution.utility.*;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.math.VectorGroup;
import net.orthus.rocketevolution.ui.Launchpad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chad on 7/23/2015.
 *
 * Rocket bodies are made by connecting the ends of n vectors together. Two vectors are locked
 * vertically, and the rest are distributed radially even. The minimum number of vectors a body can
 * have is two. The body is mirrored across the y-axis.
 */
public class Fuselage extends Graphic {

    //=== CONSTANTS
    // proportion of Rocket volume dedicated to support systems
    public static final double MIN_INERT_PROPORTION = 0.1; //TODO check
    public static final double MAX_INERT_PROPORTION = 0.4; //TODO check if this is realistic

    //TODO units?
    public static final int MAX_FUSELAGE_VECTOR_LENGTH = 300,
                            MIN_FUSELAGE_VECTOR_LENGTH = 5;
    public static final int MAX_NUMBER_OF_FUSELAGE_VECTORS = 50,
                            MIN_NUMBER_OF_FUSELAGE_VECTORS = 5;

    // average density of all support systems
    private final float densitySupport = 2650f; //TODO set a fixed density for support systems/materials
                                        // density of aluminum
    // average density of all payloads (needs to be fixed for proper competition)
    private final float densityPayload = 10f; //TODO set a fixed density for all payloads

    //=== INSTANCE VARIABLES

    // list of vectors which compromise the shape
    private VectorGroup shape;
    private VectorGroup horizontalHalf; // TODO remove this if the only time we use it is to calculate stats like volume, SA
    private VectorGroup centeredAtCOM;

    private double centroid,    // center of mass
                    dryMass,
                    initialFuelMass,
                    currentFuelMass,
                    width,          //TODO UNITS FOR ALL THIS SHIT DOCUMENT IT
                    height,
                    volume,
                    surfaceArea,
                    dragCoefficient;

    private int relativeCenter, trueCenter;

    private Hash<Integer, Engine> engines;
    private Vector[] rotatedLocations;

    private Fuel fuel;
    private Chromosome chromosome;
    private Path path;

    //=== CONSTRUCTORS

    public Fuselage(Chromosome chromosome){
        // Create graphic objects
        super();
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setColor(chromosome.fuselageColor());
        path = new Path();

        this.chromosome = chromosome;
        fuel = chromosome.fuel();
        Tuple<Integer> magnitudes = chromosome.getFuselage();

        // List which will eventually make up the VectorGroup to return
        ArrayList<Vector> vectorList = new ArrayList<>();

        // spoke is the angle to increment for each Vector so the angle between them is the same
        // minus 1 since two vectors will be vertical
        float spoke = (float) Math.PI / (magnitudes.size() - 1);

        // angle which points straight up
        float up = (float) Math.PI / 2f;

        // generate the vectors which will form the shape
        // start at top, then work CCW
        for(int i=0; i < magnitudes.size(); i++)
            vectorList.add( new Vector( magnitudes.get(i), up + (spoke * i) ) );

        // get coefficient with this half flipped so mostly working with positive values
        dragCoefficient = new Aerodynamics(
                new VectorGroup(vectorList).negateX().getVectorList()).dragCoefficient();
        //dragCoefficient = new Aerodynamics(
         //       new VectorGroup(vectorList).negateX().getVectorList()).dragCoefficient();


        // used for many calculations, rotated to "top" of rocket is pointing right (last index)
        horizontalHalf = new VectorGroup(vectorList).rotateCW(); //.rotateCCW().negateY();
        //Utility.p("%s", horizontalHalf.toString());

        // duplicate vectors on the opposite side for full shape
        shape = new VectorGroup(mirrorVectors(vectorList));

        // get the array list
        //ArrayList<Vector> vectors = shape.getVectorList();
        // grab the first half, plus vertical vectors
        //ArrayList<Vector> half = new Utility<Vector>().sub(vectors, 0, vectors.size() / 2);


        centroid = horizontalHalf.centroid3D();
        width = width();
        height = height();
        volume = -horizontalHalf.volume() / Launchpad.MILLION;
        surfaceArea = (long) -horizontalHalf.surfaceArea();

        // VectorGroup with COM as center instead of generated center
        centeredAtCOM = shape.reCenter(new Vector(0, centroid));
        engines = spawnEngines(centeredAtCOM);

        // used for drawing new paths when a rotation occurs
        rotatedLocations = new Vector[centeredAtCOM.numberOfVectors()];
        for(int i=0; i< rotatedLocations.length; i++)
            rotatedLocations[i] = new Vector();

        double inert = chromosome.inertProportion(),
                fuel = chromosome.fuelProportion();
        dryMass = calculateDryMass(chromosome.payloadProportion(), inert);
        initialFuelMass = calculateInitialFuelMass(fuel);
        currentFuelMass = initialFuelMass;

    } // end Fuselage()

    //=== PRIVATE METHODS



    private Hash<Integer, Engine> spawnEngines(VectorGroup fromCOM){

        Hash<Integer, Engine> engines = new Hash<>();
        Vector[] vs = fromCOM.getVectorArray();

        Engine engine;
        double x;
        boolean flag = true;

        // place Engine on the end of each Vector which doesn't have part of the rocket below it
        for(int i=0; i <= vs.length / 2; i++) {
            x = vs[i].getX();

            // check "below" for rocket
            for(int j=i+1; j < vs.length/2; j++)
                if(vs[j].getX() < x)
                    flag = false;

            if(flag) {
                // set reference vector to Engine template, place on Rocket
                engine = new Engine(chromosome, vs[i]);
                engines.add(i, engine);

                // if not the top or bottom Vector, add Engine to other side
                if((i != 0) && (i != vs.length/2)) {

                    // make a new instance of engine
                    engine = new Engine(chromosome, vs[vs.length - i]);
                    engines.add(vs.length - i, engine);
                }
            }

            flag = true; // reset flag

        } // end for


        return engines;

    } // end spawnEngines

    private Engine[] enginesInOrder(){

        Engine[] es = new Engine[engines.entries()];
        List<Integer> keys = engines.keys();
        Collections.sort(keys);

        for(int i=0; i< keys.size(); i++)
            es[i] = engines.get(keys.get(i));

        return es;
    }

    /**
     * Calculates the mass of the Rocket with empty fuel tanks.
     * @param proportionPayload proportion of the Rocket's volume for the payload
     * @return dry mass (Kg)
     */
    private double calculateDryMass(double proportionPayload, double proportionInert){

        double mass = volume * proportionInert * densitySupport;
        mass += volume * proportionPayload * densityPayload;

        return mass;
    }

    /**
     * Calculates the mass of the fuel will full fuel tanks.
     * @param proportionalFuel proportion of the Rocket's volume for the fuel
     * @return mass of fuel with full tanks (Kg)
     */
    private double calculateInitialFuelMass(double proportionalFuel){
        return volume * proportionalFuel * fuel.getDensity();
    }

    /**
     * Rocket's dry mass and current fuel mass.
     * @return the Rocket's total mass at this instant (Kg)
     */
    private double currentMass(){
        return dryMass + currentFuelMass;
    }

    /**
     * Estimates rotational inertia by using a cylinder with the same length (height) and a radius
     * that makes the cylinder have the same volume as the rocket.
     * TODO calculate exact rotational inertia using calculus.
     * @return rotational inertia divided by rocket mass.
     */
    private double currentInertia(){

        // radius of cylinder squared
        double r = volume / (Math.PI * height);
        // rotational inertia of cylinder about central diameter (orthogonal to base).
        double i = (0.25 * r) + ((1/12.0) * Math.pow(height, 2));

        return i * currentMass();
    }

    /**
     * Generates the remaining Vectors so the Fuselage shape is symmetric
     * @param vectors non-symmetric half-shape of body
     * @return symmetric, complete shape of rocket body. The bottom Vector is the last one in the
     * list and the index flows CCW.
     */
    private ArrayList<Vector> mirrorVectors(ArrayList<Vector> vectors) {

        // decrement through vectors, skipping the vertical ones. Start with last one
        // to continue the logical flow around the circle.
        ArrayList<Vector> holder = new ArrayList<>();

        for (int i = vectors.size() - 2; i > 0; i--)
            holder.add(new Vector( -vectors.get(i).getX(), vectors.get(i).getY()));

        // add held vectors to permanent list
        for(int i=0; i < holder.size(); i++)
            vectors.add(holder.get(i));

        return vectors;
    }

    private double width(){

        ArrayList<Vector> vectors = horizontalHalf.getVectorList();

        // find the longest x-value
        double value = 0;
        for(Vector v: vectors)
            if(v.getY() > value)
                value = v.getY();


        // double ita
        return value * 2;
    }

    /**
     * Calculates the maximum height of the shape.
     * @return the height of the shape.
     */
    private double height(){

        ArrayList<Vector> vectors = shape.getVectorList();

        // find the largest and smallest y values
        double largest = 0;
        double smallest = 0;
        for(int i=0; i < vectors.size(); i++) {
            largest  = (vectors.get(i).getY() > largest) ? vectors.get(i).getY() : largest;
            smallest = (vectors.get(i).getY() < smallest)? vectors.get(i).getY() : smallest;
        }

        // return sum
        return (smallest * -1) + largest;
    }

    private void burnFuel(double dt, double[] throttle){

        Engine[] engines = enginesInOrder();

        for(int i=0; i < engines.length; i++)
            currentFuelMass -= engines[i].getMassFlowRate() * throttle[i] * dt;

        if(currentFuelMass < 0)
            currentFuelMass = 0;
    }

    private double netTorque(double pa, double[] throttle, float[] gimbal){

        Engine[] engines = enginesInOrder();
        double total = 0;

        for(int i=0; i < engines.length; i++)
            total += engines[i].torque(pa, throttle[i], gimbal[i]);

        return total;
    }


    private double currentFuelProportion(){
        return currentFuelMass / initialFuelMass;
    }

    //=== PUBLIC METHODS


    public Vector netThrust(double pa, double[] throttle, float[] gimbal){

        Engine[] engines = enginesInOrder();
        Vector total = new Vector();

        if(throttle == null){
            throttle = new double[engines.length];
            for(int i=0; i < engines.length; i++)
                throttle[i] = 1;
        }

        if(gimbal == null){
            gimbal = new float[engines.length];
            for(int i=0; i < engines.length; i++)
                gimbal[i] = 0;
        }

        for(int i=0; i < engines.length; i++)
            total.add_(engines[i].thrust(pa, throttle[i], gimbal[i]));


        return total;
    }

    public Vector netAcc(double pa, double[] throttle, float[] gimbal){

        return netThrust(pa, throttle, gimbal).multiply(1.0 / currentMass());
    }

    /**
     *
     * @param pa ambient atmospheric pressure (Pa)
     * @param dt small slice of time (s)
     * @param throttle values from 0 to 1 for each engine
     * @param gimbal values from -Pi/2 to Pi/2 for each engine
     * @return Vector: net acceleration on system, Double: net torque on system, Double: Fuel proportion.
     */
    public Triple<Vector, Double, Double>
        step(double pa, double dt, double[] throttle, float[] gimbal, float rotation){

        Vector acc;     // instantaneous acceleration on rocket due to thrust
        double angAcc;  // instantaneous angular acceleration

        if(currentFuelMass > 0) {
            acc = netThrust(pa, throttle, gimbal).multiply(1.0 / currentMass())
                    .newAngle(rotation + (float)(Math.PI / 2f));

            //Utility.p("A: %f", acc.getAngle());

            angAcc = netTorque(pa, throttle, gimbal) / currentInertia();

            // burn fuel at current rate
            burnFuel(dt, throttle);

        }else{
            acc = new Vector();
            angAcc = 0;
        }

        return new Triple<>(acc, angAcc, currentFuelProportion());
    }

    public void setEngineBounds(){
        int x, y, r, height;
        for(Integer key : engines.keys()){

            Engine e = engines.get(key);

            x = (int) rotatedLocations[key].getX();
            y = (int) rotatedLocations[key].getY();
            r = (int) (e.getWidth() * 100 * getScale() / 2);
            height = (int) (e.getLength() * 100 * getScale());

            e.setBounds(new Bounds(x-r, x+r, y, y+height));
        }
    }

    //=== PUBLIC METHODS

    public double merlin1DRatio(){
        double t = engines.values().get(0)
                .thrust(Earth.atmosphericPressure(Earth.RADIUS), 1, 0).getMagnitude();

        return t / Engine.MERLIN_ID_SL_THRUST;
    }

    public double mass(){ return currentMass(); }

    public void setEnginePaint(Paint paint){

        for(Engine e : engines.values())
            e.setPaint(paint);
    }

    public void setEngineExhaust(Animation animation){

        for(Engine e : engines.values())
            e.setExhaust(animation);
    }

    public Path path(float theta){

        // rotate the negation of the center vector
        Vector center = new Vector(0, -centroid);
        // rotate about center of mass, then return to original center for shape
        ArrayList<Vector> vectors = centeredAtCOM.rotate(theta)
                .reCenter(center).getVectorList();

        // useful measurements
        int yAxis = (int) getBounds().centerX();

        // find new X axis
        // grab the vector with smallest Y value
        Vector smallestY = vectors.get(0);
        for(int i=0; i < vectors.size(); i++)
            smallestY = (vectors.get(i).getY() < smallestY.getY()) ? vectors.get(i) : smallestY;

        // place smallestY vector on bottom of boundary and follow up to reveal new X axis
        int xAxis = (int) (getBounds().getBottom() - (getScale() * -smallestY.getY()));

        //TODO temp variables for testing
        relativeCenter = (int) (xAxis - (getScale() * centroid));
        trueCenter = xAxis;

        // reset path
        path.reset();

        // start at top
        float x = (float)(yAxis + (vectors.get(0).getX() * getScale()));
        float y = (float) (xAxis - (vectors.get(0).getY() * getScale()));
        path.moveTo(x, y);
        rotatedLocations[0].set(x, y);

        // move through list
        for(int i=1; i< vectors.size(); i++){
            x = (float) (yAxis + (vectors.get(i).getX() * getScale()));
            y = (float) (xAxis - (vectors.get(i).getY() * getScale()));
            path.lineTo(x, y);
            rotatedLocations[i].set(x, y);
        }

        // close path and return
        path.lineTo(yAxis + ((float) vectors.get(0).getX() * getScale()),
                xAxis - ((float) vectors.get(0).getY() * getScale()));

        return path;

    } // end path

    @Override
    public void update() {}

    @Override
    public void draw(Canvas canvas) {

        canvas.drawPath(path(getRotation()), getPaint());

        int x, y, r, height;
        for(Integer key : engines.keys()){

           Engine e = engines.get(key);

            x = (int) rotatedLocations[key].getX();
            y = (int) rotatedLocations[key].getY();
            r = (int) (e.getWidth() * 100 * getScale() / 2);
            height = (int) (e.getLength() * 100 * getScale());
            e.setRotation(getRotation());
            //e.setBounds(new Bounds(x-r, x+r, y, y+height)); works but not optimal
            e.getBounds().setBounds(x-r, x+r, y, y+height);
            e.draw(canvas);
        }

    } // draw()

    /**
     * Overridden so each time the bounds are changed, the scale is changed to accommodate
     * @param bounds the new Bounds to give the Fuselage
     */
    @Override
    public void setBounds(Bounds bounds){
        super.setBounds(bounds);

        // get scaling factors
        float x = (float) (bounds.width() / width);
        float y = (float) (bounds.height() / height);
        // use the smallest one
        float scale = (x < y)? x : y;
        super.setScale(scale);
    }

    //===== STATIC METHODS
    public static Tuple<Integer> randomizeMassDistributions(){


        double inert = Utility.rand(MIN_INERT_PROPORTION, MAX_INERT_PROPORTION);
        double fuel = Utility.rand(0, 1 - inert);

        Tuple<Integer> result = new Tuple<>();
        result.add((int)(inert * 1000));
        result.add((int)(fuel * 1000));

        return result;
    }

    /**
     * Creates a list of allowable vector magnitudes for later Fuselage generation.
     * Protects against 1D Fuselage designs.
     * @return Tuple of valid Integers with Unique to later be used for form Fuselage shape.
     */
    public static Tuple<Integer> randomizedFuselageParameters() {

        Tuple<Integer> list = new Tuple<>();

        // number of vectors to randomize
        // minimum of 3 to avoid 2D/1D designs
        int num = Utility.rand(MIN_NUMBER_OF_FUSELAGE_VECTORS, MAX_NUMBER_OF_FUSELAGE_VECTORS);

        // randomize and add them to the list
        for(int i=0; i < num; i++)
            list.add(Utility.rand(1, MAX_FUSELAGE_VECTOR_LENGTH));

        return list;
    }

    public static Tuple<Integer> randomColor(){

        Tuple<Integer> color = new Tuple<>();

        switch(Utility.rand(0, 3)){
            case 0: color.add(Color.WHITE); break;
            case 1: color.add(Color.GRAY); break;
            case 2: color.add(Orthus.ORTHUS_BRONZE); break;
            case 3: color.add(Orthus.ORTHUS_SILVER); break;
        }

        return color;
    }

    //===== ACCESSORS
    public int engineCount(){ return engines.entries(); }
    public double getVolume(){ return volume; }
    public double getSurfaceArea() { return surfaceArea; }
    public double getFuelMass(){ return currentFuelMass; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }
    public double getDragCoefficient(){ return dragCoefficient; }

    public int getRelativeCenter(){ return relativeCenter; }
    public int getTrueCenter(){ return trueCenter; }

} // end Fuselage

