package de.goforittechnologies.go_for_it.storage;

public class TimeStamp {
    private int hour = 0;
    private int month = 0;
    private int day = 0;

    //Constuctor
    public TimeStamp(int month, int day, int hour){
        this.hour = hour;
        this.day = day;
        this.month = month;
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
