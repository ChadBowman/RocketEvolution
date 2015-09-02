package net.orthus.rocketevolution.ui;

/**
 * Created by Chad on 8/8/2015.
 */
public class Bounds {

    private int left, right, top, bottom;

    public Bounds(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int width(){
        return right - left;
    }

    public int height(){
        return bottom - top;
    }

    public int centerX(){
        return (width() / 2) + left;
    }

    public int centerY(){
        return (height() / 2) + top;
    }


    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getTop() { return top; }
    public int getBottom() { return bottom; }
}
