package net.orthus.rocketevolution.simulation;

import android.provider.Contacts;

import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.rocket.Fuselage;
import net.orthus.rocketevolution.rocket.Guidance;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitnesses.Altitude;
import net.orthus.rocketevolution.simulation.Fitnesses.Drag;
import net.orthus.rocketevolution.simulation.Fitnesses.DragCoefficient;
import net.orthus.rocketevolution.simulation.Fitnesses.SpecificImpulse;
import net.orthus.rocketevolution.utility.Triple;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;

/**
 * Created by Chad on 2/7/2016.
 */
public class Simulator {


    private Rocket rocket;
    public final Vector GRAV = new Vector(0, -9.81);
    public Simulator(Rocket rocket){
        this.rocket = rocket;
    }

    public Simulation run(int interval, int duration){
        /*
        1. when resources are avail, run this method for a long time (or when RUD or crash).
        2. Serialize and save the complete simulation
        3. Either fetch sections of this simulation from file at a time or fetch a less dense version for the GUI
         */

        // step time
        double dt = 1.0 / interval;
        double time = 0;        // current time
        boolean rud = false;    // rapid unscheduled disassembly indicator

        Guidance guide = new Guidance(rocket);
        Kinematic k = rocket.getKinematics();

        // INITIAL VALUES T = 0
        // set initial position to Earth Radius
        k.setPosition(new Vector(0, Earth.RADIUS));
        k.setVelocity(new Vector());
        k.setRotPos(0);

        // initial values
        Triple<Vector, Double, Double> step = rocket.getFuselage().step(
                Earth.pressure(k.getPosition()),
                0,
                guide.throttle(guide.noThrottle(), time),
                guide.gimbal(guide.noGimbal(), time),
                k.getRotPos());

        k.setAcceleration(step.first);
        double fuelProportion = step.third;
        // no velocity at T=0

        // list to stack history
        ArrayList<Frame> history = new ArrayList<>();

        Vector gravity, pos, acc, drag;
        double pressure, density;

        Vector maxPos = new Vector();
        Vector maxVel = new Vector();
        Vector maxAcc = new Vector();

        // update system until finished or RUD
        while( time < duration && !rud ){

            // subtract earth radius for history
            pos = k.getPosition().newMagnitude(k.getPosition().getMagnitude() - Earth.RADIUS);

            // add frame to list
            history.add(new Frame(
                    pos,
                    k.getVelocity(),
                    k.getAcceleration(),
                    k.getRotPos(),
                    fuelProportion));

            // TAKE A STEP THROUGH SYSTEM
            // use previous position/velocity
            pressure = Earth.pressure(k.getPosition());
            density = Earth.density(k.getPosition());
            gravity = Earth.gravitationalAcceleration(k.getPosition());
            drag = Physics.drag(
                    k.getVelocity(),
                    density,
                    rocket.getFuselage().getWidth() / 1000.0, // TODO: 24-Apr-16 figure out the units for this somewhere else
                    rocket.getFuselage().getDragCoefficient());

            step = rocket.getFuselage().step(
                    pressure,
                    dt,
                    guide.throttle(guide.testThrottle(), time),
                    guide.gimbal(guide.noGimbal(), time),
                    k.getRotPos());


            // Set Kinematics
            k.setAcceleration(step.first.add(gravity.add(drag)));
            k.setVelocity(k.getVelocity().add(k.getAcceleration().multiply(dt)));
            k.getPosition().add_(k.getVelocity().multiply(dt));
            //k.getVelocity().add_(k.getAcceleration().multiply(dt));
            // (no idea why this doesn't work when it works for position)


         /*   Utility.p("[%.2f] A:%.2f V:%.2f, P:%.2f",
                    time,
                    k.getRotAcc() / Math.PI,
                    k.getRotVel() / Math.PI,
                    k.getRotPos());
            Utility.p("|%s", f.toString());*/

            k.setRotAcc(step.second);
            k.setRotVel(k.getRotVel() + (k.getRotAcc() * dt));
            k.setRotPos(k.getRotPos() + (k.getRotVel() * dt));

            fuelProportion = step.third;

            // update time
            time += dt;

            // check for crash
            // subtract 100 so it looks like a crash on screen
            if(k.getPosition().getMagnitude() < Earth.RADIUS - 1000) {
                Utility.p("RUD @ %.2fs", time);
                rud = true;
            }

            // maximum values
            if(k.getPosition().compareTo(maxPos) > 0)
                maxPos = k.getPosition();

            if(k.getVelocity().compareTo(maxVel) > 0)
                maxVel = k.getVelocity();

            if(k.getAcceleration().compareTo(maxAcc) > 0)
                maxAcc = k.getAcceleration();

        } // end while

        Simulation sim = new Simulation(history, interval, rud);
        sim.setAltitude(new Altitude(maxPos.getY()));
        sim.setCoefficient(new DragCoefficient(rocket.getFuselage().getDragCoefficient()));
        sim.setDrag(new Drag(rocket.getFuselage().getDragCoefficient()
                + Utility.map(rocket.getFuselage().getWidth(), Fuselage.MAX_FUSELAGE_VECTOR_LENGTH * 2, 5)));
        sim.setSpecificImpulse(new SpecificImpulse(rocket.getFuselage().specificImpulse()));


        return sim;

    } // end run()

} // Simulator
