package models;

import java.util.concurrent.locks.ReentrantLock;

public class Clock {
    private int clock;
    private final ReentrantLock clockLock;

    public Clock(){
        this.clock = 0;
        this.clockLock = new ReentrantLock();
    }

    public int updateClock(){
        this.clock += 1;
        return this.clock;
    }

public int mergeClocks(int receivedClock) {
        clockLock.lock();
        try {
            clock = Math.max(clock, receivedClock); // Atualiza para o máximo
            clock++; // Incrementa após
            return clock;
        } finally {
            clockLock.unlock();
        }
    }

    public int getClock(){
        return this.clock;
    }

    public ReentrantLock getClockLock(){
        return this.clockLock;
    }
}
