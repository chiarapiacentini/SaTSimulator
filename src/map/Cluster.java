package map;

import main.Position;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    public List<Position> points;
    public Position centroid;
    public int id;

    //Creates a new Cluster
    public Cluster(int id) {
        this.id = id;
        this.points = new ArrayList<Position>();
        this.centroid = null;
    }

    public List<Position> getPoints() {
        return points;
    }

    public void addPoint(Position point) {
        points.add(point);
    }

    public void setPoints(List<Position> points) {
        this.points = points;
    }

    public Position getCentroid() {
        return centroid;
    }

    public void setCentroid(Position centroid) {
        this.centroid = centroid;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        points.clear();
    }

    public void plotCluster() {
        System.out.println("[Cluster: " + id + "]");
        System.out.println("[Centroid: " + centroid + "]");
        System.out.println("[Points: \n");
        for (Position p : points) {
            System.out.println(p);
        }
        System.out.println("]");
    }

}