package de.goforittechnologies.go_for_it.storage;

public class MapsData {

    private double longitude;
    private double latitude;
    private double altitude;
    private double height;

    // Constructor
    public MapsData(double longitude, double latitude, double altitude, double height) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.height = height;
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

}
