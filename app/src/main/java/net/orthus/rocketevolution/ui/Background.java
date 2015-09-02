package net.orthus.rocketevolution.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import net.orthus.rocketevolution.ui.Launchpad;

/**
 * Created by Chad on 7/23/2015.
 */
public class Background {


    private Paint paint;
    private Path path;
    private int[] x, y;

    public Background(){
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        path = new Path();
        x = new int[4];
        y = new int[4];

        x[0] = 0;
        y[0] = 0;
        x[1] = Launchpad.WIDTH;
        y[1] = 0;
        x[2] = Launchpad.WIDTH;
        y[2] = Launchpad.HEIGHT;
        x[3] = 0;
        y[3] = Launchpad.HEIGHT;

        path.moveTo(x[0], y[0]);
        for(int i=1; i<x.length; i++)
            path.lineTo(x[i], y[i]);
        path.close();
    }

    public void draw(Canvas canvas){

        canvas.drawPath(path, paint);
    }
}
