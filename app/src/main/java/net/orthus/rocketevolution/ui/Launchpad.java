package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.orthus.rocketevolution.Game;
import net.orthus.rocketevolution.GameThread;
import net.orthus.rocketevolution.Player;
import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.population.Generation;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Frame;
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
    private Bounds rocketBound =
            new Bounds(WIDTH / 3, WIDTH - (WIDTH / 3), HEIGHT / 4, HEIGHT - (HEIGHT / 4));
    private Label popLabel, fitLabel;
    private Paint whiteFill;

    // Player elements
    private Player player;
    private int fitness;

    // Temp elements
    private Rocket currentRocket;
    private Generation currentGen;
    private int workingIndex;
    private ArrayList<UUID> ids;
    private Frame previousFrame, currentFrame;
    private boolean rud;

    public Launchpad(Context context, Player player){
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        this.player = player;

        whiteFill = new Paint();
        whiteFill.setColor(Color.WHITE);
        whiteFill.setStyle(Paint.Style.FILL);

        if(player.getPopulation().size() == 0) {
            currentGen = new Generation(9);
            currentGen.runSims();
        }

    }


    //===== PRIVATE METHODS


    //===== INTERFACES
    @Override
    public void surfaceCreated(SurfaceHolder holder){

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

        // Create animations
        expload = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.explosion_large), 5, 5);
        expload.setBounds(rocketBound);
        //expload.setRepeat(false);

        exhaust = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.exhaust1), 1, 3);

        // Create background
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.bgtest));

        // Set default fitness selection
        fitness = Fitness.ALTITUDE;

        workingIndex = -1;
        rud = false;

        // // DON'T TOUCH BELOW! // //
        thread = new GameThread(getHolder(), this);
        // Safely start the game
        thread.setRunning(true);
        thread.start();

    } // surfaceCreate

    private Rocket fetchRocket(){
        workingIndex++;
        return currentGen.getGeneration().values().get(workingIndex);
    }


    public void update(){

        // Update the game state
        currentTime = System.nanoTime();

        // no rocket currently on screen
        if(currentRocket == null || (rud && Utility.secondsElapsed(rudTime, currentTime) > 5) ){

            // grab the next rocket
            currentRocket = fetchRocket();
            currentRocket.getSimulation().print(workingIndex);
            currentRocket.getFuselage().setBounds(rocketBound);
            currentRocket.getFuselage().setPaint(whiteFill);

            // set launch time
            launchTime = System.nanoTime();
            // get current frame
            currentFrame = currentRocket.getSimulation().position(Utility.secondsElapsed(launchTime, currentTime));
            currentRocket.getFuselage().setRotation(currentFrame.getDirection());
            rud = false;
            bg.reset();

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

                //update background
                bg.setVelocity(currentFrame.getVelocity());
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

        if(rud) {
            //expload.setEnable(true);
            expload.draw(canvas);
        }else {
            currentRocket.getFuselage().draw(canvas);
        }

        // Draw UI Objects
        popLabel.draw(canvas);
        fitLabel.draw(canvas);
        canvas.restoreToCount(savedState);

    } // end draw


    @Override
    public boolean onTouchEvent(MotionEvent event){

        rud = true;

        if(event.getAction() == MotionEvent.ACTION_DOWN){


            if(popLabel.activate(event.getX(), event.getY())) {
                Intent i = new Intent(getContext(), PopulationActivity.class);
                i.putExtra("pop", player.getPopulation());
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
