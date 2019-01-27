package patterns;

import main.Position;
import main.UAVGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiarapiacentini on 2017-04-15.
 */
public abstract class SearchPattern {
    Position origin;
    Double concealment;
    Double gamma;
    List<List<Position>> compatibleDestinations;
    Double minT;
    Double maxT;
    Double duration;
    List<Position> internalPoints;
    int nParticles;
    UAVGrid grid;
    String name;
    public static int count = 0;
    public SearchPattern(Position o, UAVGrid g) {
        ++count;
        origin = o;
        grid = g;
        gamma = 1.;
        internalPoints = new ArrayList<>();
        compatibleDestinations = new ArrayList<>();
        name = "searchpattern" + count;
        nParticles = 0;
    }

    abstract void calculateConcealment(UAVGrid grid);

    abstract void calculateGamma(UAVGrid grid);

    void setCompatibleDestinations(List<List<Position>> cd) {
        compatibleDestinations = cd;
    }

    public Position getOrigin() {
        return origin;
    }

    public Double getConcealment() {
        return concealment;
    }

    public Double getGamma() {
        return gamma;
    }

    public List<List<Position>> getCompatibleDestinations() {
        return compatibleDestinations;
    }

    public Double getMinT() {
        return minT;
    }

    public Double getMaxT() {
        return maxT;
    }

    public Double getDuration() { return duration;}

    List<Position> getInternalPoints() {
        return internalPoints;
    }

    public Integer getNParticles(){
        return nParticles;
    }

    @Override
    public String toString(){
        return name;
    }
}
