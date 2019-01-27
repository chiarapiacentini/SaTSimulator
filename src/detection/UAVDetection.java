package detection;

import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import main.*;
import patterns.*;
import main.UAVGrid.*;

import edu.uci.ics.jung.graph.DirectedGraph;
import patterns.SpiralSearchPattern;

// this is an abstract class that implements the detection method of the UAV
abstract public class UAVDetection {
	
    protected DirectedGraph<Node,Edge> rng;
	protected UAVGrid grid;
    protected List<List<Position>> paths;
    protected  Position lkp;
    protected List<SearchPattern> candidates;
    protected UAV uav;
	public  UAVDetection(){

	}
	// TODO change into private when finished implementation (call using planning class)
	public void setupGrid(UAVGrid g, Position l, UAV u){
		grid = g;
		rng = grid.rng;
        lkp = l;
        uav = u;
        paths = new ArrayList<>();
        List<Position> candidateLKP = new ArrayList<>();
        candidateLKP.add(lkp);
        HashSet<Position> visited = new HashSet<>();
        visited.add(lkp);
        int iT = 0;
        Position cl = null;
        List<List<Position>> tmpPaths = new ArrayList<List<Position>>();
        while (candidateLKP.size()>0 && iT < 10) {
            cl = candidateLKP.get(0);
            candidateLKP.remove(l);
            tmpPaths = grid.findPathsToCities(cl, 0);
            if (tmpPaths.size() == 0) {
                if (Parameters.debug) System.out.println("empty paths, try with neighbour lkps");
                List<Position> neighbours = grid.getNeighbour(l);
                for (Position n : neighbours) {
                    if (!visited.contains(n)) {
                        candidateLKP.add(n);
                        visited.add(n);
                    }
                }
                ++iT;
            } else break;
        }
        paths.addAll(tmpPaths);
        for (double i = 0; i <= 1; i += Parameters.uavEvading){
            paths.addAll(grid.findPathsToCities(cl, i));
        }
	}

	public List<List<Position>> getPaths(){return paths; }
	// toChange to plan
	public abstract HashMap<String,HashMap<Integer,List<SearchPattern>>> detect(UAVGrid grid);

    public List<SearchPattern> createCandidateSearchPatterns(HashMap<Integer,HashMap<Position, Integer>>  simulatedParticles, Position lkp, int nMax){
		HashSet<Position> alreadyInHashSet = new HashSet<>();
        List<SearchPattern> toReturn = new ArrayList<>();
        // select two search pattern for each branch
        int i = 0;
        for (HashMap<Position,Integer> positionAtTimeT : simulatedParticles.values()){
            List<SearchPattern> candidatesT = new ArrayList<>();
            candidatesT.addAll(SpiralSearchPattern.calculateSearchPatterns(positionAtTimeT, grid, alreadyInHashSet, lkp, paths));
            if (Parameters.filterCandidates) candidatesT = filterCandidates(candidatesT);
            candidatesT = filterCandidatesKBest(candidatesT,(int) Math.ceil((double)nMax/simulatedParticles.size()));
            toReturn.addAll(candidatesT);
            //if (Parameters.debug) System.out.println("time slice = " + i++ + toReturn.size());
        }
        return toReturn;
    }

    protected List<SearchPattern> filterCandidates(List<SearchPattern> candidates){
        List<SearchPattern> toReturn = new ArrayList<>();
        // select two search pattern for each branch

        for (SearchPattern c : candidates){
            if (Math.abs(uav.getEstimatedTargetVelocity().angleTo(c.getOrigin(),uav.getCurrent())) < Parameters.filterAngle){
                toReturn.add(c);
            }
        }
        return toReturn;
    }


    protected List<SearchPattern> filterCandidates(List<SearchPattern> candidates, Velocity direction, double filterAngle){
        List<SearchPattern> toReturn = new ArrayList<>();
        // select two search pattern for each branch

        for (SearchPattern c : candidates){
            if (Math.abs(direction.angleTo(c.getOrigin(),uav.getCurrent())) < filterAngle){
                toReturn.add(c);
            }
        }
        return toReturn;
    }

    protected List<SearchPattern> filterCandidatesKBest(List<SearchPattern> candidates, int k){
        return  candidates.stream().sorted(Comparator.comparing(SearchPattern::getNParticles).reversed()).limit(k).collect(Collectors.toList());
    }
    public List<SearchPattern> getCandidates(){
        return candidates;
    }
}
