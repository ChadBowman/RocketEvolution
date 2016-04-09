package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.orthus.rocketevolution.Game;
import net.orthus.rocketevolution.GameThread;
import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.environment.Physics;
import net.orthus.rocketevolution.evolution.Chromosome;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.planets.Earth;
import net.orthus.rocketevolution.population.Generation;
import net.orthus.rocketevolution.population.Population;
import net.orthus.rocketevolution.rocket.Engine;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Frame;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.simulation.Simulator;
import net.orthus.rocketevolution.utility.Utility;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Chad on 7/23/2015.
 */
public class Launchpad extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 1440;
    public static final int HEIGHT = 2560;
    public static final int MILLION = 1000000;

    // Management elements
    private GameThread thread;
    private long currentTime, launchTime, rudTime;

    // Graphic elements
    private Background bg;
    private Animation expload, exhaust;
    private Bounds rocketBound;
    private Label popLabel, fitLabel;

    // Player elements
    private Population population;
    private int fitness;

    // Temp elements
    private Rocket currentRocket;
    private int workingIndex;
    private ArrayList<UUID> ids;
    private Frame previousFrame, currentFrame;
    private boolean rud;

    public Launchpad(Context context){
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        //KEEP AT TOP
        Fuel.fuels.add(Fuel.KEROSENE_PEROXIDE, new KerosenePeroxide("", 810, 8));

        float s = WIDTH / 15f;
        float w = (4 * WIDTH / 7f) - s;

        // Population label
        popLabel = new Label(
                "Population", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        popLabel.setBounds(new Bounds(s, s + w, HEIGHT / 20f, 2 * HEIGHT / 20f));

        fitLabel = new Label(
                "Optimise", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        fitLabel.setBounds(new Bounds(WIDTH - s - w, WIDTH - s, HEIGHT / 20f, 2 * HEIGHT / 20f));

        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(70);
        popLabel.setPaint(p);
        fitLabel.setPaint(p);

        // Create the location of rocket on screen
        int fithx = WIDTH / 3;
        int fithy = HEIGHT / 4;

        rocketBound = new Bounds(fithx, WIDTH - fithx, fithy, HEIGHT - fithy);

        // Create animations
        expload = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.explosion_large), 5, 5);
        expload.setBounds(rocketBound);
        expload.setRepeat(false);

        exhaust = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.exhaust1), 1, 3);

        // Create background
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.bgtest));

        // Create initial generation
        population = new Population();
        population.add(new Generation(3));
        population.get(0).runSims();


        // Set list of current gen's keys
        ids = population.get(0).getGeneration().keys();

        // Set default fitness selection
        fitness = Fitness.ALTITUDE;

        workingIndex = 0;
        rud = false;

        // // DON'T TOUCH BELOW! // //
        thread = new GameThread(getHolder(), this);
        // Safely start the game
        thread.setRunning(true);
        thread.start();

    } // surfaceCreate


    public void update(){

        // Update the game state
        currentTime = System.nanoTime();

        // no rocket currently on screen
        if(currentRocket == null || (rud && Utility.secondsElapsed(rudTime, currentTime) > 5) ){


            // grab the first rocket
            currentRocket = population.get(0).getGeneration().get(ids.get(workingIndex));
            currentRocket.getFuselage().setBounds(rocketBound);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            currentRocket.getFuselage().setPaint(paint);
            // set launch time
            launchTime = System.nanoTime();
            // get current frame
            currentFrame = currentRocket.getSimulation().position(Utility.secondsElapsed(launchTime, currentTime));
            currentRocket.getFuselage().setRotation(currentFrame.getDirection());
            // update index
            workingIndex++;
            rud = false;

        // rocket is currently selected
        }else if(!rud){

            if(currentRocket.getSimulation().isRUD(Utility.secondsElapsed(launchTime, currentTime))){
                rud = true;
                rudTime = System.nanoTime();
            }else {

                // Update frames
                previousFrame = currentFrame;
                currentFrame = currentRocket.getSimulation().position(Utility.secondsElapsed(launchTime, currentTime));
                currentRocket.getFuselage().setRotation(currentFrame.getDirection());

                // Get velocity to update background
                Vector v = currentFrame.getPosition().subtract(previousFrame.getPosition())
                        .multiply(currentRocket.getSimulation().getInterval());
                bg.setVelocity(v);
                rud = false;
            }
        }

    }

    public void draw(Canvas canvas){
        // Canvas state
        super.draw(canvas);
        final int savedState = canvas.save();
        final float scaleFactorX = getWidth() / (WIDTH * 1f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1f);
        canvas.scale(scaleFactorX, scaleFactorY);


        bg.draw(canvas);

        if(rud)
            expload.draw(canvas);
        else
            currentRocket.getFuselage().draw(canvas);

        // Draw UI Objects
        popLabel.draw(canvas);
        fitLabel.draw(canvas);
        canvas.restoreToCount(savedState);

    } // end draw


    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_DOWN){


            if(popLabel.activate(event.getX(), event.getY())) {
                Intent i = new Intent(getContext(), PopulationActivity.class);
                i.putExtra("pop", population);
                getContext().startActivity(new Intent(getContext(), PopulationActivity.class));
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){ }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){

        boolean retry = true;
        int counter = 0;

        while(retry && counter < 1000){

            counter++;

            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null; // to be picked up by GC
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        } // end while

    } // end surfaceDestroyed

} // end Launchpad
