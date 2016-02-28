package net.orthus.rocketevolution.ui;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Chad on 8/4/2015.
 */
public abstract class Graphic {

    protected Bounds bounds;
    protected Paint paint;
    protected float scale;
    protected float rotation;

    public abstract void update();
    public abstract void draw(Canvas canvas);

    public Bounds getBounds() {
        return bounds;
    }
    public Paint getPaint() { return paint; }

    public void setPaint(Paint paint) { this.paint = paint;}
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }
    public void setScale(float scale){ this.scale = scale; }
    public void setRotation(float rotation){
        this.rotation = rotation % (float)(Math.PI * 2);
    }
}
