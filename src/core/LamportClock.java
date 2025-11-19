package core;

/**
 * Implements a Lamport logical clock. The clock is incremented on every local
 * event and updated on every message receive according to Lamport's rules.
 */
public class LamportClock {
    private long time = 0L;

    public synchronized long tickOnLocalEvent() {
        time++;
        return time;
    }

    public synchronized long tickOnSend() {
        time++;
        return time;
    }

    public synchronized long updateOnReceive(long receivedTime) {
        time = Math.max(time, receivedTime) + 1;
        return time;
    }

    public synchronized long peek() {
        return time;
    }
}
