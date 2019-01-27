package main;

import java.util.Vector;

public class Target extends Movable {

    private double startX;
    private double startY;
    private double destinationX;
    private double destinationY;
    private boolean isEvading;
    Vector<Position> route;
    RoadFinder roadFinder;
    Velocity velocity;
    int suspicion;
    //int ticker;


    public Target(double _x, double _y, double _ix, double _iy, double _fx, double _fy, RoadFinder r) {
        super(_x, _y);
        //ticker = 0;
        route = new Vector<Position>();
        startX = _ix;
        startY = _iy;
        destinationX = _fx;
        destinationY = _fy;
        roadFinder = r;
        velocity = new Velocity(0,0);
        isEvading = false;
        suspicion = 0;
        calculateRoute(0);
    }

    public void addStart(double _x, double _y) {
        startX = _x;
        startY = _y;
    }

    public void addDestination(double _x, double _y) {
        destinationX = _x;
        destinationY = _y;
    }


    private void checkUAV(){
        for (UAV u : State.getUAVs()){
            if (getPosition().distance(u.getPosition()) <= Parameters.radiusDistance+10) {
                double rnd = State.getRand();
                if (rnd < Parameters.targetEvasivness){
                    suspicion++;
                    //System.out.println(suspectLevel);
                }
            }
        }
        if (!isEvading &&  suspicion > Parameters.targetSuspicionLevel){
            isEvading = true;
            //System.out.println(getPosition().getX(),getPosition().getY());
            calculateRoute(getPosition().getLatitude(),getPosition().getLongitude(),1);
            if (Parameters.debug) System.out.println("Target: became evasive at time " + State.getTime());
        }
    }
    public void tick(int ticker) {
        //x+=0.001;
        //y+=0.001;
        if (route != null) {
            Position now = new Position(latitude, longitude);
            Position increment = getDistance(now); // TODO check this
            if (increment.equals(now)){
                System.out.println("Target: arrived at time " + State.getTime());
                State.targetArrived = true;
                //System.exit(0);
            }
            checkUAV();
            velocity = new Velocity(increment.getX() - now.getX() , increment.getY() - now.getY());
            latitude = increment.getLatitude();
            longitude = increment.getLongitude();

        }
    }

    public Velocity getVelocity(){
        return velocity;
    }

    public Position getDistance(Position i) {
        // TODO check this
        // check if we traveled the minimum distance applied
        if (route.size() == 0)
            return i;
        Position next = route.firstElement();
        double distance = isEvading ? Parameters.targetVelocityMax : Parameters.targetVelocity; // distance in m
        // add grid here and scale distance per speed
        distance *= getTerrainEffectOnSpeed(State.getGrid().getTerrain(i));
        double dist = i.distance(next); // distance in m after 1 sec (50 ms)
        while (dist < distance) {
            route.remove(0);
            if (route.size() == 0)
                return next;
            Position nnext = route.firstElement();
            dist += next.distance(nnext);
            next = nnext;
        }
        //System.out.println(dist);
        Velocity v = Velocity.headTowards(i, next, distance); // increment in m (?)
        Position newPosition = Position.move(i,v);
        return newPosition;
    }

    void calculateRoute(double concealment) {
        try {
            route = roadFinder.findRoad(new Position(startX, startY), new Position(destinationX, destinationY), concealment);
            route.remove(0);
        } catch (Exception e) {
            route = new Vector<Position>();
        }
    }

    void calculateRoute(double startX, double startY, double concealment) {
        try {
            route = roadFinder.findRoad(new Position(startX, startY), new Position(destinationX, destinationY), concealment);
            route.remove(0);
        } catch (Exception e) {
            route = new Vector<Position>();
        }
    }

    public double getStartX(){
        return startX;
    }

    public double getStartY(){
        return startY;
    }

    public double getDestinationX(){
        return destinationX;
    }
    public double getDestinationY(){
        return destinationY;
    }
    public Position getPosition() {
        return new Position(latitude, longitude);
    }

    static public double getTerrainEffectOnSpeed(Position.Terrain t){
        switch(t) {
            case URBAN:
                return 0.6;
            case SUBURBAN:
                return 0.8;
            case CITY:
                return 0.6;
        }
        return 1;
    }

    static public double getTerrainEffectOnConceleament(Position.Terrain t){
        switch(t) {
            case URBAN:
                return 0.9;
            case SUBURBAN:
                return 0.5;
            case CITY:
                return 0.9;
        }
        return 0.1;
    }

    boolean getIsEvading(){
        return isEvading;
    }

    Vector<Position> getRoute(){
        return route;
    }
}
