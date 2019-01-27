package main;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;
/***
 *
 * @author chiarapiacentini
 * class representing the state
 * each state has a set of targets and uavs
 *
 */
public class State extends Observable {

    static private int ticker = 0;
    private ArrayList<Target> targets;
    static private ArrayList<UAV> uavs;
    static UAVGrid grid;
    static GraphHopperRouter gr;
    static boolean targetArrived = false;
    public static Random rand; //= new Random(0);

    public State(GraphHopperRouter g) {
        ticker = 0;
        targetArrived = false;
        targets = new ArrayList<Target>();
        uavs = new ArrayList<UAV>();
        gr = g;
    }

    public void addTarget(Target t) {
        t.setGrid(grid);
        targets.add(t);
    }

    public void addUAV(UAV u) {
        u.setGrid(grid);
        uavs.add(u);
    }

    public ArrayList<Target> getTargets() {
        return targets;
    }

    static public ArrayList<UAV> getUAVs() {
        return uavs;
    }


    public void setGrid(UAVGrid g){
        grid = g;
        for (Target t : targets)
            t.setGrid(grid);
        for (UAV u : uavs)
            u.setGrid(g);
    }

    public void tick() {
        for (Target t : targets)
            t.tick(ticker);
        for (UAV u : uavs)
            u.tick(ticker);
        ticker++;
        setChanged();
        notifyObservers();

    }

    static GraphHopperRouter getGH() {
        return gr;
    }

    public static UAVGrid getGrid(){
        return grid;
    }

    public static double getRand() {
        return rand.nextDouble();
    }

    static public int getTime(){
        return ticker;
    }
}
