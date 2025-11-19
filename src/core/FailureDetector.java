package core;

import util.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Monitors heartbeat timestamps and signals suspected failures when timeouts
 * are exceeded.
 */
public class FailureDetector {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final int timeoutMs;
    private final Consumer<String> failureCallback;

    public FailureDetector(int timeoutMs, Consumer<String> failureCallback) {
        this.timeoutMs = timeoutMs;
        this.failureCallback = failureCallback;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
                if (now - entry.getValue() > timeoutMs) {
                    LogUtil.log("FailureDetector", "Node " + entry.getKey() + " timed out");
                    failureCallback.accept(entry.getKey());
                    lastHeartbeat.remove(entry.getKey());
                }
            }
        }, timeoutMs, timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    public void recordHeartbeat(String nodeId) {
        lastHeartbeat.put(nodeId, System.currentTimeMillis());
    }
}
