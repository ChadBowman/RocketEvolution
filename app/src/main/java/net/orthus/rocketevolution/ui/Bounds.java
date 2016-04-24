package net.orthus.rocketevolution.ui;

/**
 * Created by Chad on 8/8/2015.
 */
public class Bounds {

    //===== INSTANCE VARIABLES
    private float left, right, top, bottom;

    //===== CONSTRUCTORS
    public Bounds(){
        setBounds(0, 0, 0, 0);
    }

    public Bounds(float left, float right, float top, float bottom) {

        setBounds(left, right, top, bottom);
    }

    //===== PUBLIC METHODS
    public float width(){ return right - left; }
    public float height(){ return bottom - top; }
    public float centerX(){ return (width() / 2) + left; }
    public float centerY(){ return (height() / 2) + top; }

    public void setBounds(float left, float right, float top, float bottom){
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public boolean inBounds(float x, float y){

        if(x > left && x < right)
            if(y > top && y < bottom)
                return true;

        return false;
    }

    public String toString(){
        return String.format("Left %f Right %f Top %f Bottom %f", left, right, top, bottom);
    }

    //===== ACCESSORS
    public float getLeft() { return left; }
    public float getRight() { return right; }
    public float getTop() { return top; }
    public float getBottom() { return bottom; }

} // Bounds
