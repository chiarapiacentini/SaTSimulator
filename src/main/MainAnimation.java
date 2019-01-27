package main;

import detection.CPManager;
import detection.PDDLManager;
import detection.UAVDetection;
import detection.Planning;
import map.MapOSM;
import patterns.SearchPattern;

import java.io.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/***
 *
 * @author ch
 * iarapiacentini
 *	main class
 */
public class MainAnimation extends Observable implements Runnable, Observer {

    private Thread animation;
    private State state;
    boolean conclude = true;
    long seed = 0;
    int[] indexCities;

    /***
     *
     * initialisation of the state and the visualizer
     *
     */

    // setting up grid from file
    static UAVGrid writeReadGrid(Position current, String mapFile, String gridName){
        // to return
        UAVGrid grid;

        // check if file exists
        File f = new File(gridName);
        Boolean fileExists = false;
        if(f.exists() && !f.isDirectory()) {
            fileExists = true;
        }
        if (Parameters.createGrid || !fileExists){
            try  {
                MapOSM mapOSM = new MapOSM(mapFile);
                try {
                    Thread.sleep(20000); // this is inefficient, check how to wait for map to be created
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                FileOutputStream fileOut = new FileOutputStream(gridName);//creates a card serial file in output stream
                ObjectOutputStream out = new ObjectOutputStream(fileOut);//routs an object into the output stream.
                out.writeObject(new UAVGrid(current, mapOSM));// we designate our array of cards to be routed
                out.close();// closes the data paths
                fileOut.close();// closes the data paths
            }catch(IOException i){
                i.printStackTrace();
            }
        }

        try {
            long start_time = System.currentTimeMillis();
            if (Parameters.debug) System.out.println("\tsetting up grid");
            FileInputStream fileIn = new FileInputStream(gridName);// Read serial file.
            ObjectInputStream in = new ObjectInputStream(fileIn);// input the read file.
            grid = (UAVGrid) in.readObject();// allocate it to the object file already instanciated.
            in.close();//closes the input stream.
            fileIn.close();//closes the file data stream.
            long end_time = System.currentTimeMillis();
            if (Parameters.debug) System.out.println("\tfinished in " + (end_time-start_time)/1000. + " seconds");
        }catch(IOException i){
            i.printStackTrace();
            return null;
        }catch(ClassNotFoundException c){
            System.out.println("Error");
            c.printStackTrace();
            return null;
        }
        return grid;
    }
    public MainAnimation(UAVGrid grid, long seed) {
        super();
        UAV.uavCount = 0;
        SearchPattern.count = 0;
        PDDLManager.probNum = 0;
        CPManager.probNum = 0;
        indexCities = new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 15, 17, 19, 20, 21, 23};
        State.rand = new Random(seed);
        Parameters.seed = seed;
        System.out.println("Seed: " + seed);
        if (Parameters.randomDestinatin) {
            Parameters.cityIndex = indexCities[State.rand.nextInt(indexCities.length)];
        }
        System.out.println("Target: destination " + Parameters.cityIndex);

        // scotland
        String mapFile = "resources/scotland-latest.osm.pbf";
        animation = new Thread(this);
        Position startPosition = new Position(56.1337538, -3.9617041);
        Position startPositionUAV = startPosition.getOffsetInMeters(Parameters.radiusDistance,0);
        GraphHopperRouter gr = new GraphHopperRouter(mapFile, "resources/graphhopperfiles/Scotland");
        UAVDetection detection = new Planning();
        state = new State(gr);
        if( grid == null) {
            grid =  writeReadGrid(startPosition, mapFile,Parameters.gridName);
        }
        List<List<Position>> cities = grid.getCitiesPositions();
        List<Position> possibleDestination = cities.get(Parameters.cityIndex);
        System.out.println("possible destinations " + cities.size());
        int index = State.rand.nextInt(possibleDestination.size());
        Position destination = possibleDestination.get(index);
        Target target = new Target(startPosition.getLatitude(), startPosition.getLongitude(),
                startPosition.getLatitude(), startPosition.getLongitude(), destination.getLatitude(), destination.getLongitude(), gr);
        state.setGrid(grid);
        state.addTarget(target);
        for (int i = 0; i < Parameters.nUAV; ++i) {
            if (i == 0) {
                UAV uav = new UAV(startPositionUAV.getLatitude(), startPositionUAV.getLongitude(), target, detection);
                uav.setCurrentVelocity(new Velocity(0, Parameters.uavVelocity));
                state.addUAV(uav);
            }else{
                UAV uav = new UAV(startPositionUAV.getLatitude(), startPositionUAV.getLongitude(),detection);
                uav.setCurrentVelocity(new Velocity(0, Parameters.uavVelocity));
                state.addUAV(uav);
            }
        }
        if (Parameters.visualiser) {
            Visualizer vis = new Visualizer(startPosition.getLatitude(), startPosition.getLongitude());
            vis.addState(state);
        }

//		// england
//		String mapFile = "resources/england-latest.osm.pbf";


        setChanged();
        notifyObservers("start");
    }

    /***
     * copied from previous version
     */

    public void start() {
        animation.start();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (conclude) {
            state.tick();
            if (State.targetArrived) {
                conclude = false;
                setChanged();
                notifyObservers("arrived");
            }

            if (Parameters.visualiser) {
                try {
                    Thread.sleep(main.Parameters.scaleTime);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == this && arg.equals("arrived"))
        {
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        Parameters.setOptions(args);
        long seed = Parameters.seed;
        if (Parameters.randomSeed){
            seed = System.currentTimeMillis();
        }
        MainAnimation ma = new MainAnimation(null, seed);
        ma.addObserver(ma);
        ma.start();
        System.out.println("model with new visualizer!");
    }


}
