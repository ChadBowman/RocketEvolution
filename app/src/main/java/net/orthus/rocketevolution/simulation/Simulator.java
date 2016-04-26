package net.orthus.rocketevolution.simulation;

import android.provider.Contacts;

import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.rocket.Guidance;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitnesses.Altitude;
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
        //k.setRotPos(Math.PI / 2);

        // initial values
        Triple<Vector, Double, Double> step = rocket.getFuselage().step(
                Earth.pressure(k.getPosition()),
                0,
                guide.throttle(guide.noThrottle(), time),
                guide.gimbal(guide.noGimbal(), time));

        k.setAcceleration(step.first);
        k.setRotAcc(step.second);
        double fuelProportion = step.third;
        // no velocity at T=0

        // list to stack history
        ArrayList<Frame> history = new ArrayList<>();

        Vector gravity, drag, pos, acc;
        double pressure, density;

        Vector maxPos = new Vector();
        Vector maxVel = new Vector();
        Vector maxAcc = new Vector();

        // update system until finished or RUD
        while( time < duration && !rud ){

            //Utility.p("Rot:%.0f%s", Utility.radianToDegree(k.getRotPos()), Utility.DEGREE);
            // subtract earth radius for history
            pos = k.getPosition().newMagnitude(k.getPosition().getMagnitude() - Earth.RADIUS);
            // add frame to list
            Frame f = new Frame(
                    pos,
                    k.getVelocity(),
                    k.getAcceleration(),
                    k.getRotPos(),
                    fuelProportion);
            //Utility.p("%s|%s", rocket.getId().toString(), f.toString());
            history.add(f);

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
                    guide.throttle(guide.noThrottle(), time),
                    guide.gimbal(guide.noGimbal(), time));

            fuelProportion = step.third;

            // Set Kinematics
            k.setAcceleration(step.first.add(gravity).add(drag)); //newAngle(k.getRotPos()).add(GRAV));
            k.setVelocity(k.getVelocity().add(k.getAcceleration().multiply(dt)));
            //k.getVelocity().add_(k.getAcceleration().multiply(dt)); (no idea why this doesn't work)
            k.getPosition().add_(k.getVelocity().multiply(dt));

/*            Utility.p("[%.2f] V:%s A:%s, R:%.2f%s",
                    time,
                    k.getVelocity().toString(),
                    k.getAcceleration().toString(),
                    k.getRotPos(),
                    Utility.DEGREE);*/

            k.setRotAcc(step.second);
            k.setRotVel(k.getRotVel() + (k.getRotAcc() * dt));
            k.setRotPos(k.getRotPos() + (k.getRotVel() * dt));

            // update time
            time += dt;

            // check for crash
            if(k.getPosition().getMagnitude() < Earth.RADIUS) {
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
        sim.fitness = new Altitude(maxPos.getMagnitude());

        return sim;

    } // end run()

} // Simulator
