package net.orthus.rocketevolution.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * Created by Chad on 19-Mar-16.
 */
public class Label extends Graphic {

    //===== INSTANCE VARIABLES

    private String text;
    private Bitmap original,
                    image;

    private boolean enabled,
                    maintainRatio;

    private float textX, textY;

    //===== CONSTRUCTORS
    public Label(){
        enabled = true;
        maintainRatio = false;
        setPaint(new Paint());
    }

    public Label(String text, Bitmap bitmap){
        this.text = text;
        original = bitmap;
        image = bitmap;
        enabled = true;
        maintainRatio = false;
        setPaint(new Paint());
    }

    //===== PUBLIC METHODS

    public boolean activate(float x, float y){
        return getBounds().inBounds(x, y) && enabled;
    }

    public void setFont(int color, float size, Typeface typeface){

        if(typeface == null)
            typeface = Typeface.DEFAULT;

        Paint p = new Paint();
        p.setColor(color);
        p.setTextSize(size);
        p.setTypeface(typeface);
        setPaint(p);
    }

    public void setImage(Bitmap image){
        original = image;

        if(getBounds() != null){

            float x = getBounds().width() / original.getWidth();
            float y = getBounds().height() / original.getHeight();
            setScale((x < y)? x : y);

            if(maintainRatio)
                this.image = Bitmap.createScaledBitmap(original,
                        (int) (original.getHeight() * getScale()),
                        (int) (original.getWidth() * getScale()), false);
            else {
                this.image = Bitmap.createScaledBitmap(original,
                        (int) (original.getHeight() * x),
                        (int) (original.getWidth() * y), false);
            }
        }
    }

    public void setTextLocation(float x, float y){
        textX = x;
        textY = y;
    }

    //===== OVERRIDES

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {

        if(enabled) {

            float x = getBounds().getLeft(),
                    y = getBounds().getTop();

            if(image != null)
                canvas.drawBitmap(image, x, y, getPaint());

            if(text != null)
                canvas.drawText(text, x + textX, y + textY, getPaint());


        }
    }

    @Override
    public void setBounds(Bounds bounds){
        super.setBounds(bounds);

        if(original != null) {
            float x = bounds.width() / original.getWidth();
            float y = bounds.height() / original.getHeight();
            // use smallest
            float scale = (x < y)? x : y;
            super.setScale(scale);

            if(maintainRatio)
                image = Bitmap.createScaledBitmap(original,
                        (int) (original.getHeight() * scale),
                        (int) (original.getWidth() * scale), false);
            else
                image = Bitmap.createScaledBitmap(original,
                        (int) (original.getHeight() * x),
                        (int) (original.getWidth() * y), false);
        }
    }

    //===== ACCESSORS
    public void setText(String text){ this.text = text; }
    public void setEnabled(boolean b) { enabled = b; }
    public void setMaintainImageRatio(boolean b) { maintainRatio = b; }

} // Label
