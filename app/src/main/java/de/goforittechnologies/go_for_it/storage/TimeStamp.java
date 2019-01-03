package de.goforittechnologies.go_for_it.storage;

import android.util.Log;

/**
 * @author  Tom Hammerbacher
 * @version 0.8.
 *
 * class to represent and temporary store hour, day and month in one object
 * contain getter ad setter methods
 */

public class TimeStamp {
    private int hour = 0;
    private int month = 0;
    private int day = 0;
    private static final String TAG = "TimeStamp";

    //Constuctor

    /**
     * creating timestamp of month, day and hour
     * @param month
     * @param day
     * @param hour
     */
    public TimeStamp(int month, int day, int hour){
        this.hour = hour;
        this.day = day;
        this.month = month;
    }

    /**
     * creating timestamp out of database tablename and tableentry
     * @param tablename
     * @param tableentry
     */

    public TimeStamp(String tablename, String tableentry){
        String [] containsmonth = tablename.split("_");
        this.month = Integer.valueOf(containsmonth[1]);
        String [] containsdayhour = tableentry.split(":");

        this.day = Integer.valueOf(containsdayhour[0]);
        this.hour = Integer.valueOf(containsdayhour[1]);


    }

    //Getter
    public int getHour() { return hour; }

    public int getMonth() { return month; }

    public int getDay() { return day; }

    //Setter
    public void setHour(int hour) { this.hour = hour%24; }

    public void setMonth(int month) { this.month = month%12; }

    public void setDay(int day) {

        if (month == 2){
            this.day= day%29;
        }
        else if (month%2 == 0) {
            this.day = day%30;
        }
        else if (month%2 == 1){
            this.day = day%31;
        }

    }

    //Methods
    @Override
    public String toString() {
        return day + "." + month + ":" + hour;
    }
}
