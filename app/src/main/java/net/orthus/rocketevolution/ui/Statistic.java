package net.orthus.rocketevolution.ui;

import android.graphics.Canvas;

/**
 * Created by Chad on 8/19/2015.
 */
public class Statistic extends Graphic {


    private char unit;
    private double value;
    private int targetDecimal;


    public Statistic(char unit){
        this.unit = unit;
        this.targetDecimal = 0;
    }

    public String format(){

        int diff = String.format("%.0f", value).length() - targetDecimal;

        String format;
        String fmt = "%.0f%s";

        switch(diff){
            // kilo
            case 3: format = String.format(fmt, value / 1000, "k" + unit); break;

            // centi
            case -2: format = String.format(fmt, value * 100, "c" + unit); break;

            // milli
            case -3: format = String.format(fmt, value * 1000, "m" + unit); break;

            default: format = String.format(fmt, value, unit);
        }

        return format;

    } // end format

    public Statistic setValue(double value){
        this.value = value;
        return this;
    }
    public void setTargetDecimal(int target){ this.targetDecimal = target; }

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(format(), bounds.getLeft(), bounds.getTop(), paint);
    }

} // end Statistic
