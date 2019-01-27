package policy;

import main.UAV;
import main.Velocity;

abstract public class PolicyUAV {
    boolean holding = false;
    UAV uav;

    public PolicyUAV(UAV u) {
        uav = u;
    }

    abstract public Velocity update();

    public void setHolding() {
        holding = true;
    }
}
