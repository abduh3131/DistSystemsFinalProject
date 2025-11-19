package core;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralizes simulation settings making it easier to tweak the environment
 * without editing multiple classes.
 */
public class SimulationConfig {
    private int agentCount = 3;
    private int orderIntervalMs = 1500;
    private int heartbeatIntervalMs = 1000;
    private int heartbeatTimeoutMs = 4000;
    private int marketPort = 5050;
    private int backupPort = 5051;
    private boolean enableBackup = true;
    private Map<String, Double> initialPrices = new HashMap<>();

    public SimulationConfig() {
        initialPrices.put("AAPL", 185.2);
        initialPrices.put("GOOG", 2720.0);
        initialPrices.put("TSLA", 240.8);
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public int getOrderIntervalMs() {
        return orderIntervalMs;
    }

    public int getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public int getHeartbeatTimeoutMs() {
        return heartbeatTimeoutMs;
    }

    public int getMarketPort() {
        return marketPort;
    }

    public int getBackupPort() {
        return backupPort;
    }

    public boolean isEnableBackup() {
        return enableBackup;
    }

    public Map<String, Double> getInitialPrices() {
        return initialPrices;
    }
}
