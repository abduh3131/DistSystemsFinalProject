package core;

import net.NetworkClient;
import util.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Periodically sends heartbeat messages to peers and records incoming
 * heartbeats. The manager is shared between MarketNode and TradingAgent to keep
 * the implementation consistent.
 */
public class HeartbeatManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final SimulationConfig config;
    private final NetworkClient client;
    private final Supplier<Message> heartbeatSupplier;

    public HeartbeatManager(SimulationConfig config, NetworkClient client, Supplier<Message> heartbeatSupplier) {
        this.config = config;
        this.client = client;
        this.heartbeatSupplier = heartbeatSupplier;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Message message = heartbeatSupplier.get();
                client.send(message);
            } catch (Exception e) {
                LogUtil.log("Heartbeat", "Failed to send heartbeat: " + e.getMessage());
            }
        }, config.getHeartbeatIntervalMs(), config.getHeartbeatIntervalMs(), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    public void recordHeartbeat(String nodeId) {
        lastHeartbeat.put(nodeId, System.currentTimeMillis());
    }

    public Map<String, Long> getLastHeartbeat() {
        return lastHeartbeat;
    }
}
