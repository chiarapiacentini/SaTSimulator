package patterns;

import main.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by chiarapiacentini on 2017-04-15.
 */

public class SpiralSearchPattern extends SearchPattern {

    Double radius;
    Double innerRadius;
    Position entry;
    Position exit;

    public SpiralSearchPattern(Position o, UAVGrid g) {
        super(o, g);
        radius = Parameters.defaultRadius;
        innerRadius = Parameters.defaultInnerRadius;
        duration = Parameters.defaultDuration;
        internalPoints = grid.getGrid().stream().filter(p -> origin.distance(p) < radius+2*Parameters.gridXSize).collect(Collectors.toList());
        entry = null;
        exit = null;
    }

    public SpiralSearchPattern(Position o, UAVGrid g, int nMCS) {
        super(o, g);
        nParticles = nMCS;
        radius = Parameters.defaultRadius;
        innerRadius = Parameters.defaultInnerRadius;
        duration = Parameters.defaultDuration;
        internalPoints = grid.getGrid().stream().filter(p -> origin.distance(p) < radius+2*Parameters.gridXSize).collect(Collectors.toList());
        entry = null;
        exit = null;
    }

    private static SpiralSearchPattern calculateSearchPattern(Position o, UAVGrid grid, Position lkp, List<List<Position>> paths, HashMap<Position, Integer> positionsSimulated) {
        SpiralSearchPattern candidate = new SpiralSearchPattern(o, grid, positionsSimulated.get(o));
        candidate.calculateGamma(grid);
        candidate.calculateConcealment(grid);
        candidate.calculateTimeWindow(grid, lkp, paths);
        return candidate;
    }

    public static List<SpiralSearchPattern> calculateSearchPatterns(HashMap<Position, Integer> positionsSimulated,
                                                                    UAVGrid grid,HashSet<Position> alreadyInHashSet, Position lkp, List<List<Position>> paths) {
        List<SpiralSearchPattern> toReturn = new ArrayList<SpiralSearchPattern>();
        List<Position> sorted = positionsSimulated.entrySet().stream()
                .sorted(Map.Entry.<Position, Integer>comparingByValue().reversed()).map(Map.Entry::getKey).collect(Collectors.toList());
        for (Position c : sorted) {
            if (!alreadyInHashSet.contains(c)) {
                // if (Parameters.debug) System.out.println(c + " " + positionsSimulated.get(c));
                SpiralSearchPattern candidate = calculateSearchPattern(c, grid, lkp, paths, positionsSimulated);
                if (candidate.getMaxT() > Parameters.timeBlind) {
                    // TODO delete condition on number of search patterns
                    toReturn.add(candidate);
                    alreadyInHashSet.addAll(candidate.getInternalPoints());
                }
            }
        }
        //if (Parameters.debug) System.out.println("found " + toReturn.size() + " candidates");
        return toReturn;
    }

    // TODO fill here
    void calculateConcealment(UAVGrid grid) {
        // calculate average concealment level
    }

    // TODO fill here
    void calculateGamma(UAVGrid grid) {
        // calculate average concealment level
        gamma = 0.0;
        for (Position p : internalPoints){
            gamma += UAV.calculateGamma(UAV.getTerrainEffect(grid.getTerrain(p)));
        }
        gamma = gamma/internalPoints.size();
    }

    private void calculateTimeWindow(UAVGrid grid, Position lkp, List<List<Position>> paths){
        double entryDistance = grid.getGrid().size();
        double exitDistance = 0;
        for (List<Position> road : paths){
            // we are considering road "road"
            // check all position if they intesect the path
            double idCell = 0;
            boolean hasEntered = false;
            for (Position cell : road){
                //System.out.println(cell.distance(origin) + " " + radius);
                idCell += 1./State.getGrid().getTerrainEffectOnSpeed(cell.getTerrain());
                if (cell.distance(origin) < radius){
                    Position destination = road.get(road.size()-1);
                    if (compatibleDestinations.indexOf(road)==-1){
                        compatibleDestinations.add(road);
                    }
                    if (!hasEntered) {
                        //System.out.print("\t entry point at " + idCell);
                        hasEntered = true;
                        if (idCell < entryDistance) {
                            entryDistance = idCell;
                            entry = cell;

                        }
                    }else{
                        if (idCell > exitDistance){
                            exitDistance = idCell;
                            exit = cell;
                        }
                    }
                }
            }
        }
        //System.out.println("\t entry point at " + entryDistance + " exit " + exitDistance);
        minT = Math.max(Math.floor(entryDistance * 0.5 * (Parameters.gridXSize+ Parameters.gridYSize) / Parameters.targetVelocityMax),
            Parameters.timeBlind);
        maxT =  Math.max(Math.ceil(exitDistance * 0.5 * (Parameters.gridXSize+ Parameters.gridYSize) / Parameters.targetVelocityMin)-duration,minT+1);
//        maxT = Math.ceil(exitDistance * 0.5 * (Parameters.gridXSize+ Parameters.gridYSize) / Parameters.targetVelocityMin)-duration;

        //System.out.println("\tsearchPattern " +  name  + " entry point at " + entryDistance + " exit " + exitDistance +
        //        " time-window: " + minT + " - " + maxT + " " + (maxT-minT));
    }

    public Double getRadius() {
        return radius;
    }
    public Double getInnerRadius() {
        return innerRadius;
    }
}
