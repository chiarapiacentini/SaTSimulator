package map;

import main.Pair;
import main.Parameters;
import main.Position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapOSM implements Serializable {


    String nameFile;
    ArrayList<Position> points;
    ArrayList<Position> grid;

    // definition of min and max to setup a grid
    double maxX;
    double minX;
    double maxY;
    double minY;

    public MapOSM(String nf) {
        if (Parameters.debug) System.out.println("\t...reading " + nf);
        nameFile = nf;
        Parser p = new Parser(nameFile);
        points = p.getPoints();
        //findMinMax();
        //setTerrainType(points);
        //setupGrid();
    }


    // obsolete, try to determine terrain from edges
    public void setTerrainTypeEdges(List<Pair<Position, Position>> roads) {
        int size = roads.size();
        int counter = 0;
        for (Pair<Position, Position> p : roads) {
            //System.out.println(size);
            if (size % (size / 100) == 0) {
                System.out.println(counter);
                counter++;
            }
            Position.Terrain t = getTerrainType(p.first, main.Parameters.distanceTerrain, points);
            p.first.setTerrain(t);
        }
    }

    static public List<Position> getPositionDistance(Position pp, double distance, ArrayList<Position> points) {
        return points.stream().filter(x -> x.distance(pp) < distance).collect(Collectors.toList());
    }

    static public Position.Terrain getTerrainType(Position pp, double distance, ArrayList<Position> points) {
        List<Position> filtered = getPositionDistance(pp, distance, points);
        HashMap<Position.Terrain, Double> counter = new HashMap<Position.Terrain, Double>();
        for (Position.Terrain t : Position.Terrain.values()) {
            counter.put(t, new Double(0));
        }
        for (Position p : filtered) {
            counter.put(p.getTerrain(), counter.get(p.getTerrain()) + 2*( Math.exp(-Math.pow(pp.distance(p)/Parameters.distanceTerrain,2)/2)));
        }
        double maxSize = 0;
        Position.Terrain maxTerrain = Position.Terrain.UNKNOWN;
        for (Position.Terrain t : Position.Terrain.values()) {
            double size = counter.get(t);
            //System.out.println(t + " " + size);
            if (t == Position.Terrain.CITY && size >= 1) {
                maxSize = size;
                maxTerrain = t;
                break;
            }
            if (size > maxSize) {
                maxSize = size;
                maxTerrain = t;
            }
        }
        return maxTerrain;
    }

    public Position.Terrain getTerrainType(Position pp, double distance) {
        return getTerrainType(pp, distance, points);
    }

    public ArrayList<Position> getPoints() {
        return points;
    }


    public static void main(String args[]) {
        long s = System.currentTimeMillis();
        MapOSM map = new MapOSM("resources/scotland-latest.osm.pbf");
        long e = System.currentTimeMillis();
        System.out.println("in " + ((e - s) / 1000) + " s");
        Position pp = new Position(56.1337538, -3.9617041);
        System.out.println(map.getTerrainType(pp, 1000));
    }
}
