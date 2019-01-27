package main;

import java.util.Vector;

public abstract class RoadFinder {

    double alpha;


    RoadFinder() {
        alpha = 0;
    }

    abstract Vector<Position> findRoad(Position i, Position f, double concealment);

    Vector<Position> findRoad(double x, double y, double fx, double fy, double concealment) {
        return findRoad(new Position(x, y), new Position(x, y), concealment);
    }

}
