package edu.mondragon.we2.pinkAlert.model;

public class SimTime {

    private long time; // nanosegundos reales
    private int hours;
    private int minutes;
    private int seconds;

    public SimTime(long time) {
        this.time = time;
        this.hours = (int) getSimHours();
        this.minutes = (int) getSimMinutes();
        this.seconds = (int) getSimSeconds();
    }

    // getters para que se serialice a JSON
    public long getTime() { return time; }
    public int getHours() { return hours; }
    public int getMinutes() { return minutes; }
    public int getSeconds() { return seconds; }

    // 1 segundo real = 1 minuto simulado
    public synchronized long getSimHours() {
        long totalMinutes = (time / 1_000_000_000L);
        return totalMinutes / 60;
    }

    public synchronized long getSimMinutes() {
        long totalMinutes = (time / 1_000_000_000L);
        return totalMinutes % 60;
    }

    public synchronized long getSimSeconds() {
        return 0;
    }
}
