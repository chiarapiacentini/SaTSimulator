package detection;

import main.Parameters;
import main.Position;
import main.State;
import main.UAV;
import patterns.SearchPattern;

import java.util.*;

public class GreedyAlgorithm extends SolverManager {

    GreedyAlgorithm(List<SearchPattern> candidate){
        super(candidate, State.getUAVs());
    }

    GreedyAlgorithm(List<SearchPattern> candidate, List<UAV> uavs) {
        super(candidate, uavs);
    }

    HashMap<UAV, ArrayList<SearchPattern>> sequence() {
        //System.out.println("greedy solution");
        double time = 0;
        HashMap<UAV, ArrayList<SearchPattern>> assignments = new HashMap<>();
        int nDestination = cityNameMapping.size();
        HashMap<List<Position>, Double> pD = new HashMap<>();
        for (List<Position> d : cityNameMapping.keySet()) {
            pD.put(d, 1. / nDestination);
        }
        double totalP = 0;
        double totalF = 0;

        for (UAV u : uavs) {
            assignments.put(u, new ArrayList<>());
        }

        int iterationID = 0;

        HashMap<SearchPattern, Double> increases = new HashMap<>();
        for (SearchPattern sigma : candidateSearchPattern) {
            double pS = sigma.getGamma();
            double pC = 0;
            for (List<Position> d : sigma.getCompatibleDestinations()) {
                pC += pD.get(d);
            }
            pS *= pC;
            double increase = pS * (1 - Parameters.weight * (sigma.getMaxT() + sigma.getMinT()) * 0.5 / maxExpectedTime)
                    * (1 - totalP);
            increases.put(sigma, increase);
        }

        Comparator<SearchPattern> spComparator = new Comparator<SearchPattern>() {
            @Override
            public int compare(SearchPattern o1, SearchPattern o2) {
                return new Double(increases.get(o2) * 100000 - increases.get(o1) * 100000).intValue();
            }
        };

        PriorityQueue<SearchPattern> queue = new PriorityQueue<>(spComparator);
        for (SearchPattern sp : candidateSearchPattern) {
            queue.add(sp);
        }

        HashSet<SearchPattern> visited = new HashSet<>();
        while (true) {
            SearchPattern bestS;
            UAV bestO;
            double bestPs = 0;
            ++iterationID;
            double epsilon = 0.000000001;
            SearchPattern sigma = queue.peek();
            //System.out.println(iterationID + " selecting " + sigma.toString() + " with increase " + increases.get(sigma) + " from " + queue.size());
            double max = increases.get(sigma);
            if (max >= epsilon) {
                double pS = sigma.getGamma();
                double pC = 0;
                for (List<Position> d : sigma.getCompatibleDestinations()) {
                    pC += pD.get(d);
                }
                pS *= pC;
                double increase = pS * (1 - Parameters.weight * (sigma.getMaxT() + sigma.getMinT()) * 0.5 /
                        maxExpectedTime) * (1 - totalP);
                increases.put(sigma, increase);
                //System.out.println("\t\t\tnew increase " + increase);
                queue.remove(sigma);
                queue.add(sigma);
                boolean alreadyVisited = visited.contains(sigma);
                //System.out.println("\t\tis already visited " + alreadyVisited);
                if (alreadyVisited) {
                    UAV o = getAssignment(sigma, assignments);
                    if (o != null) {
                        bestS = sigma;
                        bestO = o;
                        bestPs = pS;
                        ArrayList<SearchPattern> newList = assignments.get(bestO);
                        newList.add(bestS);
                        //System.out.println("\tassigned to " + o.getUavName());
                        assignments.put(bestO, newList);
                        for (List<Position> d : cityNameMapping.keySet()) {
                            if (bestS.getCompatibleDestinations().contains(d)) {
                                double value = pD.get(d) * (1 - bestS.getGamma()) / (1 - bestPs);
                                pD.put(d, value);
                            } else {
                                double value = pD.get(d) / (1 - bestPs);
                                pD.put(d, value);
                            }
                        }
                        totalF = totalF + bestPs * (1 - Parameters.weight * (sigma.getMaxT() + sigma.getMinT()) * 0.5 /
                                maxExpectedTime) * (1 - totalP);
                        totalP = totalP + bestPs * (1 - totalP);
                        visited.clear();

                        pS = bestS.getGamma();
                        pC = 0;
                        for (List<Position> d : bestS.getCompatibleDestinations()) {
                            pC += pD.get(d);
                        }
                        pS *= pC;
                        double updateIncrease = pS * (1 - Parameters.weight * (bestS.getMaxT() + bestS.getMinT()) * 0.5 /
                                maxExpectedTime) * (1 - totalP);
                        increases.put(bestS, updateIncrease);
                        queue.remove(bestS);
                        queue.add(bestS);

                    } else {
                        increases.put(sigma, 0.);
                        queue.remove(sigma);
                        queue.add(sigma);
                    }
                }
                //System.out.println("\t\tadding " + sigma.toString() + " to already visited, next sp " + queue.peek() + " from " + queue.size());
                visited.add(sigma);
            } else {
                break;
            }
        }
        if (Parameters.debug) {
            for (UAV u : uavs) {
                System.out.println("uav " + u.getUavName());
                for (SearchPattern sp : assignments.get(u)) {
                    System.out.println("\t" + sp.toString());
                }
            }
        }
        System.out.println("Cost: " + totalF);
        return assignments;
    }

    UAV getAssignment(SearchPattern sp, HashMap<UAV,ArrayList<SearchPattern>> sequences){
        for (UAV o : uavs){
            ArrayList<SearchPattern> tmpList = new ArrayList<>(sequences.get(o));
            tmpList.add(sp);
            if (checkTC(tmpList, o))
                return o;
        }
        return null;
    }

    boolean checkTC(ArrayList<SearchPattern> solutionSequence, UAV uav){
        SearchPattern previousSP = null;
        double time = 0;
        for (SearchPattern sp : solutionSequence){
            double travelTime = time + distanceSP(previousSP,sp,uav);
            double tmpTime = Math.max(sp.getMinT(),travelTime);
            //System.out.println("\t\t\t" + sp.toString() + " " + tmpTime + " " + sp.getMinT() + " " + sp.getMaxT());
            if (tmpTime > sp.getMaxT())
                return false;
            else {
                time = tmpTime;
                previousSP = sp;
            }
        }
        return true;
    }

    double distanceSP(SearchPattern sp1, SearchPattern sp2, UAV u){

        if (sp1 ==null){
            return  Math.ceil(u.getCurrent().distance(sp2.getOrigin())/Parameters.uavVelocity);
        }
        return Math.ceil(sp1.getOrigin().distance(sp2.getOrigin())/ Parameters.uavVelocity)+ sp1.getDuration();
    }
    public HashMap<String, HashMap<Integer, List<SearchPattern>>> getPlan() {
        HashMap<String,HashMap<Integer,List<SearchPattern>>> timedPlan = new  HashMap<>();
        HashMap<UAV,ArrayList<SearchPattern>> sequence = sequence();
        for (UAV u : sequence.keySet()){
            HashMap<Integer,List<SearchPattern>> plan = new HashMap<>();
            int time = State.getTime() + 1;
            int startTime = 0;
            SearchPattern previous = null;
            //System.out.println("executing " + u.getUavName());
            for (SearchPattern sp : sequence.get(u)){
                List<SearchPattern> tmpList = new ArrayList<>();
                tmpList.add(sp);
                plan.put(time,tmpList);
                startTime =  new Double(Math.max(startTime+distanceSP(previous,sp,u),sp.getMinT())).intValue();
                time = startTime + State.getTime() + new Double(sp.getDuration()).intValue();
                //System.out.println(Math.max(distanceSP(previous,sp,u),sp.getMinT()) + " " + startTime + " " +time);
                if (Parameters.debug )
                {
                    System.out.println((startTime + sp.getDuration()) + " " + sp.toString() + " " + u.getUavName() );
                }
                previous = sp;
            }
            timedPlan.put(u.getUavName(),plan);
        }
        return timedPlan;
    }

}