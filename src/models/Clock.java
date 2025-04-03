package models;

import java.util.concurrent.locks.ReentrantLock;

public class Clock {
    private int clock;
    private ReentrantLock clockLock;

    public Clock(){
        this.clock = 0;
        this.clockLock = new ReentrantLock();
    }

    public int getClock(){
        return this.clock;
    }

    public int updateClock(){
        this.clock += 1;
        return this.clock;
    }

    public ReentrantLock getClockLock(){
        return this.clockLock;
    }
}
