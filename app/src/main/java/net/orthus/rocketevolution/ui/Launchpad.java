package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import net.orthus.rocketevolution.engine.GameThread;
import net.orthus.rocketevolution.engine.Player;
import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.evolution.SimpleCrossover;
import net.orthus.rocketevolution.evolution.Generation;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Fitness;
import net.orthus.rocketevolution.simulation.Frame;
import net.orthus.rocketevolution.utility.Tuple;
import net.orthus.rocketevolution.utility.Utility;

import java.util.Locale;


/**
 * Created by Chad on 7/23/2015.
 */
public class Launchpad extends SurfaceView implements SurfaceHolder.Callback {

    private static final Tuple<Integer> referenceDimensions = new Tuple<>(1440, 2560);
    public static final int MILLION = 1000000;

    private static final int LAUNCH_DELAY = 3;

    // Management elements
    private GameThread thread;
    private Thread breedThread;
    private long currentTime, launchTime, rudTime;

    // Graphic elements
    private Background bg;
    private Animation explode; //, exhaust;
    private Bounds rocketBound, exploBound;
    private Label popButton, fitLabel, ticker, fuel, loc, speed, genL, fps, fit;

    // Player elements
    private Player player;
    private int deviceWidth, deviceHeight;

    // Temp elements
    private Rocket currentRocket;
    private Generation currentGen, nextGen;
    private int workingIndex;
    private Frame currentFrame;
    private boolean rud, good;

    private int genSize = 9;
    private int genNum, rocNum;

    public Launchpad(Context context){
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);

        // get display dimensions
        DisplayMetrics dis = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dis);

        deviceWidth = dis.widthPixels;
        deviceHeight = dis.heightPixels;

        //Utility.p("%d x %d", deviceWidth, deviceHeight);

        rocketBound = new Bounds(deviceWidth / 3, deviceWidth - (deviceWidth / 3),
                        deviceHeight / 4, deviceHeight - (deviceHeight / 4));

        exploBound = new Bounds(deviceWidth / 3, 2/3f * deviceWidth,
                deviceHeight / 2, 3/4f * deviceHeight);

        nextGen = new Generation();

        breedThread = new Thread(){
            @Override
            public void run() {
                nextGen.setGeneration(new SimpleCrossover(currentGen.getGeneration()).evolve());
                //currentGen.setGeneration(new DifferentialEvolution(currentGen.getGeneration()).evolve());
                nextGen.runSims();
            }
        };

        createUIObjects();

    } // Launchpad


    //===== PRIVATE METHODS
    private void createUIObjects(){

        float s = deviceWidth / 15f;
        float w = (4 * deviceWidth / 5f) - s;

        // Population label
        popButton = new Label(
                "Toggle Fitness", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        popButton.setBounds(new Bounds(s, s + w, deviceHeight / 20f, 2 * deviceHeight / 20f));
        popButton.setFont(Color.WHITE, 70, Typeface.DEFAULT);
        popButton.setTextLocation(90, 100);

        // Optimization label
        fitLabel = new Label(
                "Optimise", BitmapFactory.decodeResource(getResources(), R.drawable.button0));
        fitLabel.setBounds(new Bounds(deviceWidth - s - w, deviceWidth - s, deviceHeight / 20f, 2 * deviceHeight / 20f));
        fitLabel.setFont(Color.WHITE, 70, Typeface.DEFAULT);
        fitLabel.setTextLocation(100, 100);

        // Ticker label
        ticker = new Label();
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

        fuel = new Label();
        fuel.setBounds(new Bounds(deviceWidth / 10f, 0, deviceHeight / 5f, 0));
        fuel.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        speed = new Label();
        speed.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) + 60, 0));
        speed.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        loc = new Label();
        loc.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) + 120, 0));
        loc.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        fit = new Label();
        fit.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight /5f) + 180, 0));
        fit.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        genL = new Label();
        genL.setBounds(new Bounds(deviceWidth / 10f, 0, (deviceHeight / 5f) - 60, 0));
        genL.setFont(Color.WHITE, 60, Typeface.DEFAULT);

        // Frames per second
        fps = new Label();
        fps.setBounds(new Bounds(deviceWidth / 12f, 0, 19/20f * deviceHeight, 0));
        fps.setFont(Color.WHITE, 40, Typeface.DEFAULT);
    }

    private void nextRocket(){
        Utility.p("Next rocket called %d %d", workingIndex, nextGen.getGeneration().entries());
        if(workingIndex == currentGen.getGeneration().entries())
            breedNew();


        // get new rocket from generation
        currentRocket = currentGen.getGeneration().values().get(workingIndex);
        launchTime = System.nanoTime();

        // set up
        currentRocket.getFuselage().setBounds(rocketBound);
        currentRocket.getFuselage().setEngineBounds();
        //currentRocket.getFuselage().setEngineExhaust(exhaust);
        workingIndex++;
        rocNum++;
        player.setWorkingIndex(workingIndex);
        player.setRocNum(rocNum);
        rud = false;
        good = false;

    }

    private void breedNew(){

        try {
            // make sure breeding is finished
            breedThread.join();
            // set a new generation
            currentGen.setGeneration(nextGen.getGeneration());
            Utility.p("Current Gen has %d ", currentGen.getGeneration().entries());
            // set keys to reference later
            player.setGeneration(currentGen.getGeneration().keys());

            // reset values
            rocNum = 0;
            genNum++;
            Utility.p("Gen num changed! to %d", genNum);
            player.setGenNum(genNum);
            workingIndex = 0;

            // re-breed rockets for next time
            breedThread.run();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    //===== INTERFACES
    @Override
    public void surfaceCreated(SurfaceHolder holder){

        workingIndex = player.getWorkingIndex();
        rocNum = player.getRocNum();
        genNum = player.getGenNum();

        if(nextGen.getGeneration().entries() == 0)
            breedThread.run();

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

            // dont have rockets fly past their simulation
            if(Utility.secondsElapsed(launchTime, currentTime) > 90) {
                good = true;
                rud = true;
            }

            // check for RUD
            if(currentRocket.getSimulation().isRUD(Utility.secondsElapsed(launchTime, currentTime))){
                rud = true;
                rudTime = System.nanoTime();

            }else {
                // Update frames
                currentFrame = currentRocket.getSimulation().position(Utility.secondsElapsed(launchTime, currentTime));
                currentRocket.getFuselage().setRotation(currentFrame.getDirection());
               // Utility.p("%s", currentFrame.toString());

                ticker.setText(String.format(Locale.US, "T + %.1f", Utility.secondsElapsed(launchTime, currentTime)));
                speed.setText(String.format(Locale.US, "Speed: %.0f m/s", currentFrame.getVelocity().getMagnitude()));
                fuel.setText(String.format(Locale.US, "Fuel: %.0f%%", currentFrame.getRemainingFuelProportion() * 100));
                loc.setText(String.format(Locale.US, "Altitude: %.2f Km", currentFrame.getPosition().getY() / 1000f));
                genL.setText(String.format(Locale.US, "Generation %d, Rocket %d", genNum, rocNum));
                fps.setText(String.format(Locale.US, "%.0f FPS", thread.getAverageFPS()));
                fit.setText(String.format(Locale.US, "Fitness: %s", Fitness.getFitness(Player.selectedFitness).name()));

                //update background
                bg.set(currentFrame.getPosition().multiply(10));

            } // else

        } // if not rud

    } // update()

    public void draw(Canvas canvas){
        super.draw(canvas);
        final int savedState = canvas.save();

        // Draw background first
        bg.draw(canvas);

        if(rud && !good)
            explode.draw(canvas);
        else
            currentRocket.getFuselage().draw(canvas);

        // Draw labels
        popButton.draw(canvas);
        //fitLabel.draw(canvas);
        ticker.draw(canvas);
        fuel.draw(canvas);
        loc.draw(canvas);
        speed.draw(canvas);
        genL.draw(canvas);
        fps.draw(canvas);
        fit.draw(canvas);

        canvas.restoreToCount(savedState);

    } // end draw


    @Override
    public boolean onTouchEvent(MotionEvent event){

        rud = true;

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            if(popButton.activate(event.getX(), event.getY())) {
                //Intent i = new Intent(getContext(), PopulationActivity.class);
                //i.putExtra("pop", player.getGeneration());
                //i.putExtra("test", "Rocket");
                //getContext().startActivity(i);
                Player.selectedFitness = (Player.selectedFitness + 1) % 3;
                breedThread.run();
                rud = false;
            }
        }

        if(rud)
            rudTime = System.nanoTime();

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int deviceWidth, int deviceHeight){

    }

    public void kill(){
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

        if(thread != null)
            kill();

    } // end surfaceDestroyed

    public Generation getCurrentGen(){ return currentGen; }
    public Player getPlayer(){ return player; }
    public int getWorkingIndex(){ return workingIndex; }
    public GameThread getThread(){ return thread; }

    public void setCurrentGen(Generation g){ currentGen = g;}
    public void setPlayer(Player p){ player = p; }
    public void setRocNum(int x){ rocNum = x; }
    public void setGenNum(int x){ genNum = x; }

} // end Launchpad
