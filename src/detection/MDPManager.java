package detection;

import main.Parameters;
import main.Position;
import main.State;
import main.UAV;
import patterns.SearchPattern;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO there is missmatch between name of uavs of solver and actual names in simulator

public class MDPManager extends  CPManager{
    String extension;
    String nameSolver;

    MDPManager(List<SearchPattern> candidate) {
        super(candidate,State.getUAVs());
        if (Parameters.pathSolver==null) {
            folder = "../SaTExperiments/mdp/";
        }else{
            folder = Parameters.pathSolver;
        }
        if (Parameters.nameSolver == null){
            nameSolver = "mem-script";
        }else{
            nameSolver = Parameters.nameValidator;
        }
        extension = ".mdp";
    }

    MDPManager(List<SearchPattern> candidate, List<UAV> uavs) {
        super(candidate,uavs);
        if (Parameters.pathSolver==null) {
            folder = "../SaTExperiments/mdp/";
        }else{
            folder = Parameters.pathSolver;
        }
        if (Parameters.nameSolver == null){
            nameSolver = "mem-script";
        }else{
            nameSolver = Parameters.nameValidator;
        }
        extension = ".mdp";
    }

    private String runSolver(String problemFile, String paramFile){
        try {
            String planName = folder + "plan_" + Parameters.seed + "_" + probNum + extension;
            String toRun = "./" + folder + nameSolver + " -dist=" + paramFile + " -param=" + problemFile + " -uavs=" + uavs.size();
            System.out.println(toRun);
            Process p =  Runtime.getRuntime().exec(toRun);
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
            String line = br.readLine();
            HashMap<String,Integer> rememberTime = new HashMap<>();
            while (line != null) {
                if (line.contains("dospiral") && !line.contains("origin")){
                    String [] tokens = line.split(" ");
                    String spName = tokens[2];
                    SearchPattern sp = nameSearchPatternMapping.get(spName);
                    String nameUAV = uavs.get(0).getUavName();
                    if (uavs.size()!=1) {
                        nameUAV = tokens[3].substring(0, tokens[3].length() - 1);
                        nameUAV = "uav" + Integer.parseInt(nameUAV.substring(3,tokens[3].length() - 1));
                    }
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
                    int index = 4;
                    Double endTime = Double.parseDouble(tokens[index].substring(1,tokens[index].length()-1));
                    time = startTime.intValue() + endTime.intValue() + State.getTime();
                    rememberTime.put(nameUAV,time);
                }
                line = br.readLine();
            }
            br.close();
            System.out.println("Cost: " + cost);
        } catch(Exception e) {
            System.out.println("Error while parsing the plan: " + e);
        }
        System.out.println(toReturn);
        return toReturn;
    }

    public HashMap<String,HashMap<Integer,List<SearchPattern>>> getPlan(){
        // TODO: change to take plan from search plan
        String planFile = runSolver(writeParameterFiles(), writeDistanceFile());
        return parsePlanFile(planFile);
    }

}
