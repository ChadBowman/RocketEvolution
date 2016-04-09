package net.orthus.rocketevolution.simulation;

import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.rocket.Guidance;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.utility.Triple;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;

/**
 * Created by Chad on 2/7/2016.
 */
public class Simulator {

    private Rocket rocket;

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
        //k.setPosition(new Vector(0, Earth.RADIUS));

        // initial step
        Triple<Vector, Double, Double> step = rocket.getFuselage().step(
                Earth.pressure(k.getPosition()),
                0,
                guide.throttle(guide.noThrottle(), time),
                guide.gimbal(guide.noGimbal(), time));

        k.setAcceleration(step.first.add(Earth.gravity(rocket.getFuselage().mass(), k.getPosition())));

        k.setRotAcc(step.second);

        double fuelProportion = step.third;


        // list to stack history
        ArrayList<Frame> history = new ArrayList<>();

        Vector gravity, drag, pos, acc;
        double pressure;

        Vector maxPos = new Vector();
        Vector maxVel = new Vector();
        Vector maxAcc = new Vector();

        // update system until finished or RUD
        while( time < duration && !rud ){

            // subtract earth radius for history
            pos = k.getPosition().newMagnitude(k.getPosition().getMagnitude() - Earth.RADIUS);

            // add frame to list
            history.add(new Frame(pos, k.getRotPos(), fuelProportion));

            // TAKE A STEP THROUGH SYSTEM
            // use previous position/velocity
            pressure = Earth.pressure(k.getPosition());
            gravity = Earth.gravity(rocket.getFuselage().mass(), k.getPosition());
            drag = new Vector(); // TODO: 13-Mar-16 implement

            step = rocket.getFuselage().step(
                    pressure,
                    dt,
                    guide.throttle(guide.noThrottle(), time),
                    guide.gimbal(guide.noGimbal(), time));

            fuelProportion = step.third;

            // Set Kinematics
            acc = new Vector(step.first.getMagnitude(), 0);//k.getRotPos());
            k.setAcceleration(acc);//.add(drag).add(gravity));
            k.addVelocity(k.getAcceleration(), dt);
            k.setPosition(k.getPosition().add(k.getVelocity().multiply(dt)));
          /*  if(fuelProportion > 0.01)
                Utility.p("[%.0f%%] A|%s V|%s P|%s", fuelProportion * 100,
                        k.getAcceleration().toString(),
                        k.getVelocity().toString(),
                        k.getPosition().toString());*/
          /*  Utility.p("Torque: %f", step.second);
            k.setRotAcc(step.second); // TODO: 18-Mar-16 add aerodynamic "inertia"
            k.setRotVel(k.getRotVel() + (k.getRotAcc() * dt));
            k.setRotPos(k.getRotPos() + (k.getRotVel() * dt));*/

            // update time
            time += dt;

            // check for crash
            if(k.getPosition().getMagnitude() < Earth.RADIUS * 0.5) {
                Utility.p("%f @ %.2f", k.getPosition().getMagnitude() / Earth.RADIUS, time);
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

        return sim;

    } // end run()

} // Simulator
