package router;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FastestWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import main.Position;
import main.State;
import main.Target;


public class MyGraphHopper extends GraphHopper {

    double alpha;


    public void setAlpha(double a) {
        alpha = a;
    }

    class AlphaWeighting implements Weighting {

        FlagEncoder encoder;
        @SuppressWarnings("unused")
        private final Graph graph;
        private final FastestWeighting superWeighting;

        AlphaWeighting(FlagEncoder e, FastestWeighting sw, Graph g) {
            encoder = e;
            superWeighting = sw;
            graph = g;
        }

        @Override
        public double getMinWeight(double distance) {
            // TODO Auto-generated method stub
            return superWeighting.getMinWeight(distance);
        }

        @SuppressWarnings("unused")
        @Override
        public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {

            // TODO Auto-generated method stub
            PointList points = edgeState.fetchWayGeometry(3);
            double sum = 0;
            // double lat = na.getLatitude(baseNode);
            //double lon = na.getLongitude(baseNode);
            //sum = MyGraphHopper.valueFor(terrain.terrainType(Position.fromLatLong(lon, lat)));

            for (GHPoint3D p : points) {
                double conc = 0;
                //System.out.println(Target.getTerrainEffectOnConceleament(State.getGrid().getTerrain(new Position(p.getLat(),p.getLon()))));
                if (State.getGrid()!=null){
                    conc = Target.getTerrainEffectOnConceleament(State.getGrid().getTerrain(new Position(p.getLat(),p.getLon())));
                }
                sum += conc;
            }
            //System.out.println(" ");
            double average = 0;
            if (points.size() != 0)
                average = sum / points.size();
            double previous = superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
            double newweight = (1 - alpha * average);
            //System.out.println(previous + " " + newweight + " " + sum);
            return previous * newweight;
        }

        @Override
        public FlagEncoder getFlagEncoder() {
            // TODO Auto-generated method stub
            return encoder;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return "alpha";
        }

        @Override
        public boolean matches(HintsMap map) {
            // TODO Auto-generated method stub
            return true;
        }

    }

    @Override
    public Weighting createWeighting(HintsMap wMap, FlagEncoder encoder) {
        String weighting = wMap.getWeighting();
        if ("alpha".equalsIgnoreCase(weighting)) {
            AlphaWeighting w = new AlphaWeighting(encoder, new FastestWeighting(encoder, wMap), this.getGraphHopperStorage());
            return w;
        } else {
            return super.createWeighting(wMap, encoder);
        }
    }

}

