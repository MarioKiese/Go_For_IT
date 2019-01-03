package de.goforittechnologies.go_for_it.storage;

import java.sql.Timestamp;

/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 *
 * class to represent and temporary store one stepdata information with step-value and timestamp
 * contain getter ad setter methods
 * @see Timestamp
 */

public class StepData {
    private int id;
    private double steps;
    private TimeStamp time;

    //Constructor
    public StepData(int id, double steps, TimeStamp time){
        this.id = id;
        this.steps = steps;
        this.time = time;
    }

    public StepData(double steps, TimeStamp time){
        this.id = id;
        this.steps = steps;
        this.time = time;
    }

    //Getter
    public int getId() { return id; }

    public double getSteps() { return steps; }

    public TimeStamp getTime() { return time; }

    //Setter
    public void setId(int id) { this.id = id; }

    public void setSteps(double steps) { this.steps = steps; }

    public void setTime(TimeStamp time) { this.time = time; }


    //Method

    @Override
    public String toString() {
        return time.toString() + " Steps: " + steps;
    }

    public String createDatabaseName(){
        return "StepDataTABLE_" + time.getMonth();
    }

    public String createTimestampEnrty(){
        return time.getDay() + ":" + time.getHour();
    }
}

