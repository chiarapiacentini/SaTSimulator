package detection;

import main.Position;
import main.State;
import main.UAV;
import patterns.SearchPattern;

import java.util.HashMap;
import java.util.List;

public abstract class SolverManager {

    protected List<SearchPattern> candidateSearchPattern;
    protected List<UAV> uavs;
    public static int probNum = 0;
    protected String folder = "planning/";

    protected HashMap<List<Position>,String> cityNameMapping;
    protected HashMap<String,SearchPattern> nameSearchPatternMapping;
    protected double maxExpectedTime = 0;
    SolverManager(List<SearchPattern> candidate, List<UAV> uavs) {
        candidateSearchPattern = candidate;
        cityNameMapping = new HashMap<>();
        nameSearchPatternMapping = new HashMap<>();
        this.uavs = uavs;
        probNum++;
        System.out.println("Search patterns candidates: " + candidateSearchPattern.size());
        int idC = 0;
        for (SearchPattern sp : candidateSearchPattern) {
            for (List<Position> r : sp.getCompatibleDestinations()) {
                if (!cityNameMapping.containsKey(r)) {
                    cityNameMapping.put(r, "v" + idC++);
                }
            }
            nameSearchPatternMapping.put(sp.toString(), sp);
            double expectedTime = (sp.getMaxT()+sp.getMinT())*0.5;
            if (expectedTime > maxExpectedTime)
                maxExpectedTime = expectedTime;
        }
    }


    abstract HashMap<String,HashMap<Integer,List<SearchPattern>>> getPlan();
}
