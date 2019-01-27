package main;

import detection.UAVDetection;
import patterns.SearchPattern;
import policy.PlanBased;
import policy.PolicyUAV;
import policy.Track;
import policy.Waiting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class UAV extends Movable {

    static public enum CameraStatus {BLIND,CLEAR,VISIBLE}

    // target that the uav is following
    private Target followingTarget;
    // policy
    private PolicyUAV policy;
    private boolean lostTarget = false;
    private boolean detectingTarget = false;
    private boolean tracking = true;
    private int numberLost = Parameters.nLosses;
    // last position observed
    private Position lastPositionObserved;
    // estimation of target velocity
    private  Velocity estimatedTargetVelocity;
    private double cameraFacing;
    private boolean cameraSweep = false;
    private int timeSinceLastSighting = 0;
    private Position current;
    private Velocity currentVelocity;
    private double radiusObservation = Parameters.radiusObservation;
    private double radiusDistance = Parameters.radiusDistance;
    private UAVDetection detection;
    private List<List<Position>> paths;
    private List<SearchPattern> candidates;
    private String uavName;
    public static int uavCount = 0;
    private int timeBlind = 0;
    private CameraStatus cameraStatus;
    private List<Position> planPosition;
    private int timeClear = Parameters.timeClear;
    Color color;
    public String[] mColors = {
            "#39add1", // light blue
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#637a91", // dark gray
            "#f092b0", // pink
            "#b7c0c7"  // light gray
    };

    public UAV(double _x, double _y, Target t, UAVDetection d) {
        super(_x, _y);
        grid = null;
        current = new Position(_x, _y);
        currentVelocity = new Velocity(0, Parameters.uavVelocity);
        followingTarget = t;
        policy = new Track(this);
        detection = d;
        paths = null;
        estimatedTargetVelocity = new Velocity(0,0);
        cameraFacing = 0;
        cameraStatus = CameraStatus.CLEAR;
        spotted(followingTarget);
        uavName = "uav" + uavCount++;
        planPosition = null;
        timeClear = Parameters.timeClear;
        color = Color.decode(mColors[uavCount]);
    }

    public UAV(double _x, double _y, UAVDetection d) {
        super(_x, _y);
        grid = null;
        current = new Position(_x, _y);
        currentVelocity = new Velocity(0, -Parameters.uavVelocity);
        followingTarget = null;
        policy = new Waiting(this);
        detection = null;
        paths = null;
        detection = d;
        estimatedTargetVelocity = new Velocity(0,0);
        cameraFacing = 0;
        cameraStatus = CameraStatus.CLEAR;
        uavName = "uav" + uavCount++;
        planPosition = null;
        timeClear = Parameters.timeClear;
        color = Color.decode(mColors[uavCount]);
    }

    public Position getEstimatedTargetPosition(){
        if (lastPositionObserved == null) return null;
        Position estimatedPosition = new Position(lastPositionObserved);
        Velocity velocity = new Velocity(estimatedTargetVelocity);
        if(velocity.speed() > 0) velocity.scaleTo((velocity.speed()+ (Parameters.targetVelocityMax/2.1 -
                velocity.speed())*(1-Math.exp(-(double) timeSinceLastSighting/300.0)))*timeSinceLastSighting);
        return estimatedPosition.move(lastPositionObserved,velocity);
    }

    public void setEstimatedTargetPosition(Position p){
        lastPositionObserved = p;
    }


    public double getRadius() {
        return radiusDistance;
    }

    public double getRadiusOservation() {
        return radiusObservation;
    }

    private void spotted(Target t){
        estimatedTargetVelocity = t.getVelocity();//new Velocity(-lastPositionObserved.getX() + newPosition.getX(),-lastPositionObserved.getY() + newPosition.getY());
        lastPositionObserved = t.getPosition();
    }

    public boolean attemptToSpot(Target t){
        if (cameraStatus == CameraStatus.CLEAR) return true;
        if (cameraStatus == CameraStatus.BLIND) return false;
        double angleTarget = new Velocity(0, -Parameters.uavVelocity).angleTo(current, followingTarget.getPosition());
        double diff = angleTarget-cameraFacing;
        diff = (diff + 180) % 360 - 180;
        if ((cameraSweep? Parameters.cameraAngleWide:Parameters.cameraAngleNarrow) >= 2 * diff ){
            // according to older simulator
            double l;
            double ll;
            if(lastPositionObserved == null || timeSinceLastSighting >= Parameters.lostThreshold){
                l = 100;
            }else{
                Position expectedPosition = Position.move(lastPositionObserved,estimatedTargetVelocity);
                l = expectedPosition.distance(followingTarget.getPosition());
            }
            ll = current.distance(followingTarget.getPosition());
            double probability = Parameters.probScale*(cameraSweep?Parameters.sweepEff:1)
                    * (1.0/((Parameters.posErrEff*l*l+1)*(Parameters.rangeEff*ll*ll+1)));
            probability *= getTerrainEffect(State.getGrid().getTerrain(followingTarget.getPosition()));
            //System.out.println("terrain " + probability );
            //probability = 1;
            probability *= Math.sqrt(Math.sqrt((1+followingTarget.getVelocity().speed())/Parameters.targetVelocityMax));
            probability *= Parameters.difficulty;
            double random = State.getRand();
            //if(!tracking) System.out.println("terrain " + probability + " " + random + " " + uavName);
            if (probability > random) {
                //System.out.println(" sigthed");
                return true;
            } //else  System.out.println(" not");

        }
        return false;
    }

    public void observing() {
        if ( (State.getTime()-timeClear) < Parameters.timeClear) cameraStatus = CameraStatus.CLEAR;
        else cameraStatus = CameraStatus.VISIBLE;
        if (timeBlind >= 0){
            --timeBlind;
            cameraStatus = CameraStatus.BLIND;
            return;
        }

        if (numberLost < 0) {
            lostTarget = true;
            return;
        }

        //TODO add distance information
        if (followingTarget.getPosition().distance(current) < radiusObservation && attemptToSpot(followingTarget)) {
            // found target
            if (lostTarget) {
                System.out.println("Target: found at time " + State.getTime() + " by " + uavName);
                // state
                for (UAV u : State.getUAVs()){
                    if (u!=this) {
                        u.setTracking(false);
                        u.setLostTarget(false);
                        u.setDetectingTarget(false);
                        u.setPolicy(new Waiting(u));
                        u.setFollowingTarget(null);
                        u.setPlanPosition(null);
                    }
                    u.setCandidates(new ArrayList<>());
                }
                tracking = true;
                lostTarget = false;
                detectingTarget = false;
                policy = new Track(this);
                timeSinceLastSighting = 0;
                lastPositionObserved = followingTarget.getPosition(); // in this way we reset the believed target position
                timeClear = Parameters.timeClear + State.getTime();
            }
            spotted(followingTarget);
            timeSinceLastSighting = 0;
            numberLost = Parameters.nLosses;
            cameraSweep = false;
        } else {
            if (!lostTarget) numberLost--;
            timeSinceLastSighting++;
            if(tracking && timeSinceLastSighting >= Parameters.trackerToWide){
                cameraSweep = true;
            }
        }
    }

    public void tick(int ticker) {
        if (followingTarget != null) observing();
        currentVelocity = policy.update();
        current = Position.move(current, currentVelocity);
        latitude = current.getLatitude();
        longitude = current.getLongitude();
        if (followingTarget == null) return;
        if (tracking) {
            double angle = new Velocity(0, -Parameters.uavVelocity).angleTo(current, getEstimatedTargetPosition());
            setCameraFacing(angle);
            planPosition = null;
        }
        if (lostTarget && !detectingTarget) {
            System.out.println("Target: lost at time " + ticker);
            for (UAV u : State.getUAVs()) {
                u.setDetectingTarget(true);
                u.setPaths();
            }
            detection.setupGrid(grid,lastPositionObserved,this);
            paths = detection.getPaths();
            HashMap<String,HashMap<Integer,List<SearchPattern>>> plans = detection.detect(grid);
            for( Map.Entry<String,HashMap<Integer,List<SearchPattern>>> p : plans.entrySet() ) {
                String name = p.getKey();
                HashMap<Integer, List<SearchPattern>> plan = p.getValue();

                //TODO : make it more efficient
                for (UAV u : State.getUAVs()) {
                    if (u.getUavName().equals(name)) {
                        u.setPlanPosition(plan.entrySet().stream().sorted(HashMap.Entry.<Integer, List<SearchPattern>>comparingByKey().reversed())
                                .map(s -> s.getValue().get(0).getOrigin())
                                .collect(Collectors.toList()));
                        if (plan.size()==0)  u.setPolicy(new PlanBased(u, null));
                        else u.setPolicy(new PlanBased(u, plan));
                        u.setFollowingTarget(followingTarget);
                        u.setEstimatedTargetPosition(getEstimatedTargetPosition());
                        u.setTracking(false);
                        u.setTimeBlind(Parameters.timeBlindReal);
                        u.setLostTarget(true);
                        u.setNumberLost(Parameters.nLosses);
                    }
                }
            }
        }
    }

    public void setCandidates(List<SearchPattern> candidates) {
        this.candidates = candidates;
    }

    public void setPolicy(PolicyUAV puav){
        policy = puav;
    }

    public void setFollowingTarget(Target t){
        followingTarget = t;
    }

    public void setDetectingTarget(boolean detectingTarget) {
        this.detectingTarget = detectingTarget;
    }

    public void setLostTarget(boolean lostTarget){
        this.lostTarget = lostTarget;
    }

    public void setTracking(boolean tracking){
        this.tracking = tracking;
    }

    public void setTimeBlind(int timeBlind){
        this.timeBlind = timeBlind;
    }

    public void setNumberLost(int numberLost){
        this.numberLost = numberLost;
    }

    public void setPlanPosition(List<Position> planPosition){
        this.planPosition = planPosition;
    }

    public List<List<Position>> getPaths() {
        return paths;
    }

    public void setPaths(){
        paths = null;
    }

    public Position getPosition() {
        return current;
    }

    public Position getLastPositionObserved() {
        return lastPositionObserved;
    }

    public void setPosition(Position p) {
        current = p;
    }

    public List<Position> getGrid() {
        if (grid != null)
            return grid.getGrid();
        return null;
    }

    public List<Position> getCentroids() {
        if (grid != null)
            return grid.getCentroids();
        return null;
    }

    public List<Pair<Position, List<Position>>> getEdges() {
        if (grid != null)
            return grid.getEdges();
        return null;
    }

    public Velocity getEstimatedTargetVelocity(){
        return estimatedTargetVelocity;
    }

    public Velocity getCurrentVelocity(){
        return currentVelocity;
    }

    public void setCurrentVelocity(Velocity v){
        currentVelocity = v;
    }

    public Position getCurrent(){
        return current;
    }

    public void setDetectionMethod(UAVDetection detection) {
        this.detection = detection;
    }

    public void setCameraFacing(double angle){
        cameraFacing = angle;
    }

    public double getCameraFacing(){
        return cameraFacing;
    }

    public boolean getCameraSweep(){ return cameraSweep;}

    public void setCameraSweep(boolean c){ cameraSweep = c;}

    public List<SearchPattern> getCandidates(){return candidates;}

    public int getTimeSinceLastSighting(){return timeSinceLastSighting;}

    public List<Position> getPlan(){
        return planPosition;
    }

    public CameraStatus getCameraStatus(){
        return cameraStatus;
    }

    public String getUavName(){
        return  uavName;
    }

    // TODO : put this in a better place

    static public double getTerrainEffect(Position.Terrain t){
        switch(t) {
            case ROUGH:
                return 0.9;
            case MOUNTAIN:
                return 0.8;
            case URBAN:
                return 0.5;
            case SUBURBAN:
                return 0.7;
            case FOREST:
                return 0.7;
            case CITY:
                return 0.5;
            case UNKNOWN:
                return 0.9;
        }
        return 0.9;
    }

    static public double getTerrainEffectOnSpeed(Position.Terrain t){
        switch(t) {
            case URBAN:
                return 0.65;
            case SUBURBAN:
                return 0.7;
            case CITY:
                return 0.6;
        }
        return 1;
    }

    static public double getTerrainEffectOnConceleament(Position.Terrain t){
        switch(t) {
            case URBAN:
                return 0.9;
            case SUBURBAN:
                return 0.5;
            case CITY:
                return 0.9;
        }
        return 0.1;
    }

    static public double calculateGamma(double terrain){
        double prob = (0.8 * 0.5 * (1.0/6) * terrain);
        prob *= Parameters.difficulty;
        double gamma = (1 - Math.pow((1 - prob),50));
        return gamma;
    }

    public Color getColor(){
        return color;
    }

}
