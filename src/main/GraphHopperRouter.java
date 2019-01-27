package main;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import router.MyGraphHopper;

public class GraphHopperRouter extends RoadFinder {
    MyGraphHopper hopper;
    ArrayList<main.Pair<Position, Position>> edges;

    @SuppressWarnings("deprecation")
    public GraphHopperRouter(String mapOSI, String dirOSI) {
        super();
        try {
            hopper = (MyGraphHopper) new MyGraphHopper().forServer();
            hopper.setAlpha(alpha);
            hopper.setOSMFile(mapOSI);
            hopper.setGraphHopperLocation(dirOSI);
            hopper.setCHEnable(false);
            hopper.setEncodingManager(new EncodingManager("car"));
            hopper.importOrLoad();
            exploreGraph();
        } catch (Exception e) {
            System.out.println("no graphhopper");
            e.printStackTrace();
        }
    }

    @Override
    Vector<Position> findRoad(Position i, Position f , double concealment) {
        System.out.println("\tfinding route between " + i + " and " + f);
        hopper.setAlpha(concealment);
        Vector<Position> route = new Vector<Position>();
        GHRequest req = new GHRequest(i.getLatitude(), i.getLongitude(), f.getLatitude(), f.getLongitude()).
                setWeighting("alpha").
                setVehicle("car").
                setLocale(Locale.US);
        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            System.out.println("error, route not found");
        }
        PathWrapper path = rsp.getBest();
        InstructionList il = path.getInstructions();
        // iterate over every turn instruction
        for (Instruction instruction : il) {

            PointList points = instruction.getPoints();
            for (GHPoint3D p : points) {
                Position pos = new Position(p.getLat(), p.getLon());
                route.add(pos);
            }
        }
        return route;
    }

    void exploreGraph() {
        edges = new ArrayList<main.Pair<Position, Position>>();


        //TODO add selection
        FlagEncoder encoder = hopper.getEncodingManager().getEncoder("car");
        //EdgeExplorer outEdgeExplorer = hopper.getGraphHopperStorage().createEdgeExplorer(new DefaultEdgeFilter(encoder, false, true));

        AllEdgesIterator it = hopper.getGraphHopperStorage().getAllEdges();
        int edgeCount = 0;
        while (it.next()) {
            double lat = hopper.getGraphHopperStorage().getNodeAccess().getLatitude(it.getBaseNode());
            double lon = hopper.getGraphHopperStorage().getNodeAccess().getLongitude(it.getBaseNode());
            double latadj = hopper.getGraphHopperStorage().getNodeAccess().getLatitude(it.getAdjNode());
            double lonadj = hopper.getGraphHopperStorage().getNodeAccess().getLongitude(it.getAdjNode());
            if (encoder.getSpeed(it.getFlags()) < 35)
                continue;
            edges.add(new Pair<Position, Position>(new Position(lat, lon), new Position(latadj, lonadj)));
            edgeCount++;

        }
        if (main.Parameters.debug)
            System.out.println("\t\tfound " + edgeCount + " edges and " + edges.size() + " points.");
    }

    ArrayList<main.Pair<Position, Position>> getEdgesAsPairs() {
        return edges;
    }
}
