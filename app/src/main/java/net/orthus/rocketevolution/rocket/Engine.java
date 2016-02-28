package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;
import android.graphics.Path;

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
public class Engine extends Graphic{

    //=== CONSTANTS

    // the radius of the throat exit arc is this factor times the throat radius
    private static final double THROAT_EXIT_FACTOR = 0.382f;
    private static final double EXPANSION_FACTOR = 0.75f;

    //=== INSTANCE VARIABLES
    private Fuel fuel;
    private VectorGroup vectorGroup;

    // fundamental ints which will be used in the chromosome
    private int throatRadius,   // throat radius in mm
            length,             // length/height in mm (distance from throat to exit)
            exitRadius;     // exhaust radius in mm

    private double throatArea,   // throat area in m^2
            exitArea,           // exit area in m^2
            chamberTemperature, // double version of temperature
            chamberPressure,    // double version of pressure
            massFlowRate,       // mass flow rate of engine (kg/s)
            exitVelocity,       // exit velocity of gas (m/s)
            exitPressure,       // exit pressure of gas (Pa)
            temperature,        // chamber temp (K)
            pressure;           // chamber pressure (Pa)


    private Vector fromCOM;     // distance/angle from center of mass

    private Chromosome chromosome;

    private double throttle; // TODO To be replaced with function


    //=== CONSTRUCTORS

    /**
     * Creates an Engine with randomized attributes.
     * @param fromCOM location of Engine on Rocket from Rocket's center of mass.
     */
    public Engine(Chromosome chromosome, Vector fromCOM){
        this.chromosome = chromosome;
        throatRadius = chromosome.getEngineThroatRadius();
        Fuel fuel = chromosome.getFuel();
        length = chromosome.getEngineLength();
        temperature = fuel.getTemperature();
        pressure = fuel.getPressure();
        this.fromCOM = fromCOM;

        // Finish calculations
        calculateEngineConstants(fuel.getSpecificHeatRatio(), fuel.getMolecularWeight());

    } // end Constructor

    //=== PRIVATE METHODS

    /**
     * Calculates engine's throat area, exit area; chamber pressure, temperature,
     * mass flow rate, exit pressure, and exit velocity. All of these constants are later
     * used to calculate engine thrust.
     */
    private void calculateEngineConstants(double specificHeat, double molarMass){

        // Throttle
        //TODO generate function instead
        throttle = 1.0f;

        // doubleS FOR CALCULATION
        // exhaust radius is a product of length and throat radius
        exitRadius = calculateExhaustRadius();
        // store areas in m^2
        throatArea = (double) (Math.PI * Math.pow(throatRadius / 1000f, 2));
        exitArea = (double) (Math.PI * Math.pow(exitRadius / 1000f, 2));
        chamberPressure = (double) pressure;
        chamberTemperature = (double) temperature;

        // CALCULATE ENGINE CONSTANTS
        double throatPressure = throatPressure(specificHeat);
        double throatTemp = throatTemperature(specificHeat);
        massFlowRate = massFlowRate(throatPressure, throatTemp, molarMass, specificHeat);
        double mach = (double) exitMach(specificHeat);
        exitPressure = exitPressure(throatTemp, mach, specificHeat);
        exitVelocity = exitVelocity(exitPressure, molarMass, specificHeat);

    }

    private VectorGroup vectorRepresentation(){
        Vector[] vectors = new Vector[6];

        double controlY = length / 8.0;
        double controlX = Math.pow(controlY, EXPANSION_FACTOR) + throatRadius * 1.3;

        double tr = (double) throatRadius;
        double er = (double) exitRadius;
        double le = (double) length;

        vectors[0] = new Vector(tr, 0);
        vectors[1] = new Vector(controlX, -controlY);
        vectors[2] = new Vector(er, -le);
        vectors[3] = new Vector(-er, -le);
        vectors[4] = new Vector(-controlX, -controlY);
        vectors[5] = new Vector(-tr, 0);

        return new VectorGroup(vectors);
    }

    /**
     * Uses too much geometry ^.^ to find the radius of the end of the engine
     * @return the exhaust (end of the engine) radius in mm.
     */
    private int calculateExhaustRadius(){

        // start with radius of circle
        double r = (double) (throatRadius * THROAT_EXIT_FACTOR);

        // the slope at the end of the arc is 3^(1/2)
        // intersectionX is the location on the parabola where the slope is 3^(1/2)
        double parabolaXIntersect = (double) Math.pow(Math.sqrt(3) / EXPANSION_FACTOR, 1 / (1 - EXPANSION_FACTOR));
        double parabolaYIntersect = (double) Math.pow(parabolaXIntersect, EXPANSION_FACTOR);

        // total length minus the x-value of arc plus the overlap
        double parabolaLength = (double) (length - (Math.sqrt(3) / 2f) * r) + parabolaXIntersect;

        // get the height of the parabola at length, subtract overlap
        double exRad = (double) Math.pow(parabolaLength, EXPANSION_FACTOR) - parabolaYIntersect;

        // add the height of the arc and the throat radius
        exRad += (r / 2f) + throatRadius;

        return (int) (exRad + 0.5f);
    }

    // CALCULATING THRUST ===============================================================

    // Throat pressure and temperature
    /**
     * Loss of thermal energy causes pressure at throat to be less than in chamber
     * @param k specific heat ratio
     * @return pressure of gas at throat (Pa)
     */
    private double throatPressure(double k){
        double p = 1 + ((k - 1) / 2);
        p = (double) Math.pow(p, -1 * (k / (k - 1))) * chamberPressure;
        return p;
    }

    /**
     * Loss of thermal energy causes temperature at throat to be less than in chamber
     * @param k specific heat ratio of gas
     * @return temperature of gas at throat (K)
     */
    private double throatTemperature(double k){
        double p = 1 + ((k - 1) / 2);
        return chamberTemperature * (1 / p);
    }

    /**
     * How much mass (fuel) the engine moves.
     * @param pt throat pressure
     * @param tt throat temperature
     * @param m molar mass of fuel
     * @param k specific heat ratio of fuel
     * @return mass flow rate (kg/s)
     */
    private double massFlowRate(double pt, double tt, double m, double k){
        return (double) ((throatArea * pt) / Math.sqrt((Physics.R * 1000 * tt) / (m * k)));
    }

    /**
     * @param k specific heat ratio of fuel
     * @return Mach number of gas at exit
     */
    private double exitMach(double k){
        double ratio = exitArea / throatArea;
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
        return solutions[solutions.length-1];
    }

    /**
     * @param pt throat pressure
     * @param n mach exit number
     * @param k specific heat ratio of fuel
     * @return pressure at the exit (Pa)
     */
    private double exitPressure(double pt, double n, double k){
        double pe = (double) (((k - 1) / 2) * Math.pow(n, 2)) + 1;
        pe = (double) (pt / Math.pow(pe, k / (k - 1)));
        return pe;
    }

    /**
     * @param pe pressure at exit
     * @param m molar mass of fuel
     * @param k specific heat F of fuel
     * @return speed of gas at exit (m/s)
     */
    private double exitVelocity(double pe, double m, double k){
        double v = 1 - (double) Math.pow(pe / chamberPressure, (k - 1) / k);
        v *= Physics.R * 1000 * chamberTemperature / m;
        v *= (2 * k) / (k - 1);
        v = (double) Math.sqrt(v);

        return v;
    }

    /**
     * @param mfr mass flow rate of engine
     * @param ve velocity of gas at exit
     * @param pe pressure of gas at exit
     * @param pa ambient external pressure
     * @return engine thrust (N)
     */
    private double thrust(double mfr, double ve, double pe, double pa){
        return mfr * ve + ((pe - pa) * exitArea);
    }


    //=== PUBLIC METHODS

    /**
     * Clones current instance
     * @return a new instance of Engine from this.
     */
    public Engine clone(){
        return new Engine(chromosome, fromCOM);
    }

    public double currentMassFlowRate(){
        return massFlowRate * throttle;
    }

    /**
     * Calculates engine thrust at given altitude
     * @param pa ambient external pressure (Pa)
     * @return engine thrust (N)
     */
    public double thrust(double pa){
        //TODO change when throttle function figured out
        return throttle * thrust(massFlowRate, exitVelocity, exitPressure, pa);
    }


    public double torque(double pa){

        // Engines directly below rocket contribute no rotational force
        double force = Math.sin(fromCOM.getAngle()) * thrust(pa);

        // tangential to the COM, away from the bottom
        double direction = (fromCOM.getAngle() <= Math.PI)?
                (double)(fromCOM.getAngle() - (Math.PI / 4)):
                (double)(fromCOM.getAngle() + (Math.PI / 4));

        Vector tangentialThrust = new Vector(force, direction);

        return fromCOM.cross(tangentialThrust);
    }

    public Path path(float theta){

        Vector[] v = vectorRepresentation().multiplyAll(scale).rotate(theta).getVectorArray();
        Path path = new Path();

        // start at center top
        path.moveTo(bounds.centerX(), bounds.getTop());

        path.lineTo(bounds.centerX() + (float) v[0].getX(), bounds.getTop() - (float) v[0].getY());

        path.quadTo((float) (bounds.centerX() + v[1].getX()), (float) (bounds.getTop() - v[1].getY()),
                (float) (bounds.centerX() + v[2].getX()), (float) (bounds.getTop() - v[2].getY()));


        path.lineTo(bounds.centerX() + (float) v[3].getX(), bounds.getTop() - (float) v[3].getY());

        path.quadTo(bounds.centerX() + (float) v[4].getX(), bounds.getTop() - (float) v[4].getY(),
                bounds.centerX() + (float) v[5].getX(), bounds.getTop() - (float) v[5].getY());

        path.lineTo(bounds.centerX(), bounds.getTop());


        return path;

    } // end path

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path(rotation), paint);
        //canvas.drawCircle(bounds.centerX(), bounds.getTop(), 10, paint);
    }

    @Override
    public void setBounds(Bounds bounds) {
        super.setBounds(bounds);
        scale = bounds.width() / (exitRadius * 2f);
    }

    //===== STATIC METHODS
    public static Tuple<Integer> randomizedEngineParameters(){

        // 50mm to 1m
        Integer throatRadius = Utility.rand(50, 1000);

        // length of exhaust bell from throat to end
        // min length is twice the length of the exit arc
        int min = (int) (Math.sqrt(3.0) * THROAT_EXIT_FACTOR * throatRadius);
        // max is 10 times the throat radius
        int max = 10 * throatRadius;

        Integer length = Utility.rand(min, max);

        return new Tuple<Integer>(throatRadius, length);

    } // randomizedEngineParameters()

    //=== ACCESSORS

    /**
     * Replaces the reference from the Rocket's center of mass Vector
     * @param v Vector of Engine placement from Rocket's center of mass.
     */
    public void setFromCOM(Vector v){ this.fromCOM = v; }

    //TODO remove when function figured out
    public void setThrottle(double t){ throttle = t; }

    /**
     * @return engine length (height) in meters
     */
    public double getLength(){ return length / 1000f;}

    /**
     * @return engine width (diameter) in meters
     */
    public double getWidth(){ return exitRadius * 2 / 1000f; }

} // end Engine
