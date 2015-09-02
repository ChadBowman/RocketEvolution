package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;
import android.graphics.Path;

import net.orthus.rocketevolution.ui.Bounds;
import net.orthus.rocketevolution.ui.Graphic;
import net.orthus.rocketevolution.Physics;
import net.orthus.rocketevolution.Utility;
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
    private final float throatExitFactor = 0.382f;
    private final float expansionFactor = 0.75f;

    //=== INSTANCE VARIABLES
    private Fuel fuel;
    private VectorGroup vectorGroup;

    // fundamental ints which will be used in the chromosome
    private int throatRadius,   // throat radius in mm
            length,             // length/height in mm (distance from throat to exit) TODO instead make length dependant on exit, throat, and expan. factor
            temperature,        // chamber temp in K
            pressure;           // chamber pressure in Pa

    // variables used in calculations
    private int exitRadius;     // exhaust radius in mm

    private float throatArea,   // throat area in m^2
            exitArea,           // exit area in m^2
            chamberTemperature, // float version of temperature
            chamberPressure,    // float version of pressure
            massFlowRate,       // mass flow rate of engine (kg/s)
            exitVelocity,       // exit velocity of gas (m/s)
            exitPressure;       // exit pressure of gas (Pa)


    private float parabolaLength,
            parabolaXIntersect,
            parabolaYIntersect;

    private Vector fromCOM;     // distance/angle from center of mass


    //=== CONSTRUCTORS


    public Engine(Fuel fuel, Vector fromCOM){
        this.fuel = fuel;
        this.fromCOM = fromCOM;

        // CHROMOSOME INTEGER GENERATION
        // throat rad 50mm to 500mm
        throatRadius = Utility.rand(50, 1000);
        // length of exhaust bell from throat to end
        // min length is twice the length of exit arc
        // max length is 15 times the throat radius
        length = Utility.rand((int) (Math.sqrt(3) * throatExitFactor * throatRadius), 10 * throatRadius);
        temperature = Utility.rand(2500, 4000);
        pressure = Utility.rand(Fuel.MINIMUM_PRESSURE + 1, Fuel.MAXIMUM_PRESSURE - 1);

        // FLOATS FOR CALCULATION
        // exhaust radius is a product of length and throat radius
        exitRadius = calculateExhaustRadius();
        // store areas in m^2
        throatArea = (float) (Math.PI * Math.pow(throatRadius / 1000f, 2));
        exitArea = (float) (Math.PI * Math.pow(exitRadius / 1000f, 2));
        chamberPressure = (float) pressure;
        chamberTemperature = (float) temperature;

        // CALCULATE ENGINE CONSTANTS
        float specificHeat = fuel.specificHeatRatio(chamberPressure);
        float molarMass = fuel.molecularWeight(chamberPressure);
        float throatPressure = throatPressure(specificHeat);
        float throatTemp = throatTemperature(specificHeat);
        massFlowRate = massFlowRate(throatPressure, throatTemp, molarMass, specificHeat);
        float mach = (float) exitMach(specificHeat);
        exitPressure = exitPressure(throatTemp, mach, specificHeat);
        exitVelocity = exitVelocity(exitPressure, molarMass, specificHeat);

    } // end Constructor

    //=== PRIVATE METHODS

    private VectorGroup vectorRepresentation(){
        Vector[] vectors = new Vector[6];

        long controlY = (long)(length / 8f);
        long controlX = (long)((Math.pow(controlY, expansionFactor) + throatRadius) * 1.3);

        vectors[0] = new Vector((long) (throatRadius + 0.5), 0);
        vectors[1] = new Vector(controlX, -controlY);
        vectors[2] = new Vector(exitRadius, -length);
        vectors[3] = new Vector(-exitRadius, -length);
        vectors[4] = new Vector(-controlX, -controlY);
        vectors[5] = new Vector((long) -(throatRadius + 0.5), 0);

        return new VectorGroup(vectors);
    }

    /**
     * Uses too much geometry ^.^ to find the radius of the end of the engine
     * @return the exhaust (end of the engine) radius in mm.
     */
    private int calculateExhaustRadius(){

        // start with radius of circle
        float r = (float) (throatRadius * throatExitFactor);

        // the slope at the end of the arc is 3^(1/2)
        // intersectionX is the location on the parabola where the slope is 3^(1/2)
        parabolaXIntersect = (float) Math.pow(Math.sqrt(3) / expansionFactor, 1 / (1 - expansionFactor));
        parabolaYIntersect = (float) Math.pow(parabolaXIntersect, expansionFactor);

        // total length minus the x-value of arc plus the overlap
        parabolaLength = (float) (length - (Math.sqrt(3) / 2f) * r) + parabolaXIntersect;

        // get the height of the parabola at length, subtract overlap
        float exRad = (float) Math.pow(parabolaLength, expansionFactor) - parabolaYIntersect;

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
    private float throatPressure(float k){
        float p = 1 + ((k - 1) / 2);
        p = (float) Math.pow(p, -1 * (k / (k - 1))) * chamberPressure;
        return p;
    }

    /**
     * Loss of thermal energy causes temperature at throat to be less than in chamber
     * @param k specific heat ratio of gas
     * @return temperature of gas at throat (K)
     */
    private float throatTemperature(float k){
        float p = 1 + ((k - 1) / 2);
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
    private float massFlowRate(float pt, float tt, float m, float k){
        return (float) ((throatArea * pt) / Math.sqrt((Physics.R * 1000 * tt) / (m * k)));
    }

    /**
     * @param k specific heat ratio of fuel
     * @return Mach number of gas at exit
     */
    private double exitMach(float k){
        float ratio = exitArea / throatArea;
        float x = (2*k - 2) / (k + 1);
        float y = (k + 1) / 2;
        float z = (k - 1) / 2;

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
    private float exitPressure(float pt, float n, float k){
        float pe = (float) (((k - 1) / 2) * Math.pow(n, 2)) + 1;
        pe = (float) (pt / Math.pow(pe, k / (k - 1)));
        return pe;
    }

    /**
     * @param pe pressure at exit
     * @param m molar mass of fuel
     * @param k specific heat ratio of fuel
     * @return speed of gas at exit (m/s)
     */
    private float exitVelocity(float pe, float m, float k){
        float v = 1 - (float) Math.pow(pe / chamberPressure, (k - 1) / k);
        v *= Physics.R * 1000 * chamberTemperature / m;
        v *= (2 * k) / (k - 1);
        v = (float) Math.sqrt(v);

        return v;
    }

    /**
     * @param mfr mass flow rate of engine
     * @param ve velocity of gas at exit
     * @param pe pressure of gas at exit
     * @param pa ambient external pressure
     * @return engine thrust (N)
     */
    private float thrust(float mfr, float ve, float pe, float pa){
        return mfr * ve + ((pe - pa) * exitArea);
    }


    //=== PUBLIC METHODS

    /**
     * Calculates engine thrust at given altitude
     * @param pa ambient external pressure (Pa)
     * @return engine thrust (N)
     */
    public float thrust(float pa){
        return thrust(massFlowRate, exitVelocity, exitPressure, pa);
    }


    public float torque(float pa){

        // Engines directly below rocket contribute no rotational force
        long force = (long)(Math.sin(fromCOM.getAngle()) * thrust(pa));

        // tangential to the COM, away from the bottom
        float direction = (fromCOM.getAngle() <= Math.PI)?
                (float)(fromCOM.getAngle() - (Math.PI / 4)):
                (float)(fromCOM.getAngle() + (Math.PI / 4));

        Vector tangentialThrust = new Vector(force, direction);

        return fromCOM.cross(tangentialThrust);
    }

    public Path path(float theta){

        Vector[] v = vectorRepresentation().multiplyAll(scale).rotate(theta).getVectorArray();
        Path path = new Path();

        // start at center top
        path.moveTo(bounds.centerX(), bounds.getTop());

        path.lineTo(bounds.centerX() + v[0].getX(), bounds.getTop() - v[0].getY());

        path.quadTo(bounds.centerX() + v[1].getX(), bounds.getTop() - v[1].getY(),
                bounds.centerX() + v[2].getX(), bounds.getTop() - v[2].getY());

        path.lineTo(bounds.centerX() + v[3].getX(), bounds.getTop() - v[3].getY());
        path.quadTo(bounds.centerX() + v[4].getX(), bounds.getTop() - v[4].getY(),
                bounds.centerX() + v[5].getX(), bounds.getTop() - v[5].getY());
        path.lineTo(bounds.centerX(), bounds.getTop());


        return path;

    } // end path

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path(0), paint);
    }

    @Override
    public void setBounds(Bounds bounds) {
        super.setBounds(bounds);
        scale = bounds.width() / (exitRadius * 2f);
    }

    //=== ACCESSORS

    /**
     * @return engine length (height) in meters
     */
    public float getLength(){ return length / 1000f;}

    /**
     * @return engine width (diameter) in meters
     */
    public float getWidth(){ return exitRadius * 2 / 1000f; }

} // end Engine
