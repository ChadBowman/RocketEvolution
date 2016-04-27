package net.orthus.rocketevolution.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import net.orthus.rocketevolution.ui.Launchpad;

/**
 * Created by Chad on 7/23/2015.
 */
public class GameThread extends Thread {

    private int FPS = 30;
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
    public boolean isRunning(){ return running; }
    public float getAverageFPS(){ return averageFPS; }

    @Override
    public void run(){

        long startTime;
        long waitTime;
        long totalTime = 0;
        long frameCount = 0;
        long targetTime = 1000 / FPS;
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
            }catch(Exception e){
                e.printStackTrace();
            }

            finally {
                if(canvas != null){

                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            elapsed = (System.nanoTime() - startTime) / Launchpad.MILLION;
            waitTime = targetTime - elapsed;

            try{
                Thread.sleep(waitTime);
            }catch(Exception ignored){}

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS){
                averageFPS = 1000 / ((totalTime / frameCount) / Launchpad.MILLION);
                frameCount = 0;
                totalTime = 0;
            }

        } // end while

    } // end run
    
} // end GameThread
