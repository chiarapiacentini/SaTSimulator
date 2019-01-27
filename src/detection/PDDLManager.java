package detection;

import main.Parameters;
import main.State;
import main.UAV;
import patterns.SearchPattern;
import main.Position;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by chiarapiacentini on 2017-05-15.
 */

public class PDDLManager extends SolverManager{

    String nameSolver;

    PDDLManager(List<SearchPattern> candidate){
        super(candidate,State.getUAVs());
        if (Parameters.solver == Parameters.Solver.PLANNING) {
            if (Parameters.pathSolver==null) {
                folder = "../SaTExperiments/planning/";
            }else{
                folder = Parameters.pathSolver;
            }
        }else {
            if (Parameters.pathSolver==null) {
                folder = "../SaTExperiments/zplanning/";
            }else{
                folder = Parameters.pathSolver;
            }
        }
        if (Parameters.nameSolver == null){
            nameSolver = "mem-script";
        }else{
            nameSolver = Parameters.nameValidator;
        }

    }

    PDDLManager(List<SearchPattern> candidate, List<UAV> uavs){
        super(candidate,uavs);
        if (Parameters.solver == Parameters.Solver.PLANNING) {
            if (Parameters.pathSolver==null) {
                folder = "../SaTExperiments/planning/";
            }else{
                folder = Parameters.pathSolver;
            }
        }else {
            if (Parameters.pathSolver==null) {
                folder = "../SaTExperiments/zplanning/";
            }else{
                folder = Parameters.pathSolver;
            }
        }
        if (Parameters.nameSolver == null){
            nameSolver = "mem-script";
        }else{
            nameSolver = Parameters.nameValidator;
        }
    }

    private String writePDDLProblemFile(){
        HashSet<List<Position>> compatibleDestinations = new HashSet();
        try {
            String problemName = "prob_" + Parameters.seed + "_" + probNum;
            String fileName = folder + problemName + ".pddl";
            BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println("Written problem " + fileName);
            outFile.write(";;; problem generated on  the " + dateFormat.format(date) + " ;;;\n");
            outFile.write("(define (problem "+ problemName +")\n");
            outFile.write("\t(:domain UAV)\n");
            outFile.write("\t(:objects \n");
            outFile.write("\t\t");
            for (SearchPattern sp : candidateSearchPattern){
                outFile.write(sp.toString() + " ");
                nameSearchPatternMapping.put(sp.toString(),sp);
                compatibleDestinations.addAll(sp.getCompatibleDestinations());
            }
            outFile.write( " - spiral\n");

            outFile.write("\t\t");
            for (List<Position> r : compatibleDestinations){
                outFile.write(cityNameMapping.get(r) + " ");
            }
            outFile.write( " - destination\n");
            outFile.write( "\t\t");
            for (UAV u : uavs){
                outFile.write( u.getUavName() + " ");
            }
            outFile.write( "- uav\n");
            outFile.write( "\t\t");
            for (UAV u : uavs){
                outFile.write( "origin" + u.getUavName() + " ");
            }
            for (SearchPattern sp : candidateSearchPattern){
                outFile.write(sp.toString() + "s ");
                outFile.write(sp.toString() + "e ");
            }
            outFile.write( " - waypoint\n");
            outFile.write(")\n");
            outFile.write( "\t(:init\n");
            outFile.write("\t(= (n-pattern) 0)\n");
            outFile.write("\t(= (expected-time) 0)\n");
            outFile.write("\t(= (previous-expected-time) 0)\n");
            outFile.write("\t(= (heuristic-approximation) 0.001)\n");
            for (UAV u : uavs){
                outFile.write( "\t(at " + u.getUavName() + " origin" + u.getUavName() + ")\n");
            }
            for (List<Position> r : compatibleDestinations){
                Position p = r.get(r.size()-1);
                outFile.write("\t(= (previous-probability "+ cityNameMapping.get(r)+ ") " + 1./compatibleDestinations.size()+ ")\n");
                outFile.write("\t(= (probability "+ cityNameMapping.get(r)+ ") " + 1./compatibleDestinations.size()+ ")\n");
            }
            outFile.write("\t(= (total-probability) 0)\n");
            outFile.write("\t(= (previous-total-probability) 0)\n");
            for (UAV u : uavs){
                outFile.write( 	"	(= (is-uav " +  u.getUavName() + ") 0)\n");
                for (SearchPattern sp : candidateSearchPattern) {
                    outFile.write("\t(= (is-doing " + sp + " " +  u.getUavName()  + ") 0)\n");
                }
            }
            for (SearchPattern sp : candidateSearchPattern){
                outFile.write("\t(= (timefor "+ sp + ") "+ sp.getDuration() + ")\n");
                outFile.write("\t(beginAt " + sp + "s " + sp + ")\n");
                outFile.write("\t(endAt " + sp + "e " + sp + ")\n");
                outFile.write("\t(at " + sp.getMinT() + " (active " + sp + "))\n");
                outFile.write("\t(at " + sp.getMaxT() + " (not (active " + sp + ")))\n");
                outFile.write("\t(= (n-pattern-active " + sp + ") " +  ((sp.getMaxT()+sp.getMinT())*0.5) +")\n");
                // TODO change this to add real origin distance
                for (UAV u : uavs){
                    outFile.write( 	"	(= (is-uav " +  u.getUavName() + ") 0)\n");
                    outFile.write("\t(= (distance origin" + u.getUavName() + " " + sp + "s) " + u.getCurrent().distance(sp.getOrigin())/Parameters.uavVelocity +")\n");
                }
                for (SearchPattern sp2 : candidateSearchPattern) {
                    double distance = 1;
                    if (!sp.toString().equals(sp2.toString())) distance = Math.ceil(sp.getOrigin().distance(sp2.getOrigin())/ Parameters.uavVelocity);
                    outFile.write("\t(= (distance  " + sp + "e " + sp2 + "s) " +
                             distance + ")\n");
                }
            }
            outFile.write("	)\n");
            outFile.write("	(:goal (and (> (total-probability) 0)))\n");
            outFile.write("	(:metric maximize ( - (total-probability) (* "+ Parameters.weight +" (expected-time)))))\n");
            outFile.close();
            return fileName;
        } catch(Exception e) {
            System.out.println("Problem writing problem to file");
        }
        return null;
    }

    private String writeParameterFile(){
        try {
            String problemName = "param_" + Parameters.seed + "_" + probNum;
            String fileName = folder + problemName + ".pddl";
            BufferedWriter outFile = new BufferedWriter(new FileWriter(fileName));
            System.out.println("Written parameters " + fileName);
            Double maxAverageTime = 0.0;
            for (SearchPattern sp : candidateSearchPattern){
                outFile.write(sp.toString() + " " + sp.getGamma());
                for (List<Position> r : sp.getCompatibleDestinations()){
                    //Position d = r.get(r.size()-1);
                    outFile.write(" " + cityNameMapping.get(r));
                }
                outFile .write("\n");
                Double averageTime = 0.5 * (sp.getMinT() + sp.getMaxT());
                if (averageTime > maxAverageTime){
                    maxAverageTime = averageTime;
                }
            }
            outFile.write(maxAverageTime + "\n");
            outFile.close();
            return fileName;
        } catch(Exception e) {
            System.out.println("Problem writing parameters to file");
        }
        return null;
    }

    private String runPlanner(String problemFile, String paramFile){
        try {
            String planName = folder + "plan_" + Parameters.seed + "_" + probNum + ".pddl";
            String toRun = folder + nameSolver + " -n -E -t" + Parameters.planTimeLimit;
            if (Parameters.solver == Parameters.Solver.PLANNING)
                toRun = toRun + " -x ";
            else if (Parameters.solver == Parameters.Solver.PLANNINGH)
                toRun = toRun + " -xy0 ";
            else
                toRun = toRun + " -xy1 ";
            toRun = toRun + folder + "domain.pddl "
                    + problemFile + " " + folder + "libSaT.dylib " + paramFile;
            System.out.println(toRun);
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

    HashMap<String,HashMap<Integer,List<SearchPattern>>> parsePlanFile(String planFile){
        HashMap<String,HashMap<Integer,List<SearchPattern>>> toReturn = new  HashMap<>();
        double cost=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(planFile));
            boolean solutionFound = false;
            String line = br.readLine();
            HashMap<String,Integer> rememberTime = new HashMap<>();
            while (line != null) {
                if (line.contains("; Cost:")){
                    String [] tokens = line.split(" ");
                    cost = Double.parseDouble(tokens[2]);
                }
                if (solutionFound){
                    if (line.contains("dospiral")){
                        String [] tokens = line.split(" ");
                        SearchPattern sp = nameSearchPatternMapping.get(tokens[4]);
                        String nameUAV = tokens[5].substring(0,tokens[5].length()-1);
                        List<SearchPattern> searchPatterns = new ArrayList<>();
                        searchPatterns.add(sp);
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
                        //toReturn.put(time, searchPatterns);
                        Double startTime = Double.parseDouble(tokens[0].substring(0,tokens[0].length()-1));
                        Double endTime = Double.parseDouble(tokens[7].substring(1,tokens[7].length()-1));
                        time = startTime.intValue() + endTime.intValue() + State.getTime();
                        rememberTime.put(nameUAV,time);
                    }
                }else if (line.contains("Solution Found")) solutionFound = true;
                line = br.readLine();
            }
            br.close();
            System.out.println("Cost: " + cost);
        } catch(Exception e) {
            System.out.println("Error while parsing the plan: " + e);
        }
        //System.out.println(toReturn);
        return toReturn;
    }


    public HashMap<String,HashMap<Integer,List<SearchPattern>>> getPlan(){
        // TODO: change to take plan from search plan
        String planFile = runPlanner(writePDDLProblemFile(), writeParameterFile());
        return parsePlanFile(planFile);
    }

}
