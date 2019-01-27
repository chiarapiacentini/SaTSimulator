package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

// perform experiments
public class Experiments extends Observable implements Runnable, Observer{

    private Thread experiment;
    UAVGrid grid;
    private int maxExp = Parameters.nmax;
    boolean isNext = true;
    private int count;
    String args[];
    MainAnimation ma;
    PrintStream ps = null;
    PrintStream orig = System.out;
    String folderName;

    public Experiments(String args[], String folderName){
        this.args = args;
        this.folderName = folderName;
        String mapFile = "resources/scotland-latest.osm.pbf";
        Position startPosition = new Position(56.1337538, -3.9617041);
        grid =  MainAnimation.writeReadGrid(startPosition, mapFile, Parameters.gridName);
        experiment = new Thread(this);
        count = Parameters.nmin;
    }


    public void start() {

        experiment.start();

    }

    @Override
    public void update(Observable o, Object arg) {

        if ( o == ma && arg.equals("arrived"))
        {
            setChanged();
            notifyObservers("animation");
        }else if ( o == this && arg.equals("animation") ){
            setChanged();
            notifyObservers("pause");
            if (count < maxExp) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                if (ps!=null) {
                    System.setOut(orig);
                    ps.close();
                }
                try {
                    String nameFile =  folderName +"run"  +  count + ".txt";
                    System.out.println("writing " + nameFile);
                    ps = new PrintStream(nameFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                System.setOut(ps);

                System.out.println("Run " +  count);
                // city
                // index of destination
                long seed = System.currentTimeMillis();
                ma = new MainAnimation(grid, seed);
                ma.addObserver(this);
                ma.start();
                count++;
            } else {
                System.out.println("end of experiments");
                setChanged();
                notifyObservers("end experiment");
            }

        }else if ( o == this && arg.equals("end experiment") ){
            System.setOut(orig);
            ps.close();
            System.exit(0);
        }
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (isNext) {
            setChanged();
            notifyObservers("animation");
            isNext = false;
        }
    }


    public static void main(String args[]) {

        Parameters.visualiser = false;
        Parameters.solver = Parameters.Solver.GREEDY;
        System.out.println(Parameters.solver.toString());
        Parameters.nUAV = 4;
        Parameters.setOptions(args);
        String toAdd = "";
        if (Parameters.weight != 0.1) {
            Integer w = new Integer((int)(Parameters.weight*100));
            toAdd = "_w" + w.toString();
        }
        String pathExperiments;
        if (Parameters.pathExperiments==null){
            pathExperiments = "../SaTExperiments/Experiments/";
        }else{
            pathExperiments = Parameters.pathExperiments;
        }
        String nameFolder = pathExperiments + Parameters.solver.toString() + Parameters.nUAV + "UAVs_"+ Parameters.isCentralised+"_"+ new Integer((int)Parameters.targetEvasivness*10).toString() + toAdd +"/";
        File file = new File(nameFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                System.out.println(nameFolder + " created");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        Experiments experiment = new Experiments(args, nameFolder);
        experiment.addObserver(experiment);
        experiment.start();


    }

}
