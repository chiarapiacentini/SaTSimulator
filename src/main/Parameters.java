package main;

public class Parameters {
    static public boolean debug = false;
    static public double gridXSize = 300; // in m
    static public double gridYSize = 300; // in m
    static double pointsInGrid = 200; // in m
    static double gridDivision = 10000.;
    static public long scaleTime = 50; // 50 ms -> 1 s

    static public double distanceTerrain = 1250;//2500;

    static boolean randomSeed = true;
    static public long seed = 0;
    static boolean randomDestinatin = true;
    static public int nmin = 0;
    static public int nmax = 1000;

    static boolean visualiser = true;
    static public boolean visualiseSelectedRoads = false;
    static boolean visualiseUAVTarget = false;
    static boolean visualiseAllRoads = false;
    static public boolean visualiseGrid = false;
    static boolean visualiseColorRoad = true;
    static public boolean visualiseMoveMap = false;
    static public boolean visualiseSearchPatterns = true;
    static public boolean visualiseCities = true;
    static boolean visualiseSatellite = false;
    static public boolean visualisePlan = true;
    static public boolean visualiseTargetRoute = true;

    // parameters for simulation
    static public int nParticlerMCS = 5000;
    static public int nTimeslices = 15;
    static double difficulty = 0.8; // 1 is easy, 0 is difficult
    static public int nLosses = 100;
    // search patterns
    static public double defaultRadius = 2500;
    static public double defaultInnerRadius = 1000;
    static public double radiusIncrement = 5;
    static public double defaultDuration = 295; // changed to be diameter/v_target

    // initial target velocity (m/s)
    static double targetVelocity = 22;
    static public double targetVelocityMin = 20;
    static public double targetVelocityMax = 22;
    static double targetEvasivness = 0; // 0 not evasive, 1 always evasive
    static int targetSuspicionLevel = 100;

    //static public double targetTopSpeed = 28;
    // uav velocity (m/s)
    static public int nUAV = 2; // number of sampling of evasivness
    static public boolean isCentralised = true; //
    static public double uavEvading = 2; // number of sampling of evasivness
    static public double uavVelocity = 65;
    public static double flightAccuracy = 10;
    public static double tightTurn = 300;
    static int trackerToWide = 50;
    public static int timeBlind = 90;
    static int timeBlindReal = 180;
    static int timeClear = 60; // initial clear time
    static int cameraAngleNarrow = 90;
    static int cameraAngleWide = 180;
    static double radiusObservation = 3000;
    static double radiusDistance = 1500;
    static public boolean filterCandidates = true;
    static public double filterAngle = 180;
    static public int filterNumber = 30;
    public enum Solver {PLANNING ("Planning"), PLANNINGH ("PlanningH"), PLANNINGZ ("PlanningZ"), CP ("CP"), MDP ("MDP"), GREEDYCP ("GreedyCP"),
        CPWS ("CPWS"), GREEDYSEQ ("GreedySeq"), GREEDYBI ("GreedyBi"), CPISO ("CPISO"), CPISOWS ("CPISOWS"),
        CPWSBI ("CPWSBI"), CPWSSEQ("CPWSSEQ"), CPISOWSBI ("CPISOWSBI"), CPISOWSSEQ ("CPISOWSSEQ"), GREEDY("Greedy") ;
        private final String name;
        Solver(String s) {
            name = s;
        }
        public String toString() {
            return this.name;
        }
    };
    static public Solver solver = Solver.GREEDY;
    static public boolean validatePlans = false;

    static public double weight = 0.1;

    // other parameters
    static double posErrEff = 1.0/radiusDistance;
    static double rangeEff = 1.0/(radiusObservation*radiusObservation);
    static double sweepEff = 0.5;
    static double probScale = 1;
    static int lostThreshold = 30;
    // create grid
    static boolean createGrid = false;
    static String gridName = "resources/grid.ser";
    // planner
    static public int planTimeLimit = 60;
    static int cityIndex = 3;
    static public String pathSolver = null;
    static public String pathValidate = null;
    static public String pathExperiments = null;
    static public String nameSolver = null;
    static public String nameValidator = null;
    // options
    static void setOptions(String[] args)
    {
        for(int i = 0;i < args.length;++i)
        {
            if(args[i].equals("-novis"))
            {
                visualiser = false;
                scaleTime = 0;
            }
            else if(args[i].equals("-route") && i+1 < args.length) {
                randomDestinatin = false;
                cityIndex = Integer.parseInt(args[i+1]);
                i++;
            }
            else if(args[i].equals("-uav") && i+1 < args.length) {
                nUAV = Integer.parseInt(args[i + 1]);
                i++;
            }else if(args[i].equals("-angle") && i+1 < args.length) {
                filterAngle = Integer.parseInt(args[i+1]);
                i++;
            }else if(args[i].equals("-maxCandidate") && i+1 < args.length) {
                filterNumber = Integer.parseInt(args[i+1]);
                i++;
            }else if(args[i].equals("-seed") && i+1 < args.length) {
                randomSeed = false;
                seed = Long.parseLong(args[i+1]);
                i++;
            }else if(args[i].equals("-nmax") && i+1 < args.length) {
                nmax = Integer.parseInt(args[i+1]);
                i++;
            }else if(args[i].equals("-nmin") && i+1 < args.length) {
                nmin = Integer.parseInt(args[i + 1]);
                i++;
            }else if (args[i].equals("-solver") && i+1 < args.length) {
                String nameSolver = (args[i + 1]);
                i++;
                switch (nameSolver) {
                    case "cp":
                        solver = Solver.CP;
                        break;
                    case "cpws":
                        solver = Solver.CPWS;
                        break;
                    case "cpwsbi":
                        solver = Solver.CPWSBI;
                        break;
                    case "cpwsseq":
                        solver = Solver.CPWSSEQ;
                        break;
                    case "cpiso":
                        solver = Solver.CPISO;
                        break;
                    case "cpisows":
                        solver = Solver.CPISOWS;
                        break;
                    case "cpisowsbi":
                        solver = Solver.CPISOWSBI;
                        break;
                    case "cpisowsseq":
                        solver = Solver.CPISOWSSEQ;
                        break;
                    case "mdp":
                        solver = Solver.MDP;
                        break;
                    case "greedycp":
                        solver = Solver.GREEDYCP;
                        break;
                    case "greedyseq":
                        solver = Solver.GREEDYSEQ;
                        break;
                    case "greedybi":
                        solver = Solver.GREEDYBI;
                        break;
                    case "planning":
                        solver = Solver.PLANNING;
                        break;
                    case "planningH":
                        solver = Solver.PLANNINGH;
                        break;
                    case "planningZ":
                        solver = Solver.PLANNINGZ;
                        break;
                    case "greedy":
                        solver = Solver.GREEDY;
                        break;
                    default:
                        System.out.println("Solver not recognised, using greedy instead");
                        solver = Solver.GREEDY;
                }

            }else if(args[i].equals("-decentralised")) {
                isCentralised = false;
            }else if(args[i].equals("-centralised")) {
                isCentralised = true;
            }else if(args[i].equals("-v")) {
                debug = true;
            }else if(args[i].equals("-evasive")) {
                targetEvasivness = 1;
                uavEvading = 0.5;
            }else if(args[i].equals("-weight")) {
                weight = Double.parseDouble(args[i+1]);
                i++;
            }else if (args[i].equals("-pathsolver")){
                pathSolver = args[i+1];
                i++;
            } else {
                System.out.println("Options:\n"+
                        "  -novis            -- run without GUI\n"+
                        "  -route n          -- set target destination\n"+
                        "  -uav n            -- set number of uavs\n"+
                        "  -angle n          -- set total filter angle\n"+
                        "  -seed n           -- set seed\n"+
                        "  -solver [solver]  -- set solver\n"+
                        "  -decentralised    -- different circular sectors\n"+
                        "  -centralised      -- use centralised system\n"+
                        "  -v                -- verbose mode\n"
                );
                System.exit(0);
            }
        }
    }


}
