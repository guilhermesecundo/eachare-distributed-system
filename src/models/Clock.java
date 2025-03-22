package models;

public class Clock {
    private int clock;

    public Clock(){
        this.clock = 0;
    }

    public int getClock(){
        return this.clock;
    }

    public void updateClock(){
        this.clock++;
    }
}
