package main;

/***
 *
 * @author chiarapiacentini
 * abstract class for objects that can be represented in the map and can move;
 * @params x, y are the coordinate in the map
 *
 */
abstract public class Movable {
    double longitude;
    double latitude;
    UAVGrid grid;
//    MapOSM mapOSM;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Movable(double _lat, double _lon) {
        latitude = _lat;
        longitude = _lon;
    }

    public void setGrid(UAVGrid g){
        grid = g;
    }

    /***
     *
     * @param ticker ticker is the time
     * this method should tell how the coordinate changes with the time
     */
    abstract void tick(int ticker);
}
