package map;

/* 
 * KMeans.java ; Cluster.java ; Point.java
 *
 * Solution implemented by DataOnFocus
 * www.dataonfocus.com
 * 2015
 *
*/

import main.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {

    //Number of Clusters. This metric should be related to the number of points
    private int NUM_CLUSTERS;


    private List<Position> points;
    private List<Cluster> clusters;

    public KMeans(List<Position> p, int n) {
        this.points = p;
        this.clusters = new ArrayList<Cluster>();
        NUM_CLUSTERS = n;
    }

    public static void main(String[] args) {
        List<Position> pos = new ArrayList<Position>();
        pos.add(new Position(3, 3));
        KMeans kmeans = new KMeans(pos, 3);
        kmeans.init();
        kmeans.calculate();
    }

    //Initializes the process
    public void init() {
        //Create Points

        //Create Clusters
        //Set Random Centroids
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            Cluster cluster = new Cluster(i);
            Random rand = new Random();
            int n = rand.nextInt(points.size());
            Position centroid = points.get(n);
            cluster.setCentroid(centroid);
            clusters.add(cluster);
        }

        //Print Initial state
        //plotClusters();
    }

    /*
    private void plotClusters() {
    	for (int i = 0; i < clusters.size(); i++) {
    		Cluster c = clusters.get(i);
    		c.plotCluster();
    	}
    }
	*/
    public List<Position> getPoints() {
        return points;
    }

    //The process to calculate the K Means, with iterating method.
    public void calculate() {
        boolean finish = false;

        // Add in new data, one at a time, recalculating centroids with each new one. 
        while (!finish) {
            //Clear cluster state
            clearClusters();

            List<Position> lastCentroids = getCentroids();

            //Assign points to the closer cluster
            assignCluster();

            //Calculate new centroids.
            calculateCentroids();


            List<Position> currentCentroids = getCentroids();

            //Calculates total distance between new and old Centroids
            double distance = 0;
            for (int i = 0; i < NUM_CLUSTERS; i++) {
                distance += lastCentroids.get(i).distance(currentCentroids.get(i));
            }
            //System.out.println("#################");
            //System.out.println("Iteration: " + iteration);
            //System.out.println("Centroid distances: " + distance);
            //plotClusters();

            if (distance == 0) {
                finish = true;
            }
        }
    }

    private void clearClusters() {
        for (Cluster cluster : clusters) {
            cluster.clear();
        }
    }

    private List<Position> getCentroids() {
        List<Position> centroids = new ArrayList<Position>(NUM_CLUSTERS);
        for (Cluster cluster : clusters) {
            Position aux = cluster.getCentroid();
            Position point = new Position(aux.getLatitude(), aux.getLongitude());
            centroids.add(point);
        }
        return centroids;
    }

    private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;

        for (Position point : points) {
            min = max;
            for (int i = 0; i < NUM_CLUSTERS; i++) {
                Cluster c = clusters.get(i);
                distance = point.distance(c.getCentroid());
                if (distance < min) {
                    min = distance;
                    cluster = i;
                    point.setCity(i);
                }
            }
            //point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
        }
    }

    private void calculateCentroids() {
        for (Cluster cluster : clusters) {
            double sumX = 0;
            double sumY = 0;
            List<Position> list = cluster.getPoints();
            int n_points = list.size();
            for (Position point : list) {
                sumX += point.getLatitude();
                sumY += point.getLongitude();
            }

            @SuppressWarnings("unused")
            Position centroid = cluster.getCentroid();
            if (n_points >= 0) {
                double newX = sumX / n_points;
                double newY = sumY / n_points;
                centroid = new Position(newX, newY);
            }
        }
    }
}