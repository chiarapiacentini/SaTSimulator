package policy;

import main.Position;
import main.UAV;
import main.Velocity;
import main.Parameters;

public class Track extends PolicyUAV {

    public Track(UAV u) {
        super(u);
    }

    public Velocity update() {

        Velocity velTarget = uav.getEstimatedTargetVelocity();
        Velocity currentVelocity = uav.getCurrentVelocity();
        Position estimatedPosition = uav.getEstimatedTargetPosition();//Position.move(pTarget, velTarget);
        Position currentPosition = uav.getCurrent();
        double radius = uav.getRadius();
        //double angle = uav.getCurrentVelocity().angleTo(uav.getCurrent(), estimatedPosition);
        //uav.setCameraFacing(angle);

        double d = estimatedPosition.distance(currentPosition);
        double x = estimatedPosition.getX() - currentPosition.getX();
        double y = estimatedPosition.getY() - currentPosition.getY();
        Velocity relvel = new Velocity(currentVelocity.getVx()-velTarget.getVx(),
                currentVelocity.getVy()-velTarget.getVy());


        double crossprod = relvel.getVx()*y - relvel.getVy()*x;
        double dotprod = relvel.getVx()*x+relvel.getVy()*y;

        Velocity newVelocity = currentVelocity;
        if( Math.abs(d - radius ) <  Parameters.flightAccuracy) {
            double a1 = Math.min(relvel.speed()/radius,Parameters.uavVelocity/Parameters.tightTurn);
            if(crossprod > 0) {
                newVelocity = currentVelocity.rotate(-a1);
            }
            else {
                newVelocity = currentVelocity.rotate(a1);
            }
            return newVelocity;
        }

        if(d > radius) {
            // Outside circle
            double a1 = Math.asin((radius)/(d));
            double a2 = Math.acos((dotprod)/(relvel.speed()*d));
            if(a1 < a2-0.01) {
                double a3 = Math.min(a2-a1,Parameters.uavVelocity/Parameters.tightTurn);
                if(crossprod > 0) {
                    newVelocity = currentVelocity.rotate(a3);
                } else {
                    newVelocity =  currentVelocity.rotate(-a3);
                }
                return newVelocity;
            }
            if(a1 >= a2-0.01) {
                return newVelocity;
            }
            newVelocity = currentVelocity.rotate(relvel.speed()/Parameters.tightTurn);
            return newVelocity;
        }
        return  newVelocity;
    }
}
