package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.math.Vector;
import net.orthus.rocketevolution.ui.Launchpad;
import net.orthus.rocketevolution.utility.Utility;

import java.util.BitSet;

/**
 * Created by Chad on 7/23/2015.
 */
public class Background extends Graphic{

    //===== INSTANCE VARIABLES
    private Bitmap image;
    private float x, y;
    private long previous;
    private int width, height;

    //===== CONSTRUCTOR
    public Background(Bitmap image, int width, int height){

        this.width = width;
        this.height = height;

        super.setScale(image.getWidth() / width);

        this.image = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * getScale()),
                (int) (image.getHeight() * getScale()), true);

        x = 0;
        y = -image.getHeight() + height;
        previous = System.nanoTime();
    }

    public void update(){


    }

    public void draw(Canvas canvas){


        // if part of the background is off the screen, loop it around
        if(x > 0)
            canvas.drawBitmap(image, x - image.getWidth(), y, null);
        if(x < 0)
            canvas.drawBitmap(image, x + image.getWidth(), y, null);

        if(x > image.getWidth())
            canvas.drawBitmap(image, x - image.getWidth() * 2, y, null);
        if(x < -image.getWidth())
            canvas.drawBitmap(image, x + image.getWidth() * 2, y, null);
        else
            canvas.drawBitmap(image, x, y, null);

    }

    public void add(float x, float y){
        this.x += x;
        this.y += y;
    }

    public void set(Vector v){

        x = (float) -v.getX() * 50000;
        y = (float) v.getY() + height - image.getHeight();
    }

    public void reset(){
        x = 0;
        y = -image.getHeight() + height;
    }

    public boolean atGround(){
        return y <= height - image.getHeight();
    }



} // Background
