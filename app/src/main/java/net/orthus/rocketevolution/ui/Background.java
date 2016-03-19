package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.ui.Launchpad;
import net.orthus.rocketevolution.utility.Utility;

import java.util.BitSet;

/**
 * Created by Chad on 7/23/2015.
 */
public class Background extends Graphic{

    //===== INSTANCE VARIABLES
    private Bitmap image;
    private int x, y;

    //===== CONSTRUCTOR
    public Background(Bitmap image){

        scale = image.getWidth() / Launchpad.WIDTH;

        this.image = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * scale),
                (int) (image.getHeight() * scale), true);

        x = 0;
        y = -image.getHeight() + Launchpad.HEIGHT;
    }

    public void update(){}

    public void draw(Canvas canvas){

        canvas.drawBitmap(image, x, y, null);
    }

    public void set(int x, int y){
        this.x = x;
        this.y = y - image.getHeight() + Launchpad.HEIGHT;
    }

    public void reset(){
        x = 0;
        y = -image.getHeight() + Launchpad.HEIGHT;
    }


} // Background
