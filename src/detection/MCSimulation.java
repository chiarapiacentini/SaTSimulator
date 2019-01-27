package detection;

import java.util.List;
import java.util.HashMap;
import main.Parameters;
import main.Position;
import main.State;

public class MCSimulation {
	List<List<Position>> paths;
	
	public MCSimulation(List<List<Position>> p){
		paths = p;
	}

	// TODO make sure that the velocity is taken into accout
	public HashMap<Integer,HashMap<Position, Integer>>  simulate(){

        HashMap<Integer,HashMap<Position, Integer>>  toReturn = new  HashMap<Integer,HashMap<Position, Integer>> ();
		if (paths.size() ==0 ) return toReturn;
		for (int i = 0; i<Parameters.nParticlerMCS;++i){
			for (int t = 0; t<Parameters.nTimeslices; ++t){
				// index of destination
				int index = State.rand.nextInt(paths.size());
				List<Position> path = paths.get(index);
				// time sliced simulated
				int positionIndex = randInt(t*Parameters.nTimeslices,(t+1)*Parameters.nTimeslices);
				if (positionIndex > path.size() - 1)
				    continue;
				Position p = path.get(positionIndex);
//				Position p = path.get(Math.min(positionIndex, path.size()-1));
                if (toReturn.containsKey(t)) {
                    if (toReturn.get(t).containsKey(p))
                        toReturn.get(t).put(p, toReturn.get(t).get(p) + 1);
                    else
                        toReturn.get(t).put(p, 1);
                }else {
                    HashMap<Position,Integer> tmp = new HashMap<Position,Integer>();
                    tmp.put(p,1);
                    toReturn.put(t, tmp);
                }
			}
		}
		return toReturn;
	}
	
	private static int randInt(int min, int max) {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = State.rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
}
