package net.orthus.rocketevolution.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.orthus.rocketevolution.GameThread;
import net.orthus.rocketevolution.rocket.Engine;
import net.orthus.rocketevolution.rocket.Rocket;

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
    private Engine engine;
    private int launchTime;

    private double d;

    public Launchpad(Context context){
        super(context);

        getHolder().addCallback(this);

        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background();
        launchTime = 5;

        rocket = new Rocket();

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

        long elapsed = (System.nanoTime() - startTime) / MILLION;

        if(elapsed > 5000){

            rocket = new Rocket();

            launchTime = 0;
            startTime = System.nanoTime();

        }else if(elapsed > 4000){
            launchTime = 1;
        }else if(elapsed > 3000){
            launchTime = 2;
        }else if(elapsed > 2000){
            launchTime = 3;
        }else if(elapsed > 1000){
            launchTime = 4;
        }

    }

    public void draw(Canvas canvas){
        super.draw(canvas);

        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if(canvas != null){
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);

            // draw stuff
            bg.draw(canvas);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            int fithx = WIDTH / 3;
            int fithy = HEIGHT / 4;

            Bounds rocketBound = new Bounds(fithx, WIDTH - fithx, fithy, HEIGHT - fithy);

            rocket.setBounds(rocketBound);
            rocket.setPaint(paint);
            rocket.draw(canvas);

            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.BLUE);
            canvas.drawCircle(WIDTH / 2, rocket.getBody().getTrueCenter(), 10, paint);

            paint.setColor(Color.RED);
            canvas.drawCircle(WIDTH / 2, rocket.getBody().getRelativeCenter(), 10, paint);

            drawStats(canvas);

            canvas.restoreToCount(savedState);

        } // end if

    } // end draw

    public void drawStats(Canvas canvas){

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format("T-00:00:0%d", launchTime),
                WIDTH / 4, HEIGHT - (HEIGHT / 10), paint);

        int line = HEIGHT / 15;
        int dy = line / 3;

        paint.setTextSize(50);
        String fmt = "%-10s %d m%s";
        canvas.drawText(String.format(fmt, "Width:", rocket.width(), ""),
                WIDTH / 15, line, paint);
        canvas.drawText(String.format(fmt, "Height:", rocket.height(), ""),
                WIDTH / 15, line + dy, paint);
        canvas.drawText(String.format(fmt, "Area:", rocket.surfaceArea(), "\u00b2"),
                WIDTH / 15, line + dy * 2, paint);
        canvas.drawText(String.format(fmt, "Volume:", rocket.volume(), "\u00B3"),
                WIDTH / 15, line + dy * 3, paint);
        rocket.displayMass().draw(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){ }

} // end Launchpad
