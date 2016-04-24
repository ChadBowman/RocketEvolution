package net.orthus.rocketevolution.ui;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Chad on 8/4/2015.
 */
public abstract class Graphic {

    //===== INSTANCE VARIABLES
    private Bounds bounds;
    private Paint paint;
    private float scale;
    private float rotation;

    public Graphic(){
        bounds = new Bounds();
        paint = new Paint();
    }

    //===== ABSTRACT METHODS
    public abstract void update();
    public abstract void draw(Canvas canvas);

    //===== ACCESSORS
    public Bounds getBounds() { return bounds; }
    public Paint getPaint() { return paint; }
    public float getScale() { return scale; }
    public float getRotation() { return rotation; }

    public void setBounds(Bounds bounds) { this.bounds = bounds; }
    public void setPaint(Paint paint) { this.paint = paint; }
    public void setScale(float scale){ this.scale = scale; }
    public void setRotation(float rotation){
        this.rotation = rotation % (float)(Math.PI * 2);
    }

} // Graphic
