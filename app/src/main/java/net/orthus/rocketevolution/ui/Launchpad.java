package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import net.orthus.rocketevolution.GameThread;
import net.orthus.rocketevolution.Player;
import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.population.Generation;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Frame;
import net.orthus.rocketevolution.utility.Utility;

import java.util.Locale;


/**
 * Created by Chad on 7/23/2015.
 */
public class Launchpad extends SurfaceView implements SurfaceHolder.Callback {

    //public static final int deviceWidth = 1440;
    //public static final int deviceHeight = 2560;
    public static final int MILLION = 1000000;

    private static final int LAUNCH_DELAY = 3;

    // Management elements
    private GameThread thread;
    private long currentTime, launchTime, rudTime;

    // Graphic elements
    private Background bg;
    private Animation explode;//, exhaust;
    private Bounds rocketBound, exploBound;
    private Label popLabel, fitLabel, ticker, fuel, loc, speed, genL, acc;

    // Player elements
    private Player player;
    private int fitness;
    private int deviceWidth, deviceHeight;

    // Temp elements
    private Rocket currentRocket;
    private Generation currentGen;
    private int workingIndex;
    private Frame currentFrame;
    private boolean rud;

    private int genSize = 4;
    private int genNum, rocNum;

    public Launchpad(Context context, Player player, int width, int height){
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        this.player = player;
        deviceWidth = width;
        deviceHeight = height;
        genNum = 1;
        rocNum = 0;

        rocketBound = new Bounds(deviceWidth / 3, deviceWidth - (deviceWidth / 3),
                        deviceHeight / 4, deviceHeight - (deviceHeight / 4));

        exploBound = new Bounds(deviceWidth / 3, 2/3f * deviceWidth,
                deviceHeight / 2, 3/4f * deviceHeight);


        if(player.getPopulation().size() == 0) {
            currentGen = new Generation(genSize);
            currentGen.runSims();
            player.addGeneration(currentGen.idList());
        }


    } // Launchpad


    //===== PRIVATE METHODS
    private void createUIObjects(){

        float s = deviceWidth / 15f;
        float w = (4 * deviceWidth / 7f) - s;

        // Population label
        popLabel = new Label(
                "Population", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        popLabel.setBounds(new Bounds(s, s + w, deviceHeight / 20f, 2 * deviceHeight / 20f));
        popLabel.setFont(Color.WHITE, 70, Typeface.DEFAULT);
        popLabel.setTextLocation(90, 100);

        // Optimization label
        fitLabel = new Label(
                "Optimise", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        fitLabel.setBounds(new Bounds(deviceWidth - s - w, deviceWidth - s, deviceHeight / 20f, 2 * deviceHeight / 20f));
        fitLabel.setFont(Color.WHITE, 70, Typeface.DEFAULT);
        fitLabel.setTextLocation(100, 100);

        // Ticker label
        ticker = new Label("T-00s", null);
        ticker.setBounds(new Bounds(deviceWidth / 3f, 2 * deviceWidth / 3f, 19/20f * deviceHeight,  deviceHeight));
        ticker.setFont(Color.WHITE, 150, Typeface.DEFAULT);

        // Explosion animation
        explode = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.explosion_large), 5, 5);
        explode.setBounds(exploBound);
        //explode.setRepeat(false);

        // Exhaust animation
        //exhaust = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.exhaust1), 3, 1);

        // Create background
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.bgtest), deviceWidth, deviceHeight);

        fuel = new Label("Fuel: 100%", null);
        fuel.setBounds(new Bounds(deviceWidth / 10f, 0, deviceHeight / 5f, 0));
        fuel.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        speed = new Label("Speed: 0 m/s", null);
        speed.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) + 60, 0));
        speed.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        loc = new Label();
        loc.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) + 120, 0));
        loc.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        genL = new Label();
        genL.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) - 60, 0));
        genL.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        acc = new Label();
        acc.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight /5f) + 120, 0));
    }

    private void nextRocket(){
        Utility.p("Grabbing new rocket.");
        //if(thread != null)
            //Utility.p("Average FPS: %f", thread.getAverageFPS());

        if(workingIndex == currentGen.getGeneration().entries())
            breedNew();

        // get new rocket from generation
        currentRocket = currentGen.getGeneration().values().get(workingIndex);
        //currentRocket.getSimulation().print(workingIndex);
        Utility.p("Drag: %.3f Type: %s", currentRocket.getFuselage().getDragCoefficient(),
                currentRocket.getFuselage().type);
        launchTime = System.nanoTime();
        // set up
        currentRocket.getFuselage().setBounds(rocketBound);
        currentRocket.getFuselage().setEngineBounds();
        //currentRocket.getFuselage().setEngineExhaust(exhaust);
        workingIndex++;

        rud = false;
        rocNum++;
        bg.update();
    }

    private void breedNew(){
        currentGen = new Generation(currentGen);
        genNum++;
        rocNum = 0;
        currentGen.runSims();
        workingIndex = 0;
    }


    //===== INTERFACES
    @Override
    public void surfaceCreated(SurfaceHolder holder){

        createUIObjects();

        // Set default fitness selection
        fitness = Fitness.ALTITUDE;

        // Set initial conditions
        workingIndex = 0;

        // Grab first rocket
        nextRocket();

        // Start the graphics thread
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();

    } // surfaceCreate


    public void update(){

        // Grab a new time
        currentTime = System.nanoTime();

        if(Utility.secondsElapsed(launchTime, currentTime) > 2 && currentFrame.getPosition().getY() < 0)
            rud = true;

        // Last rocket exploded
        if(rud && Utility.secondsElapsed(rudTime, currentTime) > 1){


            // grab the next rocket
            nextRocket();

        // current rocket still going
        }else if(!rud){

            // check for RUD
            if(currentRocket.getSimulation().isRUD(Utility.secondsElapsed(launchTime, currentTime))){
            //if(bg.atGround() && currentFrame.getVelocity().getY() < 0){
                rud = true;
                rudTime = System.nanoTime();

            }else {
                // Update frames
                currentFrame = currentRocket.getSimulation().position(Utility.secondsElapsed(launchTime, currentTime));
                currentRocket.getFuselage().setRotation(currentFrame.getDirection());

                ticker.setText(String.format(Locale.US, "T + %.0f", Utility.secondsElapsed(launchTime, currentTime)));
                speed.setText(String.format(Locale.US, "Speed: %.0f m/s", currentFrame.getVelocity().getMagnitude()));
                fuel.setText(String.format(Locale.US, "Fuel: %.0f%%", currentFrame.getRemainingFuelProportion() * 100));
                loc.setText(String.format(Locale.US, "Altitude: %.2f Km",
                        currentFrame.getPosition().getY() / 1000));
                genL.setText(String.format(Locale.US, "Generation %d, Rocket %d", genNum, rocNum));

                //update background
                //bg.setVelocity(currentFrame.getVelocity().multiply(0.1));
                bg.set(currentFrame.getPosition());
            }
        }

    } // update()

    public void draw(Canvas canvas){
        super.draw(canvas);
        final int savedState = canvas.save();

        // Draw background first
        bg.draw(canvas);

        if(rud) {
            //expload.setEnable(true);
            explode.draw(canvas);
        }else {
            currentRocket.getFuselage().draw(canvas);
        }

        // Draw labels
        popLabel.draw(canvas);
        //fitLabel.draw(canvas);
        ticker.draw(canvas);
        fuel.draw(canvas);
        loc.draw(canvas);
        speed.draw(canvas);
        genL.draw(canvas);

        canvas.restoreToCount(savedState);

    } // end draw


    @Override
    public boolean onTouchEvent(MotionEvent event){

        rud = true;
        rudTime = System.nanoTime();

        if(event.getAction() == MotionEvent.ACTION_DOWN){


            if(popLabel.activate(event.getX(), event.getY())) {
                Intent i = new Intent(getContext(), PopulationActivity.class);
                i.putExtra("pop", player.getPopulation());
                i.putExtra("test", "Rocket");
                getContext().startActivity(i);
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int deviceWidth, int deviceHeight){

    }

    private void kill(){
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
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){

        kill();

    } // end surfaceDestroyed

} // end Launchpad
