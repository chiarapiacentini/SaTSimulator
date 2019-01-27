package policy;

import main.Parameters;
import main.UAV;
import main.Velocity;
import patterns.SearchPattern;
import main.State;
import patterns.SpiralSearchPattern;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chiarapiacentini on 2017-05-14.
 */
// this policy receive a plan of the form time, search pattern
public class PlanBased extends PolicyUAV {
    SpiralManouvre s = null;

    private HashMap<Integer,List<SearchPattern>> plan;
    private int time;
    public PlanBased(UAV u, HashMap<Integer,List<SearchPattern>> p) {
        super(u);
        plan = p;
        if ( Parameters.debug) System.out.println("Plan with " + plan.size() + " actions");
        u.setCameraSweep(true);
    }

    public Velocity update() {
        time = State.getTime();
        // decide which search patter to execute
        if (plan.get(time)!=null){
            SearchPattern sp = plan.get(time).get(0);
            if (sp instanceof SpiralSearchPattern) {
                SpiralSearchPattern ssp = (SpiralSearchPattern)sp;
                s = new SpiralManouvre(ssp, uav);
                System.out.println( "UAV: " + uav.getUavName() + " executing search pattern: " + sp + " at time " + time);
            }
        }
        // execute search pattern
        if (s == null)  return new Velocity(0,0);
        return s.update();
    }
}