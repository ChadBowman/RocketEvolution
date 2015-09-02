package net.orthus.rocketevolution.rocket;

import android.graphics.Canvas;

import net.orthus.rocketevolution.ui.Graphic;
import net.orthus.rocketevolution.ui.Launchpad;
import net.orthus.rocketevolution.fuels.KerosenePeroxide;
import net.orthus.rocketevolution.ui.Statistic;

/**
 * Created by Chad on 7/23/2015.
 */
public class Rocket extends Graphic{
    
    private RocketBody body;

    public Rocket(){
        this.body = new RocketBody(10, 4000, new KerosenePeroxide("", 23.4f)); // base unit in CMs
    }

    public long volume(){ return body.getVolume() / Launchpad.MILLION; }
    public long surfaceArea(){ return body.getSurfaceArea() / 10000; }
    public long height(){ return body.getHeight() / 100; }
    public long width(){ return body.getWidth() / 100; }

    public RocketBody getBody(){ return body; }

    public Statistic displayThrust(float pa){
        return new Statistic('N').setValue(body.thrust(pa));
    }

    public Statistic displayMass(){
        Statistic s = new Statistic('g').setValue(body.getMass() * 1000);
        s.setTargetDecimal(3);
        return s;
    }

    @Override
    public void update() { }

    @Override
    public void draw(Canvas canvas) {

        body.setBounds(bounds);
        body.setPaint(paint);
        body.draw(canvas);
    }
}
