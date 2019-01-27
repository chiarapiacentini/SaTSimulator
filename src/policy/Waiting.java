package policy;

import main.Parameters;
import main.Position;
import main.UAV;
import main.Velocity;

public class Waiting extends PolicyUAV {

    double radius;
    Position origin;
    public Waiting(UAV u) {
        super(u);
        origin = u.getCurrent();
        radius = Parameters.defaultInnerRadius;
    }

    public Velocity update() {
        double angle = new Velocity(0, -Parameters.uavVelocity).angleTo(uav.getCurrent(), origin);
        uav.setCameraFacing(angle);
        Velocity velocity = uav.getCurrentVelocity().rotate(uav.getCurrentVelocity().speed()/radius);
        return velocity;
    }
}