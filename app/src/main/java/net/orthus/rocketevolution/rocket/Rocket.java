package net.orthus.rocketevolution.rocket;

import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;
import net.orthus.rocketevolution.environment.Kinematic;
import net.orthus.rocketevolution.environment.Kinetic;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;

/**
 * Created by Chad on 7/23/2015.
 */
public class Rocket implements Kinetic{

    // proportion of Rocket volume dedicated to support systems
    private static final double MIN_INERT_PROPORTION = 0.1; //TODO check
    private static final double MAX_INERT_PROPORTION = 0.4; //TODO check if this is realistic

    private Chromosome chromosome;
    private Fuselage body;
    private Kinematic kinematics;

    // below temp
    private Vector g;
    private double pos, vel, acc;

    public Rocket(Chromosome chromosome){
        body = new Fuselage(chromosome);
    }

    public Rocket(){
        chromosome = new Chromosome();
        this.body = new Fuselage(chromosome); // base unit in CMs
        g = new Vector(0, -10);
        this.kinematics = new Kinematic();
    }

    public Kinematic getKinematics(){
        return kinematics;
    }

    public void update(long dt){

        double s = dt * 1e-9;
        Vector grav = new Vector(9.81, (float)-Math.PI/2);

        // instant acceleration
        if(body.getFuelMass() > 0) {
            double pa = Physics.atmosphericPressure(kinematics.getPosition().getY());
            kinematics.setAcceleration(body.acceleration(pa).add(grav));
            kinematics.setRotAcc(1);
            Utility.p("Acc: %s", kinematics.getAcceleration());
            body.burnFuel(s);
        }else
            kinematics.setAcceleration(grav);

        if(kinematics.getPosition().getY() <= 0 && kinematics.getAcceleration().getY() <= 0) {
            kinematics.setVelocity(new Vector());
        }else {
            kinematics.setVelocity(kinematics.getVelocity().add(kinematics.getAcceleration().multiply(s)));
            Utility.p("Vel: %s", kinematics.getVelocity());
            //kinematics.getVelocity().setY(kinematics.getVelocity().getY()
              //      + (kinematics.getAcceleration().getY() * s));
            kinematics.setRotVel(kinematics.getRotVel() + (kinematics.getRotAcc() * s));
        }
        kinematics.setPosition(kinematics.getPosition().add(kinematics.getVelocity().multiply(s)));
        Utility.p("Pos: %s", kinematics.getPosition());
        //kinematics.getPosition().setY(
          //      kinematics.getPosition().getY() + (kinematics.getVelocity().getY() * s));
        kinematics.setRotPos(kinematics.getRotPos() + (kinematics.getRotVel() * s));
        body.setRotation((float) kinematics.getRotPos());

    } // update()

    public double volume(){
        return body.getVolume();
    }

    public double surfaceArea(){ return body.getSurfaceArea() / 10000; }
    public double height(){ return body.getHeight() / 100; }
    public double width(){ return body.getWidth() / 100; }
    public long mass(){ return (long) (body.getMass() + 0.5); }
    public String speed(){ return String.format("Speed: %.0f m/s", kinematics.getVelocity().getY()); }
    public String acc(){ return String.format("Acceleration: %.0f m/s/s", kinematics.getAcceleration().getY()); }
    public String alt(){ return String.format("Altitude: %.0f m", kinematics.getPosition().getY()); }
    public double gal(){ return kinematics.getPosition().getY(); }
    public String anAcc(){ return String.format("Angular Acc.: %.02f", kinematics.getRotAcc());}

    public Fuselage getBody(){ return body; }

    //===== STATIC METHODS

    public static Tuple<Integer> randomizeMassDistributions(){

        double inert = Utility.rand(MIN_INERT_PROPORTION, MAX_INERT_PROPORTION);
        double fuel = Utility.rand(0, 1 - inert);

        return new Tuple<Integer>((int)(inert * 1000), (int)(fuel * 1000));
    }

} // end Rocket
