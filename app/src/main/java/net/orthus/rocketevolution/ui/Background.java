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
    private Vector velocity;
    private long previous;

    //===== CONSTRUCTOR
    public Background(Bitmap image){

        super.setScale(image.getWidth() / Launchpad.WIDTH);

        this.image = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * getScale()),
                (int) (image.getHeight() * getScale()), true);

        x = 0;
        y = -image.getHeight() + Launchpad.HEIGHT;
        velocity = new Vector();
        previous = System.nanoTime();
    }

    public void update(){
        float s = (float) Utility.secondsElapsed(previous, System.nanoTime());
        add((float) velocity.getX() * -s, (float)velocity.getY() * -s);

    }

    public void draw(Canvas canvas){

        canvas.drawBitmap(image, x, y, null);

        if(x > 0)
            canvas.drawBitmap(image, x - image.getWidth(), y, null);
        if(x < 0)
            canvas.drawBitmap(image, x + image.getWidth(), y, null);

        if(x > canvas.getWidth() || x < -canvas.getWidth())
            x = 0;
    }

    public void add(float x, float y){
        this.x += x;
        this.y += y;
    }

    public void reset(){
        x = 0;
        y = -image.getHeight() + Launchpad.HEIGHT;
    }

    public void setVelocity(Vector v){ velocity = v; }


} // Background
