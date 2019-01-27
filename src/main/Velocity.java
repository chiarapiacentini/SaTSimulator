package main;

/**
 * Created by chiarapiacentini on 2017-04-18.
 * from original sat simulator
 */
public class Velocity {
    // m/s
    private double dx;
    private double dy;

    public Velocity(double x, double y) {
        dx = x;
        dy = y;
    }

    public Velocity(Velocity v) {
        dx = v.dx;
        dy = v.dy;
    }

    public double getTheta() {
        if (dx == 0) {
            return dy > 0 ? 90.0 : (dy < 0 ? 270.0 : 0);
        }
        double angle = Math.toDegrees(Math.atan(dy / dx));
        if (dx < 0) {
            return angle + 180;
        }
        return angle;
    }

    public double speed() {
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getVx() {
        return dx;
    }

    public double getVy() {
        return dy;
    }

    public void scaleTo(double spd) {
        double scl = spd / speed();
        dx = dx * scl;
        dy = dy * scl;
    }

    public static Velocity headTowards(Position from, Position to, double speed) {
        double x = to.getX() - from.getX();
        double y = to.getY() - from.getY();
        Velocity v = new Velocity(x, y);

        v.scaleTo(speed);
        return v;
    }

    public Velocity rotate(double angle) {
        double nx = dx * Math.cos(angle) - dy * Math.sin(angle);
        double ny = dx * Math.sin(angle) + dy * Math.cos(angle);
        return new Velocity(nx, ny);
    }

    public double angleTo(Position p1, Position p2) {

        double xx = p1.getX()-p2.getX();
        double yy = p1.getY()-p2.getY();

        return angleTo(xx, yy);
    }

    public double angleTo(Velocity v) {
        return angleTo(v.getVx(), v.getVy());
    }

    public double angleTo(double x, double y) {
        double s = speed();
        double vx = dx / s;
        double vy = dy / s;
        double ss = Math.sqrt(x * x + y * y);
        x /= ss;
        y /= ss;
        double ang = Math.toDegrees(Math.acos(vx * x + vy * y));
        return (vx * y - vy * x < 0) ? -ang : ang;
    }

    public String toString() {
        return "dx : " + dx + " dy " + dy;
    }
}
