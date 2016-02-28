package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.ui.Launchpad;

import java.util.BitSet;

/**
 * Created by Chad on 7/23/2015.
 */
public class Background extends Graphic{

    private Bitmap image;
    private int height;

    public Background(Bitmap image){

        this.scale = image.getWidth() / Launchpad.WIDTH;
        this.image = Bitmap.createScaledBitmap(image,
                (int) (image.getWidth() * scale),
                (int) (image.getHeight() * scale), true);

        this.height = -this.image.getHeight() + Launchpad.HEIGHT;
    }

    public void update(){}

    public void draw(Canvas canvas){

        canvas.drawBitmap(image, 0, height, null);
    }

    public void reset(){
        height = -this.image.getHeight() + Launchpad.HEIGHT;
    }

    public void addH(int h){
        height += h;
    }
}
