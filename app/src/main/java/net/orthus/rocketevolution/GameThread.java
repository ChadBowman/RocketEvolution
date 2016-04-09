package net.orthus.rocketevolution;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import net.orthus.rocketevolution.ui.Launchpad;

/**
 * Created by Chad on 7/23/2015.
 */
public class GameThread extends Thread {

    private int FPS = 10;
    private float averageFPS;
    private SurfaceHolder surfaceHolder;
    private Launchpad launchpad;
    private boolean running;
    public static Canvas canvas;

    public GameThread(SurfaceHolder surfaceHolder, Launchpad launchpad){
        super();
        this.surfaceHolder = surfaceHolder;
        this.launchpad = launchpad;

    }

    public void setRunning(boolean b){ running = b; }

    @Override
    public void run(){

        long startTime;
        long waitTime;
        long totalTime = 0;
        long frameCount = 0;
        long targetTime = 1000/FPS;
        long elapsed;

        while(running){
            startTime = System.nanoTime();
            canvas = null;

            try{
                canvas = this.surfaceHolder.lockCanvas();

                synchronized (surfaceHolder){
                    this.launchpad.update();
                    this.launchpad.draw(canvas);
                }
            }catch(Exception e){}

            finally {
                if(canvas != null){

                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            elapsed = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - elapsed;

            try{
                this.sleep(waitTime);
            }catch(Exception e){}

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS){
                averageFPS = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
            }

        } // end while

    } // end run
    
} // end GameThread
