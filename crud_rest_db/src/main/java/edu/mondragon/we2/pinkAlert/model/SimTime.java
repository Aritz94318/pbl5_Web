package edu.mondragon.we2.pinkAlert.model;

public class SimTime {

    private long time;
    private int hours;
    private int minutes;
    private int seconds;

    public SimTime(long time, int hours, int minutes,int seconds) {
        this.time = time;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public long getTime() {
        return time;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }


}
