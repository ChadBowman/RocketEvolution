package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.ui.Animation;
import net.orthus.rocketevolution.ui.Bounds;
import net.orthus.rocketevolution.ui.Graphic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.utility.*;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.math.VarSum;
import net.orthus.rocketevolution.math.Variable;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.math.VectorGroup;

/**
 * Created by Chad on 8/3/2015.
 *
 * Every engine has the same bell-shape which is achieved by joining an arc made with a pi/6
 * section of a circle from pi3/4 to pi11/12 with the half-parabola of sqrt(x). The two shapes are
 * intersected where their slopes are the same (x=1/12 for parabola).
 */
public class Engine extends Graphic {

    //=== CONSTANTS
    public static final int MIN_THROAT_RADIUS = 50;
    public static final int MAX_THROAT_RADIUS = 200;
    public static final double MERLIN_ID_SL_THRUST = 756e3;

    // the radius of the throat exit arc is this factor times the throat radius
    private static final double THROAT_EXIT_FACTOR = 0.382;
    private static final double EXPANSION_FACTOR = 0.75;
    private static final int MAX_LENGTH_FACTOR = 5; //10;

    //=== INSTANCE VARIABLES

    private Chromosome chromosome;
    private Vector fromCOM;     // distance/angle from center of mass
    private VectorGroup vectorRepresentation;
    private Animation exhaust;

    private double length,
            exitRadius,  // exhaust radius in mm
            exitArea,           // exit area in m^2
            massFlowRate,       // mass flow rate of engine (kg/s)
            exitVelocity,       // exit velocity of gas (m/s)
            exitPressure;       // exit pressure of gas (Pa)

    private Path path;

    //===== CONSTRUCTORS

    /**
     * Creates an Engine with randomized attributes.
     * @param fromCOM location of Engine on Rocket from Rocket's center of mass.
     */
    public Engine(Chromosome chromosome, Vector fromCOM){

        this.chromosome = chromosome;
        this.fromCOM = fromCOM;

        double throatRadius = chromosome.engineThroatRadius();
        length = (double) chromosome.engineLength();

        Fuel fuel = chromosome.fuel();
        double chamberTemperature = fuel.getTemperature();
        double chamberPressure = fuel.getPressure();
        double specificHeat = fuel.getSpecificHeatRatio();
        double molarMass = fuel.getMolecularWeight();

        // doubleS FOR CALCULATION
        // exhaust radius is a product of length and throat radius
        exitRadius = calculateExhaustRadius(throatRadius, length);
        exitArea = Math.PI * Math.pow(exitRadius / 1000.0, 2);
        double throatArea = Math.PI * Math.pow(throatRadius / 1000.0, 2);
        vectorRepresentation = vectorRepresentation(throatRadius, length, exitRadius);

        // CALCULATE ENGINE CONSTANTS
        double throatPressure = throatPressure(specificHeat, chamberPressure);
        double throatTemp = throatTemperature(specificHeat, chamberTemperature);
        double mach =  exitMach(specificHeat, exitArea, throatArea);
        massFlowRate = massFlowRate(throatPressure, throatTemp, molarMass, specificHeat, throatArea);
        exitPressure = exitPressure(throatTemp, mach, specificHeat);
        exitVelocity = exitVelocity(exitPressure, molarMass, specificHeat, chamberTemperature);

        // Set Same Paint for all Engines
        Paint paint = new Paint();
        paint.setColor(Color.rgb(57, 57, 57));
        paint.setStyle(Paint.Style.FILL);
        setPaint(paint);

        path = new Path();

    } // end Constructor

    //====== PRIVATE METHODS


    private VectorGroup vectorRepresentation(double throatRadius, double length, double exitRadius){
        Vector[] vectors = new Vector[6];

        double controlY = length / 8.0;
        double controlX = Math.pow(controlY, EXPANSION_FACTOR) + throatRadius * 1.3;

        vectors[0] = new Vector(throatRadius, 0);
        vectors[1] = new Vector(controlX, -controlY);
        vectors[2] = new Vector(exitRadius, -length);
        vectors[3] = new Vector(-exitRadius, -length);
        vectors[4] = new Vector(-controlX, -controlY);
        vectors[5] = new Vector(-throatRadius, 0);

        return new VectorGroup(vectors);
    }

    /**
     * Uses geometry to find the radius of the end of the engine
     * @return the exhaust (end of the engine) radius in mm.
     */
    private double calculateExhaustRadius(double throatRadius, double length){

        // start with radius of circle
        double r = throatRadius * THROAT_EXIT_FACTOR;

        // the slope at the end of the arc is 3^(1/2)
        // intersectionX is the location on the parabola where the slope is 3^(1/2)
        double parabolaXIntersect = Math.pow(Math.sqrt(3) / EXPANSION_FACTOR, 1 / (1 - EXPANSION_FACTOR));
        double parabolaYIntersect = Math.pow(parabolaXIntersect, EXPANSION_FACTOR);

        // total length minus the x-value of arc plus the overlap
        double parabolaLength = (length - (Math.sqrt(3) / 2.0) * r) + parabolaXIntersect;

        // get the height of the parabola at length, subtract overlap
        double exRad = Math.pow(parabolaLength, EXPANSION_FACTOR) - parabolaYIntersect;

        // add the height of the arc and the throat radius
        exRad += (r / 2.0) + throatRadius;

        return exRad;
    }

    // CALCULATING THRUST ===============================================================

    // Throat pressure and temperature
    /**
     * Loss of thermal energy causes pressure at throat to be less than in chamber
     * @param k specific heat ratio
     * @return pressure of gas at throat (Pa)
     */
    private double throatPressure(double k, double cp){
        double p = 1 + ((k - 1) / 2);
        p = (double) Math.pow(p, -1 * (k / (k - 1))) * cp;
        return p;
    }

    /**
     * Loss of thermal energy causes temperature at throat to be less than in chamber
     * @param k specific heat ratio of gas
     * @return temperature of gas at throat (K)
     */
    private double throatTemperature(double k, double ct){
        double p = 1 + ((k - 1) / 2);
        return ct * (1 / p);
    }

    /**
     * How much mass (fuel) the engine moves.
     * @param pt throat pressure
     * @param tt throat temperature
     * @param m molar mass of fuel
     * @param k specific heat ratio of fuel
     * @param ta throat area
     * @return mass flow rate (kg/s)
     */
    private double massFlowRate(double pt, double tt, double m, double k, double ta){
        return (ta * pt) / Math.sqrt((Physics.R * 1000 * tt) / (m * k));
    }

    /**
     * @param k specific heat ratio of fuel
     * @return Mach number of gas at exit
     */
    private double exitMach(double k, double ea, double ta){
        double ratio = ea / ta;
        double x = (2*k - 2) / (k + 1);
        double y = (k + 1) / 2;
        double z = (k - 1) / 2;

        double co = Math.pow(ratio, x) * y;

        Variable v1 = new Variable(co, 'm', x);
        Variable v2 = new Variable(-1 * z, 'm', 2);
        Variable v3 = new Variable(-1);

        VarSum func = new VarSum(v1, v2, v3);
        Double[] solutions = func.solve(0, 10);

        // return only the last (largest) solution
        return solutions[solutions.length - 1];
    }

    /**
     * @param pt throat pressure
     * @param n mach exit number
     * @param k specific heat ratio of fuel
     * @return pressure at the exit (Pa)
     */
    private double exitPressure(double pt, double n, double k){
        double pe = (((k - 1) / 2) * Math.pow(n, 2)) + 1;
        pe = pt / Math.pow(pe, k / (k - 1));
        return pe;
    }

    /**
     * @param pe pressure at exit
     * @param m molar mass of fuel
     * @param k specific heat F of fuel
     * @return speed of gas at exit (m/s)
     */
    private double exitVelocity(double pe, double m, double k, double ct){
        double v = 1 - Math.pow(pe / ct, (k - 1) / k);
        v *= Physics.R * 1000 * ct / m;
        v *= (2 * k) / (k - 1);
        v = Math.sqrt(v);

        return v;
    }

    /**
     * @param mfr mass flow rate of engine
     * @param ve velocity of gas at exit
     * @param pe pressure of gas at exit
     * @param ea exit area
     * @param pa ambient external pressure
     * @return engine thrust (N)
     */
    private double thrust(double mfr, double ve, double pe, double ea, double pa){

        return mfr * ve + ((pe - pa) * ea);
    }


    //=== PUBLIC METHODS


    /**
     * Clones current instance
     * @return a new instance of Engine from this.
     */
    public Engine clone(){
        return new Engine(chromosome, fromCOM);
    }

    public double specificImpulse(){
        return exitVelocity / Earth.acceleration(Earth.RADIUS);
    }

    /**
     * Calculates engine thrust at given altitude
     * @param pa ambient external pressure (Pa)
     * @return engine thrust (N)
     */
    public Vector thrust(double pa, double throttle, float gimbal){

        gimbal = (float) (Math.PI / 2) + gimbal;
        double force = thrust(throttle * massFlowRate, exitVelocity, exitPressure, exitArea, pa);

        // TODO: 13-Apr-16 TEMP
        force *= 3;

        return new Vector(force, gimbal);
    }


    public double torque(double pa, double throttle, float gimbal){

        return fromCOM.cross(thrust(pa, throttle, gimbal));
    }

    public Path path(float theta){

        path.reset();
        Vector[] v = vectorRepresentation.multiplyAll(getScale()).rotate(theta).getVectorArray();
        Bounds bounds = getBounds();

        // start at center top
        path.moveTo(bounds.centerX(), bounds.getTop());

        path.lineTo(bounds.centerX() + (float) v[0].getX(),
                bounds.getTop() - (float) v[0].getY());

        path.quadTo(bounds.centerX() + (float) v[1].getX(),
                (bounds.getTop() - (float) v[1].getY()),
                (bounds.centerX() + (float) v[2].getX()),
                (bounds.getTop() - (float) v[2].getY()));

        path.lineTo(bounds.centerX() + (float) v[3].getX(),
                bounds.getTop() - (float) v[3].getY());

        path.quadTo(bounds.centerX() + (float) v[4].getX(),
                bounds.getTop() - (float) v[4].getY(),
                bounds.centerX() + (float) v[5].getX(),
                bounds.getTop() - (float) v[5].getY());

        path.lineTo(bounds.centerX(), bounds.getTop());


        return path;

    } // end path

    @Override
    public void update() {}

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path(getRotation()), getPaint());
        //exhaust.draw(canvas); TODO fix
    }

    @Override
    public void setBounds(Bounds bounds) {
        super.setBounds(bounds);

        float x = (float) (bounds.width() / exitRadius * 2);
        float y = (float) (bounds.height() / length);
        // use smallest
        float scale = (x < y)? x : y;
        super.setScale(scale);

        // set the bounds to the exhaust animation
        exhaust.setScale(scale);
        exhaust.setBounds(new Bounds(bounds.getLeft(), 0, bounds.getBottom(), 0));
    }

    //===== STATIC METHODS

    public static int minimumLength(int throatRadius){
        // min length is twice the length of the exit arc
        return (int) (Math.sqrt(3.0) * THROAT_EXIT_FACTOR * throatRadius);
    }

    public static int maximumLength(int throatRadius){
        return MAX_LENGTH_FACTOR * throatRadius;
    }

    public static Tuple<Integer> randomizedEngineParameters(){

        // 50mm to 1m
        Integer throatRadius = Utility.rand(MIN_THROAT_RADIUS, MAX_THROAT_RADIUS);

        // length of exhaust bell from throat to end
        Integer length = Utility.rand(minimumLength(throatRadius), maximumLength(throatRadius));

        Tuple<Integer> t = new Tuple<>();
        t.add(throatRadius);
        t.add(length);

        return t;

    } // randomizedEngineParameters()

    //=== ACCESSORS

    public void setExhaust(Animation a){ exhaust = a; }
    /**
     * Replaces the reference from the Rocket's center of mass Vector
     * @param v Vector of Engine placement from Rocket's center of mass.
     */
    public void setFromCOM(Vector v){ this.fromCOM = v; }

    public double getWidth(){ return exitRadius / 500.0; }

    public double getLength(){ return length / 1000.0; }

    public double getMassFlowRate(){
        return massFlowRate;
    }


} // end Engine
