package de.goforittechnologies.go_for_it.storage;

/**
 * @author  Mario Kiese
 * @version 0.8.
 *
 * class to represent and temporary store position-points
 * contain getter ad setter methods
 */

public class MapData {

    private double longitude;
    private double latitude;
    private double altitude;
    private double height;
    private int id;

    // Constructor
    MapData(double longitude, double latitude,
            double altitude, double height, int id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.height = height;
        this.id = id;
    }

    // Getter
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getHeight() {
        return height;
    }

    public int getId() { return id; }

    // Setter
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * method to convert object to String
     * @return String with information about object's values
     */
    @Override
    public String toString() {


        return "Longitude: " + longitude+ ", " +
                "Latitude: " + latitude + ", " +
                "Altitude: " + altitude + ", " +
                "Height: "   + height;
    }
}
