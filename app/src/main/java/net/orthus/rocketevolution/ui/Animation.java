package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import net.orthus.rocketevolution.utility.Utility;

/**
 * Created by Chad on 18-Mar-16.
 */
public class Animation extends Graphic{

    //===== INSTANCE VARIABLES

    private Bitmap[] original,      // unscaled originals
                    sprites;        // sprites in sequence
    private int currentFrame;       // index of current frame to draw
    private float delay;            // delay between frame changes
    private long startTime;         // time to compare against for delay
    private boolean enable,         // draw when true
                    repeat;         // loop through sequence when true

    //===== CONSTRUCTOR

    public Animation(Bitmap spriteSheet, int inRow, int inColumn){

        // defaults
        delay = 0.1f; // 10 times a second
        enable = true;
        repeat = true;
        currentFrame = -1;

        // split sheet into sequential bitmaps
        original = new Bitmap[inRow * inColumn];

        int width = spriteSheet.getWidth() / inRow;
        int height = spriteSheet.getHeight() / inColumn;

        // for each sprite
        int row = 0, column = 0;
        for(int i=0; i < original.length; i++, column++){

            // if transitioning to the next row
            if(i % inRow == 0 && i > 0) {
                row++;
                column = 0;
            }

            // cut out sprite from sheet
            original[i] = Bitmap.createBitmap(spriteSheet,
                    column * width, row * height, width, height);

        } // for

        // use original sprites to start
        sprites = original;

    } // Animation()

    //===== PUBLIC METHODS

    public void toggleEnable(){

        if(enable)
            setEnable(false);
        else
            setEnable(true);
    }

    //===== OVERRIDES
    @Override
    public void update() {
        
        long now = System.nanoTime();

        // animation is enabled and time has elapsed enough for frame change
        if(enable && Utility.secondsElapsed(startTime, now) > delay) {

            if(repeat) {
                currentFrame = (currentFrame + 1) % sprites.length;

            } else {
                // non-repeating animation
                if (currentFrame < sprites.length-1)
                    currentFrame++;
                else
                    enable = false;
            }
            // reset startTime
            startTime = System.nanoTime();
        }

    } // update()


    @Override
    public void draw(Canvas canvas) {

        if(enable) {
            update();
            canvas.drawBitmap(sprites[currentFrame],
                    getBounds().getLeft(), getBounds().getTop(), getPaint());
        }
    }

    @Override
    public void setBounds(Bounds bounds){
        super.setBounds(bounds);

        // get scaling factors
        float x = bounds.width() / original[0].getWidth();
        float y = bounds.height() / original[0].getWidth();
        // use the smallest one
        float scale = (x < y)? x : y;
        super.setScale(scale);

        // properly scale new bitmaps
        for(int i=0; i < original.length; i++)
            sprites[i] = Bitmap.createScaledBitmap(original[i],
                    (int) (original[i].getWidth() * scale),
                    (int) (original[i].getHeight() * scale),
                    false);
        
    } // setBounds()

    //===== ACCESSORS
    public void setRepeat(boolean repeat){
        this.repeat = repeat;
    }

    public void setDelay(float delay){
        this.delay = delay;
    }


    public void setEnable(boolean enable){
        this.enable = enable;

        if(enable) {
            startTime = System.nanoTime();
            currentFrame = -1;
        }

    }

} // Animation
