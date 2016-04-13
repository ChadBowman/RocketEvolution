package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.evolution.Chromosome;
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

    //TODO units?
    public static final int MAX_FUSELAGE_VECTOR_LENGTH = 500;
    private static final int MAX_NUMBER_OF_FUSELAGE_VECTORS = 10;

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
                    surfaceArea;

    private int relativeCenter, trueCenter;

    private Hash<Integer, Engine> engines;
    private Vector[] rotatedLocations;

    private Fuel fuel;
    private Chromosome chromosome;
    private Path path;


    //=== CONSTRUCTORS

    public Fuselage(Chromosome chromosome){

        this.chromosome = chromosome;
        fuel = chromosome.fuel();
        Tuple<Integer> magnitudes = chromosome.getFuselage();

        // List which will eventually make up the VectorGroup to return
        ArrayList<Vector> vectorList = new ArrayList<Vector>();

        // spoke is the angle to increment for each Vector so the angle between them is the same
        // minus 1 since two vectors will be vertical
        float spoke = (float) Math.PI / (magnitudes.size() - 1);

        // angle which points straight up
        float up = (float) Math.PI / 2f;

        // generate the vectors which will form the shape
        for(int i=0; i < magnitudes.size(); i++)
            vectorList.add( new Vector( magnitudes.get(i), up + (spoke * i) ) );

        shape = new VectorGroup(mirrorVectors(vectorList));

        // get the array list
        ArrayList<Vector> vectors = shape.getVectorList();
        // grab the first half, plus vertical vectors
        ArrayList<Vector> half = new Utility<Vector>().sub(vectors, 0, vectors.size() / 2);

        horizontalHalf = new VectorGroup(half).rotateCW();

        centroid = horizontalHalf.centroid3D();
        width = width();
        height = height();
        volume = -horizontalHalf.volume() / Launchpad.MILLION;
        surfaceArea = (long) -horizontalHalf.surfaceArea();

        centeredAtCOM = shape.reCenter(new Vector(0, centroid));
        engines = spawnEngines(centeredAtCOM);

        double inert = chromosome.inertProportion(),
                fuel = chromosome.fuelProportion();
        dryMass = calculateDryMass(chromosome.payloadProportion(), inert);
        initialFuelMass = calculateInitialFuelMass(fuel);
        currentFuelMass = initialFuelMass;

        // Graphic elements
        setPaint(new Paint());
        path = new Path();

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
     * @return symmetric, complete shape of rocket body
     */
    private ArrayList<Vector> mirrorVectors(ArrayList<Vector> vectors) {

        // decrement through vectors, skipping the vertical ones. Start with last one
        // to continue the logical flow around the circle.
        // Vectors are copied to array first so ArrayList size doesn't change
        ArrayList<Vector> holder = new ArrayList<>();
        for (int i = vectors.size() - 2; i > 0; i--)
            holder.add(new Vector(vectors.get(i).getX() * -1, vectors.get(i).getY()));

        // add held vectors to permanent list
        for(int i=0; i < holder.size(); i++)
            vectors.add(holder.get(i));

        return vectors;
    }

    private double width(){

        ArrayList<Vector> vectors = shape.getVectorList();

        // find the longest x-value
        double value = 0;
        for(int i=0; i < vectors.size(); i++)
            value = (vectors.get(i).getX() > value)?
                    vectors.get(i).getX() : value;

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

        for(int i=0; i < engines.length; i++)
            total.add_(engines[i].thrust(pa, throttle[i], gimbal[i]));


        return total;
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
        step(double pa, double dt, double[] throttle, float[] gimbal){

        Vector acc;     // instantaneous acceleration on rocket due to thrust
        double angAcc;  // instantaneous angular acceleration

        if(currentFuelMass > 0) {
            acc = netThrust(pa, throttle, gimbal).multiply(1.0 / currentMass());
            angAcc = netTorque(pa, throttle, gimbal) / currentInertia();

            // burn fuel at current rate
            burnFuel(dt, throttle);

        }else{
            acc = new Vector();
            angAcc = 0;
        }

        return new Triple<>(acc, angAcc, currentFuelProportion());
    }

    //=== PUBLIC METHODS

    public double mass(){ return currentMass(); }

    public void setEnginePaint(Paint paint){

        for(Engine e : engines.values())
            e.setPaint(paint);
    }

    public Path path(float theta){

        float scale = getScale();
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
        int xAxis = (int) (getBounds().getBottom() - (scale * -smallestY.getY()));

        //TODO temp variables for testing
        relativeCenter = (int) (xAxis - (scale * centroid));
        trueCenter = xAxis;

        //Array to save locations
        rotatedLocations = new Vector[vectors.size()];

        // reset path
        path.reset();

        // start at top
        int x = (int) (yAxis + (vectors.get(0).getX() * scale));
        int y = (int) (xAxis - (vectors.get(0).getY() * scale));
        path.moveTo(x, y);
        rotatedLocations[0] = new Vector((double) x, (double) y);

        // move through list
        for(int i=1; i< vectors.size(); i++){
            x = (int) (yAxis + (vectors.get(i).getX() * scale));
            y = (int) (xAxis - (vectors.get(i).getY() * scale));
            path.lineTo(x, y);
            rotatedLocations[i] = new Vector((double) x, (double) y);
        }

        // close path and return
        path.lineTo(yAxis + ((float) vectors.get(0).getX() * scale),
                xAxis - ((float) vectors.get(0).getY() * scale));

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
            e.setBounds(new Bounds(x - r, x + r, y, y + height));
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

    /**
     * Creates a list of allowable vector magnitudes for later Fuselage generation.
     * Protects against 1D Fuselage designs.
     * @return Tuple of valid Integers with Unique to later be used for form Fuselage shape.
     */
    public static Tuple<Integer> randomizedFuselageParameters() {

        Tuple<Integer> list = new Tuple<>();

        // number of vectors to randomize
        // minimum of 3 to avoid 2D/1D designs
        int num = Utility.rand(3, MAX_NUMBER_OF_FUSELAGE_VECTORS);

        // randomize and add them to the list
        for(int i=0; i < num; i++)
            list.add(Utility.rand(1, MAX_FUSELAGE_VECTOR_LENGTH));

        return list;
    }

    //===== ACCESSORS
    public int engineCount(){ return engines.entries(); }
    public double getVolume(){ return volume; }
    public double getSurfaceArea() { return surfaceArea; }
    public double getFuelMass(){ return currentFuelMass; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }

    public int getRelativeCenter(){ return relativeCenter; }
    public int getTrueCenter(){ return trueCenter; }

} // end Fuselage

