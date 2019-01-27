package main;

import java.io.Serializable;

public class Position implements Serializable {
    static public enum Terrain {ROUGH, URBAN, SUBURBAN, MOUNTAIN, FOREST, CLEAR, CITY, UNKNOWN};

    private double latitude; // this is the latitude (that should be y)
    private double longitude; // this is the longitude (that should be x)
    final private double R=6378137.; // earth radius

    Terrain terrain = Terrain.UNKNOWN;
    int city = -1;

    public Position(double xx, double yy) {
        latitude = xx;
        longitude = yy;
    }

    public Position(Position p) {
        latitude = p.latitude;
        longitude = p.longitude;
    }

    public void setTerrain(Terrain type) {
        terrain = type;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setCity(int c) {
        city = c;
    }

    public int getCity() {
        return city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double  getX() {
        return 111111.1 * (longitude) * Math.cos(Math.toRadians(latitude));
    }

    public double getY() {
        return 111111.1 * (latitude);
    }

    public String toString() {
        return "(" + latitude + "," + longitude + ")";
    }

    public Position getOffsetInMeters(double dx, double dy){

        //Coordinate offsets in radians
        double dLat = dy/R;
        double dLon = dx/(R*Math.cos(Math.toRadians(latitude)));

        //OffsetPosition, decimal degrees
        double newLat = latitude + Math.toDegrees(dLat);
        double newLon = longitude + Math.toDegrees(dLon);
        return new Position(newLat,newLon);
    }

    // distance in m
    public double distance(Position p) {

        Double lonDistance = Math.toRadians(longitude - p.getLongitude());
        Double latDistance = Math.toRadians(latitude - p.getLatitude());
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(p.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to meters
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }


    public static Position move(Position p, Velocity v){
        return p.getOffsetInMeters(v.getVx(), v.getVy());

    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        return hashCode() == other.hashCode();
    }
}
