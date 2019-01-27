package policy;

import main.Parameters;
import main.Velocity;
import main.UAV;
import main.Position;
import patterns.SpiralSearchPattern;

/**
 * Created by chiarapiacentini on 2017-05-14.
 */
public class SpiralManouvre extends PolicyUAV {

    private SpiralSearchPattern spiral;
    private boolean reachedSpiralInnerRadius;
    private boolean reachedSpiralMaximumRadius;
    private double radius;

    SpiralManouvre(SpiralSearchPattern s, UAV u){
        super(u);
        spiral = s;
        reachedSpiralInnerRadius = false;
        reachedSpiralMaximumRadius = false;
        radius = s.getInnerRadius();
    }

    public Velocity update() {
        double angle = new Velocity(0, -Parameters.uavVelocity).angleTo(uav.getCurrent(), spiral.getOrigin());
        uav.setCameraFacing(angle);
        if (!reachedSpiralInnerRadius){
            Position current = uav.getCurrent();
            Position origin = spiral.getOrigin();
            if (current.distance(origin) < spiral.getInnerRadius()) reachedSpiralInnerRadius = true;
            if (reachedSpiralInnerRadius){
                // calculate angle between current velocity and centre
                double angleEntering = uav.getCurrentVelocity().angleTo(uav.getCurrent(),spiral.getOrigin());
                Velocity velocity = uav.getCurrentVelocity().rotate(Math.toRadians(90-angleEntering));
                return velocity;
            } else {
                // head towards center of the search pattern
                Velocity velocity = Velocity.headTowards(current, origin, Parameters.uavVelocity);
                return velocity;
            }
        } else if (!reachedSpiralMaximumRadius){
            Position current = uav.getCurrent();
            Position origin = spiral.getOrigin();
            if (current.distance(origin) > spiral.getRadius()) reachedSpiralMaximumRadius = true;
            if (!reachedSpiralMaximumRadius) {
                radius += Math.min(Parameters.radiusIncrement,spiral.getRadius());
            }
            Velocity velocity = uav.getCurrentVelocity().rotate(uav.getCurrentVelocity().speed()/radius);
            return velocity;
        } else {
            // rotate around center
            Velocity velocity = uav.getCurrentVelocity().rotate(uav.getCurrentVelocity().speed()/radius);
            return velocity;
        }
        //return  new Velocity(0,0);
    }

}