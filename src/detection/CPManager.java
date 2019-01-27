package detection;

import main.*;
import patterns.SearchPattern;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiarapiacentini on 2017-05-18.
 */
public class CPManager extends  SolverManager{
    String extension;
    boolean isValidating;
    HashMap<String,HashMap<Integer,List<SearchPattern>>> planToValidate;
    String nameSolver;
    String nameValidator;

    CPManager(List<SearchPattern> candidate){
        super(candidate,State.getUAVs());
        if (Parameters.pathSolver==null) {
            folder = "../SaTExperiments/constraintProgramming/";
        }else{
            folder = Parameters.pathSolver;
        }
        if (Parameters.nameSolver == null){
            nameSolver = "SaTCP";
        }else{
            nameSolver = Parameters.nameSolver;
        }
        if (Parameters.nameValidator == null){
            nameValidator = "SaTCP";
        }else{
            nameValidator = Parameters.nameValidator;
        }
        if (Parameters.solver == Parameters.Solver.GREEDY || Parameters.solver == Parameters.Solver.GREEDYBI || Parameters.solver == Parameters.Solver.GREEDYSEQ)  extension = ".greedy";
        else extension = ".cp";
        isValidating = false;
    }

    CPManager(List<SearchPattern> candidate, List<UAV> uavs){
        super(candidate,uavs);
        if (Parameters.pathSolver==null) {
            folder = "../SaTExperiments/constraintProgramming/";
        }else{
            folder = Parameters.pathSolver;
        }
        if (Parameters.nameSolver == null){
            nameSolver = "SaTCP";
        }else{
            nameSolver = Parameters.nameSolver;
        }
        if (Parameters.nameValidator == null){
            nameValidator = "SaTCP";
        }else{
            nameValidator = Parameters.nameValidator;
        }
        if (Parameters.solver == Parameters.Solver.GREEDY || Parameters.solver == Parameters.Solver.GREEDYBI || Parameters.solver == Parameters.Solver.GREEDYSEQ) extension = ".greedy";
        else extension = ".cp";
        isValidating = false;
    }

    CPManager(List<SearchPattern> candidate, List<UAV> uavs, HashMap<String,HashMap<Integer,List<SearchPattern>>> plan){
        super(candidate,uavs);
        if (Parameters.pathValidate==null) {
            folder = "../SaTExperiments/constraintProgramming/";
        }else{
            folder = Parameters.pathValidate;
        }
        extension = ".validate";
        isValidating = true;
        planToValidate = plan;
    }


    public String writeParameterFiles(){
        try {
            String problemName = "parameter_" + Parameters.seed + "_" + probNum;
            String fileName = folder + problemName + extension;
            BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
            if (Parameters.debug) System.out.println("Written parameters " + fileName);
            for (UAV u : uavs) {
                outFile.write("origin" +  u.getUavName() + " 0 0 0\n");
            }
            for (SearchPattern sp : candidateSearchPattern){
                outFile.write(sp.toString() + " " + sp.getMinT() + " " + sp.getMaxT() + " " + sp.getGamma() + " ");
                for (List<Position> r : sp.getCompatibleDestinations()){
                    outFile.write(cityNameMapping.get(r) + " ");
                }
                outFile.write("\n");
            }
            outFile.close();
            Thread.sleep(main.Parameters.scaleTime);
            return fileName;
        } catch(Exception e) {
            System.out.println("Problem writing problem to file");
        }
        return null;
    }

    public String writeDistanceFile(){
        try {
            String problemName = "distance_" + Parameters.seed + "_" + probNum;
            String fileName = folder + problemName + extension;
            BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
            if (Parameters.debug) System.out.println("Written distance " + fileName);
            for (SearchPattern sp : candidateSearchPattern){
                // TODO change this to add real origin distance
                for (UAV u : uavs){
                    outFile.write("origin" +  u.getUavName() + " " + sp + " " + Math.ceil(u.getCurrent().distance(sp.getOrigin())/Parameters.uavVelocity) +"\n");
                }
                for (SearchPattern sp2 : candidateSearchPattern) {
                    double distance = 1;
                    if (!sp.toString().equals(sp2.toString())) distance = Math.ceil(sp.getOrigin().distance(sp2.getOrigin())/ Parameters.uavVelocity);
                    outFile.write( sp + " " + sp2 + " " + distance + "\n");
                }
            }
            outFile.close();
            Thread.sleep(main.Parameters.scaleTime);
            return fileName;
        } catch(Exception e) {
            System.out.println("Problem writing parameters to file");
        }
        return null;
    }

    public String writeValidationPlan(){
        try {
            String planName = "plan_" + Parameters.seed + "_" + probNum;
            String fileName = folder + planName + extension;
            BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
            if (Parameters.debug) System.out.println("Written validate plan " + fileName);
            for (UAV u : uavs){
                outFile.write("0 origin" + u.getUavName() + " " + u.getUavName() + "\n");
            }
            LinkedHashMap<Integer,List<main.Pair<SearchPattern, String>>> orderedPlan = new LinkedHashMap<>();

            for (Map.Entry<String,HashMap<Integer,List<SearchPattern>>> plan : planToValidate.entrySet()) {
                // TODO change this to add real origin distance
                String nameUAV = plan.getKey();
                LinkedHashMap<Integer, List<SearchPattern>> tmp = plan.getValue().entrySet().stream().
                        sorted(HashMap.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                for (Map.Entry<Integer, List<SearchPattern>> timesp : tmp.entrySet()) {
                    if (!orderedPlan.containsKey(timesp.getKey()))
                        orderedPlan.put(timesp.getKey(), new ArrayList<>());
                    timesp.getValue().forEach(x -> orderedPlan.get(timesp.getKey()).add(new Pair<>(x, nameUAV)));
                }
            }

            for (Map.Entry<Integer,List<Pair<SearchPattern,String>>> timesp : orderedPlan.entrySet().stream().
                    sorted(HashMap.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue, LinkedHashMap::new)).entrySet()) {
                for (Pair<SearchPattern,String> spName : timesp.getValue()) {
                    SearchPattern sp = spName.first;
                    String nameUAV = spName.second;
                    outFile.write(timesp.getKey()-State.getTime() + " " + sp.toString() + " " + nameUAV + "\n");
                }

            }
            outFile.close();
            Thread.sleep(main.Parameters.scaleTime);
            return fileName;
        } catch(Exception e) {
            System.out.println("Problem writing parameters to file");
        }
        return null;
    }

    public String runCPSolver(String parameterFile, String distanceFile){
        try {
            String planName = folder + "plan_" + Parameters.seed + "_" + probNum + extension;
            String mode = "200";
            if (Parameters.solver == Parameters.Solver.GREEDYCP) mode = "19";
            if (Parameters.solver == Parameters.Solver.GREEDYBI) mode = "20";
            if (Parameters.solver == Parameters.Solver.GREEDYSEQ) mode = "18";
            if (Parameters.solver == Parameters.Solver.CPWS) mode = "22";
            if (Parameters.solver == Parameters.Solver.CPWSBI) mode = "14";
            if (Parameters.solver == Parameters.Solver.CPWSSEQ) mode = "21";
            if (Parameters.solver == Parameters.Solver.CPISO) mode = "8";
            if (Parameters.solver == Parameters.Solver.CPISOWS) mode = "24";
            if (Parameters.solver == Parameters.Solver.CPISOWSBI) mode = "16";
            if (Parameters.solver == Parameters.Solver.CPISOWSSEQ) mode = "23";
            String toRun = folder + nameSolver + " "
                    + parameterFile + " " + distanceFile + " " + mode + " " + Parameters.planTimeLimit + " 1 " + Parameters.weight;
            if (Parameters.debug) System.out.println(toRun);
            Process p = Runtime.getRuntime().exec(toRun);
            InputStream input = new BufferedInputStream( p.getInputStream());
            OutputStream output = new BufferedOutputStream( new FileOutputStream(planName));
            int cnt;
            byte[] buffer = new byte[1024];
            while ( (cnt = input.read(buffer)) != -1) {
                output.write(buffer, 0, cnt );
            }
            output.close();
            return planName;
        }catch(Exception e) {
            System.out.println("Error while executing planner: " + e.toString());
        }

        return null;
    }

    public String runValidate(String parameterFile, String distanceFile, String planName){
        String validateName = folder + "validate_" + Parameters.seed + "_" + probNum + extension;
        try {
            String mode = "7";
            String toRun = folder + nameValidator + " "
                    + parameterFile + " " + distanceFile + " " + mode + " " + Parameters.planTimeLimit + " 1 " + Parameters.weight + " " + planName ;
            if (Parameters.debug) System.out.println(toRun);
            Process p = Runtime.getRuntime().exec(toRun);
            InputStream input = new BufferedInputStream( p.getInputStream());
            OutputStream output = new BufferedOutputStream( new FileOutputStream(validateName));
            int cnt;
            byte[] buffer = new byte[1024];
            while ( (cnt = input.read(buffer)) != -1) {
                output.write(buffer, 0, cnt );
            }
            output.close();
        }catch(Exception e) {
            System.out.println("Error while executing validator: " + e.toString());
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(validateName));
            double cost = 0;
            int notValid = 0;
            String line = br.readLine();
            while (line != null) {
                //System.out.println(line);
                if (line.contains("; Cost ")){
                    String [] tokens = line.split(" ");
                    cost = Double.parseDouble(tokens[2]);
                }
                if (line.contains("Exceeding"))
                    notValid++;
                line = br.readLine();
            }
            br.close();
            System.out.println("Validate: " + cost + " " + notValid);
        } catch(Exception e) {
            System.out.println("Error while parsing the plan: " + e);
        }

        return null;
    }
    HashMap<String,HashMap<Integer,List<SearchPattern>>> parsePlanFile(String planFile){
        HashMap<String,HashMap<Integer,List<SearchPattern>>> toReturn = new  HashMap<>();
        double cost=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(planFile));
            boolean solutionFound = false;
            String line = br.readLine();
            HashMap<String,Integer> rememberTime = new HashMap<>();
            while (line != null) {
                //System.out.println(line);
                if (solutionFound){
                    if (line.contains("search")){
                        String [] tokens = line.split(" ");
                        SearchPattern sp = nameSearchPatternMapping.get(tokens[1]);
                        List<SearchPattern> searchPatterns = new ArrayList<>();
                        searchPatterns.add(sp);
                        String nameUAV = "";
                        if (State.getUAVs().size()==1){
                            nameUAV = State.getUAVs().get(0).getUavName();
                        }else{
                            nameUAV = tokens[4];
                        }
                        int time = State.getTime() + 1;
                        if (toReturn.get(nameUAV)!=null){
                            time  = rememberTime.get(nameUAV);
                            toReturn.get(nameUAV).put(time,searchPatterns);
                        }else{
                            HashMap<Integer,List<SearchPattern>> tmp = new HashMap<>();
                            tmp.put(time,searchPatterns);
                            toReturn.put(nameUAV,tmp);
                            rememberTime.put(nameUAV,time);
                        }
                        Double startTime = Double.parseDouble(tokens[0]);//+ sp.getDuration();
                        time = startTime.intValue() + State.getTime();
                        rememberTime.put(nameUAV,time);
                    }
                }
                if (line.contains("checking")) {
                    solutionFound = true;
                    toReturn  = new HashMap<>();
                }
                if (line.contains("; Cost ") && cost ==0 ){
                    String [] tokens = line.split(" ");
                    cost = Double.parseDouble(tokens[2]);
                }
                line = br.readLine();
            }
            br.close();
            System.out.println("Cost: " + cost);
        } catch(Exception e) {
            System.out.println("Error while reading the validation: " + e);
        }
        return toReturn;
    }

    public HashMap<String,HashMap<Integer,List<SearchPattern>>> getPlan(){
        // TODO: change to take plan from search plan
        String planFile = runCPSolver(writeParameterFiles(), writeDistanceFile());
        return parsePlanFile(planFile);
    }

    public  void validate(){
        String planFile = writeValidationPlan();
        runValidate(writeParameterFiles(), writeDistanceFile(),planFile);
    }
}
