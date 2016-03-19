package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import net.orthus.rocketevolution.utility.Utility;

/**
 * Created by Chad on 18-Mar-16.
 */
public class Animation extends Graphic{

    //===== INSTANCE VARIABLES
    private Bitmap[] sprites;
    private int currentFrame;
    private float delay;

    private long startTime;
    private boolean play, repeat;

    public Animation(Bitmap spriteSheet, int inRow, int inColumn){

        // default update rate
        delay = 0.1f; // 10 times a second
        play = false;
        repeat = true;
        currentFrame = 0;

        sprites = new Bitmap[inRow * inColumn];

        int width = spriteSheet.getWidth() / inRow;
        int height = spriteSheet.getHeight() / inColumn;

        // for each sprite
        int row = 0, column = 0;
        for(int i=0; i < sprites.length; i++, column++){

            // if transitioning to the next row
            if(i % inRow == 0 && i > 0) {
                row++;
                column = 0;
            }

            // cut out sprite from sheet
            sprites[i] = Bitmap.createBitmap(spriteSheet,
                    column * width, row * height, width, height);

        } // for

    } // Animation()

    //===== PUBLIC METHODS
    public void togglePlay(){

        if(play)
            setPlay(false);
        else
            setPlay(true);
    }

    //===== OVERRIDES
    @Override
    public void update() {


        long now = System.nanoTime();

        // animation set to play and time has elapsed enough for frame change
        if(play && Utility.secondsElapsed(startTime, now) > delay) {
            if(repeat) {
                currentFrame = (currentFrame + 1) % sprites.length;

            } else {
                // non-repeating animation
                if (currentFrame < sprites.length)
                    currentFrame++;
                else
                    setPlay(false);
            }
            // reset startTime
            startTime = System.nanoTime();
        }

    } // update

    @Override
    public void draw(Canvas canvas) {

        if(play)
            canvas.drawBitmap(sprites[currentFrame], bounds.getLeft(), bounds.getTop(), paint);

    }

    @Override
    public void setBounds(Bounds bounds){
        super.setBounds(bounds);

        // get scaling factors
        float xFactor = (float) bounds.width() / sprites[0].getWidth();
        float yFactor = (float) bounds.height() / sprites[0].getWidth();

        // use the smallest one
        scale = (xFactor < yFactor)? xFactor : yFactor;

        for(int i=0; i < sprites.length; i++)
            sprites[i] = Bitmap.createScaledBitmap(sprites[i],
                    (int) (sprites[i].getWidth() * scale),
                    (int) (sprites[i].getHeight() * scale),
                    false);
    }

    //===== ACCESSORS
    public void setRepeat(boolean repeat){
        this.repeat = repeat;
    }

    public void setDelay(float delay){
        this.delay = delay;
    }

    public void setPlay(boolean play){
        this.play = play;

        if(play)
            startTime = System.nanoTime();

        currentFrame = 0;
    }

} // Animation
