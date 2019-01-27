package detection;

import main.*;
import patterns.SearchPattern;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Planning extends UAVDetection {

	@Override
	public HashMap<String,HashMap<Integer,List<SearchPattern>>> detect(UAVGrid grid) {
        MCSimulation mcs = new MCSimulation(paths);
        HashMap<Integer, HashMap<Position, Integer>> simulatedParticles = mcs.simulate();
        candidates = createCandidateSearchPatterns(simulatedParticles, lkp, Parameters.filterNumber);
        candidates = filterCandidatesKBest(candidates,Parameters.filterNumber);
        HashMap<String, HashMap<Integer, List<SearchPattern>>> toReturn = new HashMap<>();

        if (Parameters.debug) System.out.println("before cut : " + candidates.size());
        if (Parameters.isCentralised) {
            uav.setCandidates(candidates);
            if (Parameters.solver == Parameters.Solver.CP  || Parameters.solver == Parameters.Solver.CPWS || Parameters.solver == Parameters.Solver.CPISO ||
                    Parameters.solver == Parameters.Solver.CPISOWS ||  Parameters.solver == Parameters.Solver.GREEDYBI ||
                    Parameters.solver == Parameters.Solver.GREEDYSEQ || Parameters.solver == Parameters.Solver.GREEDYCP ||
                    Parameters.solver == Parameters.Solver.CPWSBI || Parameters.solver == Parameters.Solver.CPWSSEQ ||
                    Parameters.solver == Parameters.Solver.CPISOWSBI || Parameters.solver == Parameters.Solver.CPISOWSSEQ )
                toReturn = new CPManager(candidates).getPlan();
            else if (Parameters.solver == Parameters.Solver.PLANNING || Parameters.solver == Parameters.Solver.PLANNINGH || Parameters.solver == Parameters.Solver.PLANNINGZ)
                toReturn = new PDDLManager(candidates).getPlan();
            else if (Parameters.solver == Parameters.Solver.GREEDY) {
                toReturn = new GreedyAlgorithm(candidates).getPlan();
            }else {
                toReturn = new MDPManager(candidates).getPlan();
            }
        } else {
            int n = 0;
            for (UAV u : State.getUAVs()) {
                List<SearchPattern> filteredCandidates = filterCandidates(candidates, uav.getEstimatedTargetVelocity().rotate(Math.toRadians(Parameters.filterAngle - (0.5 + n) * 2 * Parameters.filterAngle / Parameters.nUAV)), Parameters.filterAngle / Parameters.nUAV);
                u.setCandidates(filteredCandidates);
                SolverManager solver;
                if (Parameters.solver == Parameters.Solver.CP  || Parameters.solver == Parameters.Solver.CPWS || Parameters.solver == Parameters.Solver.CPISO ||
                        Parameters.solver == Parameters.Solver.CPISOWS ||  Parameters.solver == Parameters.Solver.GREEDYBI ||
                        Parameters.solver == Parameters.Solver.GREEDYSEQ || Parameters.solver == Parameters.Solver.GREEDYCP ||
                        Parameters.solver == Parameters.Solver.CPWSBI || Parameters.solver == Parameters.Solver.CPWSSEQ ||
                        Parameters.solver == Parameters.Solver.CPISOWSBI || Parameters.solver == Parameters.Solver.CPISOWSSEQ)
                    solver = new CPManager(filteredCandidates, Arrays.asList(u));
                else if (Parameters.solver == Parameters.Solver.PLANNING || Parameters.solver == Parameters.Solver.PLANNINGH || Parameters.solver == Parameters.Solver.PLANNINGZ)
                    solver = new PDDLManager(filteredCandidates, Arrays.asList(u));
                else if (Parameters.solver == Parameters.Solver.GREEDY) {
                    solver = new GreedyAlgorithm(filteredCandidates, Arrays.asList(u));
                }else{
                    solver = new MDPManager(filteredCandidates, Arrays.asList(u));
                }
                HashMap<String, HashMap<Integer, List<SearchPattern>>> plan = solver.getPlan();
                if (plan.containsKey(u.getUavName()))
                    toReturn.put(u.getUavName(), plan.get(u.getUavName()));
                n++;
            }
        }
        if (Parameters.validatePlans) {
            CPManager validator = new CPManager(candidates, State.getUAVs(),toReturn);
            validator.validate();
        }
        return toReturn;
    }
}
