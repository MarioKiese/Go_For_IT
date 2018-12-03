package de.goforittechnologies.go_for_it.storage;

public class RouteData {

    private String route;
    private String time;
    private double calories;
    private double kilometers;
    private int id;

    // Constructor
    public RouteData(String route, String time, double calories, double kilometers, int id) {
        this.route = route;
        this.time = time;
        this.calories = calories;
        this.kilometers = kilometers;
        this.id = id;
    }

    public RouteData(String route, String time, double calories, double kilometers) {
        this.route = route;
        this.time = time;
        this.calories = calories;
        this.kilometers = kilometers;
    }


    // Getter

    public String getRoute() {
        return route;
    }

    public String getTime() {
        return time;
    }

    public double getCalories() {
        return calories;
    }

    public double getKilometers() {
        return kilometers;
    }

    public int getId() {
        return id;
    }


    // Setter

    public void setRoute(String route) {
        this.route = route;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setKilometers(double kilometers) {
        this.kilometers = kilometers;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString() {


        return  "Route: " + route+ ", " +
                "Time: " + time + ", " +
                "Calories: " + calories + ", " +
                "Kilometers: " + kilometers;
    }

}
