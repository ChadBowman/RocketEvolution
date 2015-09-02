package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.ui.Bounds;
import net.orthus.rocketevolution.ui.Graphic;
import net.orthus.rocketevolution.Utility;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.math.VectorGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Chad on 7/23/2015.
 *
 * Rocket bodies are made by connecting the ends of n vectors together. Two vectors are locked
 * vertically, and the rest are distributed radially even. The minimum number of vectors a body can
 * have is two. The body is mirrored across the y-axis.
 */
public class RocketBody extends Graphic {

    //=== CONSTANTS

    // angle to start Vector generation
    private final float initialAngle = (float) Math.PI / 2;
    // proportion of Rocket volume dedicated to support systems
    private final float proportionSupport = 0.1f; //TODO check if this is realistic
    // average density of all support systems
    private final float densitySupport = 20f; //TODO set a fixed density for support systems/materials
    // average density of all payloads (needs to be fixed for proper competition)
    private final float densityPayload = 20f; //TODO set a fixed density for all payloads

    //=== INSTANCE VARIABLES

    // list of vectors which compromise the shape
    private VectorGroup vectors;
    private VectorGroup horizontalHalf; // TODO remove this if the only time we use it is to calculate stats like volume, SA
    private VectorGroup centeredAtCOM;

    private double centroid,
                    inertia,
                    mass;

    private long width,
                 height,
                 volume,
                 surfaceArea;

    private int relativeCenter, trueCenter;

    private HashMap<Integer, Engine> engines;
    private Vector[] rotatedLocations;

    private Fuel fuel;

    // utilities
    private Random rand = new Random();

    //=== CONSTRUCTORS

    /**
     * When randomly generating body.
     * @param numberOfVectors Number of vectors for one side, not including the two vertical vectors.
     *                        9 here will yield a total of 20 when shape is complete.
     *                        Values less than 1 are replaced with 1.
     * @param maxMagnitude An upward bound for the length of any of the randomly, generated vectors.
     */
    public RocketBody(int numberOfVectors, int maxMagnitude, Fuel fuel){

        this.fuel = fuel;

        // make sure at least 3 vectors are present, and no negatives
        // this ensures the rocket will have some area (as long as magnitudes are checked)
        numberOfVectors = (numberOfVectors < 1)? 1 : numberOfVectors;

        // create list
        ArrayList<Vector> vectorList = new ArrayList<Vector>();

        // spoke is the angle to increment by for each vector
        // plus 1 because two vectors will be vertical
        float spoke = (float) Math.PI / (numberOfVectors + 1);

        // generate vectors which will form shape
        for(int i=0; i < numberOfVectors + 2; i++)
            vectorList.add(new Vector(rand.nextInt(maxMagnitude), initialAngle + (spoke * i)));

        // avoid a 1D rocket
        while(vectorList.size() == 3 && vectorList.get(1).isZero() )
            vectorList.get(1).newMagnitude_(rand.nextInt(maxMagnitude));

        //TODO there's a very rare chance that 1D Rockets can still be generated and might crash the app when calculations are made

        // generate the rest of the vectors, package in VectorGroup
        this.vectors = new VectorGroup(mirrorVectors(vectorList));

        gatherStats();
        this.centeredAtCOM = vectors.reCenter(new Vector(0, (int) (centroid + 0.5)));
        this.engines = spawnEngines(centeredAtCOM, fuel);

        float proportionFuel = Utility.rand(0.1f, 0.9f);
        float proportionPayload = 1f - proportionFuel - proportionSupport;
        this.mass = calculateInitialMass(fuel, proportionFuel, proportionPayload);

    } // end constructor

    //=== PRIVATE METHODS

    private void gatherStats(){

        // get the array list
        ArrayList<Vector> vectors = this.vectors.getVectorList();
        // grab the first half, plus vertical vectors
        ArrayList<Vector> half = new Utility<Vector>().sub(vectors, 0, vectors.size()/2);

        horizontalHalf = new VectorGroup(half).rotateCW();

        centroid = horizontalHalf.centroid3D();
        width = width();
        height = height();
        volume = (long) -horizontalHalf.volume();
        surfaceArea = (long) -horizontalHalf.surfaceArea();
        inertia = calculateInertia(); //TODO multiply this result by the rocket mass
    }

    private HashMap<Integer, Engine> spawnEngines(VectorGroup fromCOM, Fuel fuel){

        HashMap<Integer, Engine> hash = new HashMap<Integer, Engine>();
        Vector[] vs = fromCOM.getVectorArray();

        long x;
        boolean flag = true;
        // place Engine on the end of each Vector which doesn't have part of the rocket below it
        for(int i=0; i <= vs.length/2; i++) {
            x = vs[i].getX();

            // check "below" for rocket
            for(int j=i+1; j < vs.length/2; j++)
                if(vs[j].getX() < x)
                    flag = false;


            if(flag) {
                hash.put(i, new Engine(fuel, vs[i]));
                //if not the top or bottom Vector, add Engine to other side
                if((i != 0) && (i != vs.length/2))
                    hash.put(vs.length - i, new Engine(fuel, vs[vs.length - i]));
            }

            flag = true;
        }

        return hash;

    } // end spawnEngines


    private double calculateInitialMass(Fuel fuel, float proportionFuel, float proportionPayload){

        double mass = 0;

        //TODO check mass calcs
        mass += volume * proportionFuel * fuel.getDensity();
        mass += volume * proportionSupport * densitySupport;
        mass += volume * proportionPayload * densityPayload;

        return mass;
    }

    /**
     * Estimates rotational inertia by using a cylinder with the same length (height) and a radius
     * that makes the cylinder have the same volume as the rocket.
     * TODO calculate exact rotational inertia using calculus.
     * @return rotational inertia divided by rocket mass.
     */
    private double calculateInertia(){

        // radius of cylinder squared
        long r = (long) ((volume / (Math.PI * height)) + 0.5);
        // rotational inertia of cylinder about central diameter (orthogonal to circle).
        long i = (long) ((0.25 * r) + ((1/12.0) * Math.pow(height, 2)) + 0.5);

        return i * mass;
    }

    /**
     * Generates the remaining Vectors so the RocketBody shape is symmetric
     * @param vectors non-symmetric half-shape of body
     * @return symmetric, complete shape of rocket body
     */
    private ArrayList<Vector> mirrorVectors(ArrayList<Vector> vectors) {

        // decrement through vectors, skipping the vertical ones. Start with last one
        // to continue the logical flow around the circle.
        // Vectors are copied to array first so ArrayList size doesn't change
        ArrayList<Vector> holder = new ArrayList<Vector>();
        for (int i = vectors.size() - 2; i > 0; i--)
            holder.add(new Vector(vectors.get(i).getX() * -1, vectors.get(i).getY()));

        // add held vectors to permanent list
        for(int i=0; i < holder.size(); i++)
            vectors.add(holder.get(i));

        return vectors;
    }

    private long width(){

        ArrayList<Vector> vectors = this.vectors.getVectorList();

        // find the longest x-value
        long value = 0;
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
    private long height(){

        ArrayList<Vector> vectors = this.vectors.getVectorList();

        // find the largest and smallest y values
        long largest = 0;
        long smallest = 0;
        for(int i=0; i < vectors.size(); i++) {
            largest  = (vectors.get(i).getY() > largest) ? vectors.get(i).getY() : largest;
            smallest = (vectors.get(i).getY() < smallest)? vectors.get(i).getY() : smallest;
        }

        // return sum
        return (smallest * -1) + largest;
    }

    //=== PUBLIC METHODS

    public float netTorque(float pa){
        float total = 0;

        for(Engine engine : engines.values())
            total += engine.torque(pa);

        return total;
    }

    public float thrust(float pa){
        float total = 0;

        for(Engine engine : engines.values())
            total += engine.thrust(pa);

        return total;
    }

    //=== PUBLIC METHODS


    public Path path(float theta){

        // rotate the negation of the center vector
        Vector center = new Vector(0, (int) -(centroid + 0.5));
        // rotate about center of mass, then return to original center for shape
        ArrayList<Vector> vectors = centeredAtCOM.rotate(theta)
                .reCenter(center).getVectorList();


        // useful measurements
        int yAxis = bounds.centerX();

        // find new X axis
        // grab the vector with smallest Y value
        Vector smallestY = vectors.get(0);
        for(int i=0; i < vectors.size(); i++)
            smallestY = (vectors.get(i).getY() < smallestY.getY()) ? vectors.get(i) : smallestY;

        // place smallestY vector on bottom of boundary and follow up to reveal new X axis
        int xAxis = (int) (bounds.getBottom() - (scale * -smallestY.getY()));

        //TODO temp variables for testing
        relativeCenter = (int) (xAxis - (scale * centroid));
        trueCenter = (int) xAxis;

        //Array to save locations
        rotatedLocations = new Vector[vectors.size()];

        // create path
        Path path = new Path();

        // start at top
        int x = (int) (yAxis + (vectors.get(0).getX() * scale));
        int y = (int) (xAxis - (vectors.get(0).getY() * scale));
        path.moveTo(x, y);
        rotatedLocations[0] = new Vector(x, y);

        // move through list
        for(int i=1; i< vectors.size(); i++){
            x = (int) (yAxis + (vectors.get(i).getX() * scale));
            y = (int) (xAxis - (vectors.get(i).getY() * scale));
            path.lineTo(x, y);
            rotatedLocations[i] = new Vector(x, y);
        }

        // close path and return
        path.lineTo(yAxis + (vectors.get(0).getX() * scale),
                xAxis - (vectors.get(0).getY() * scale));

        return path;

    } // end path

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {

        canvas.drawPath(path(0), paint);
        Engine e;
        int x, y, rad, height;
        for(Integer key : engines.keySet()) {
            e = engines.get(key);
            x = (int) rotatedLocations[key].getX();
            y = (int) rotatedLocations[key].getY();
            rad = (int)(e.getWidth() * 100 * scale / 2);
            height = (int)(e.getLength() * 100 * scale);

            e.setBounds(new Bounds(x - rad, x + rad, y, y + height));
            e.draw(canvas);
        }
    }

    /**
     * Overridden so each time the bounds are changed, the scale is changed to accommodate
     * @param bounds the new Bounds to give the RocketBody
     */
    @Override
    public void setBounds(Bounds bounds){
        super.setBounds(bounds);

        // get scaling factors
        float xFactor = bounds.width() / (width * 1f);
        float yFactor = bounds.height() / (height * 1f);

        // use the smallest one
        scale = (xFactor < yFactor)? xFactor : yFactor;
    }

    @Override
    public void setPaint(Paint paint) {
        super.setPaint(paint);

        Paint paint1 = new Paint();
        paint1.setColor(Color.RED);
        paint1.setStyle(Paint.Style.FILL);
        for(Engine e : engines.values()) {
            e.setPaint(paint1);
        }
    }

    //=== ACCESSORS
    public long getWidth(){ return width; }
    public long getHeight(){ return height; }
    public long getVolume(){ return volume; }
    public long getSurfaceArea() { return surfaceArea; }
    public double getMass(){ return mass; }

    public int getRelativeCenter(){ return relativeCenter; }
    public int getTrueCenter(){ return trueCenter; }

} // end RocketBody
