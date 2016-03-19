package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.orthus.rocketevolution.GameThread;
import net.orthus.rocketevolution.R;
import net.orthus.rocketevolution.fuels.Fuel;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;
import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.rocket.Rocket;
import net.orthus.rocketevolution.simulation.Frame;
import net.orthus.rocketevolution.simulation.Simulation;
import net.orthus.rocketevolution.simulation.Simulator;
import net.orthus.rocketevolution.utility.Utility;

import java.util.ArrayList;

/**
 * Created by Chad on 7/23/2015.
 */
public class Launchpad extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 1440;
    public static final int HEIGHT = 2560;
    public static final int MILLION = 1000000;


    private GameThread thread;

    private long startTime;
    private Background bg;
    private Rocket rocket;
    private Simulation sim;
    private Frame frame;
    private Animation expload;
    private Bounds rocketBound;


    public Launchpad(Context context){
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        int fithx = WIDTH / 3;
        int fithy = HEIGHT / 4;
        rocketBound = new Bounds(fithx, WIDTH - fithx, fithy, HEIGHT - fithy);

        expload = new Animation(BitmapFactory.decodeResource(getResources(), R.drawable.explosion_large), 5, 5);
        expload.setBounds(rocketBound);

        //bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.bgtest));
        expload.setRepeat(false);
        expload.setPlay(true);


        //Fuel.fuels.add(Fuel.KEROSENE_PEROXIDE, new KerosenePeroxide("", 810, 8));
        //rocket = new Rocket();
        //sim = new Simulator(rocket).run(10, 60);

        // // DON'T TOUCH BELOW! // //
        thread = new GameThread(getHolder(), this);
        startTime = System.nanoTime();
        // Safely start the game
        thread.setRunning(true);
        thread.start();
    }

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

    public void update(){
        //frame = sim.position((System.nanoTime() - startTime) / 1e9);
        expload.update();
    }

    public void draw(Canvas canvas){
        super.draw(canvas);
        final int savedState = canvas.save();

        final float scaleFactorX = getWidth() / (WIDTH * 1f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1f);

        canvas.scale(scaleFactorX, scaleFactorY);

        // draw stuff
        //bg.set((int) (frame.getPosition().getX() / -1000),
         //       (int) (frame.getPosition().getY() / -100000));
        //bg.draw(canvas);
        //Paint paint = new Paint();
        //paint.setColor(Color.WHITE);
        //paint.setStyle(Paint.Style.FILL);



        //rocket.getFuselage().setBounds(rocketBound);
        //rocket.getFuselage().setPaint(paint);
        //rocket.getFuselage().setRotation(frame.getDirection());
        //rocket.getFuselage().draw(canvas);
        expload.draw(canvas);

        //paint.setStyle(Paint.Style.FILL);

        //paint.setColor(Color.RED);
        //canvas.drawCircle(WIDTH / 2, rocket.getFuselage().getRelativeCenter(), 10, paint);

        canvas.restoreToCount(savedState);


    } // end draw


    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //rocket = new Rocket();
            //sim = new Simulator(rocket).run(10, 60);
            //startTime = System.nanoTime();
            //bg.reset();
            expload.setPlay(true);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){ }

} // end Launchpad
