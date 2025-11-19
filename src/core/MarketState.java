package core;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the current market snapshot, including prices, recent trades, and basic
 * per-agent statistics. The class is thread-safe through internal
 * synchronization blocks.
 */
public class MarketState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Double> prices = new ConcurrentHashMap<>();
    private final List<Trade> recentTrades = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer> agentOrderCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();

    public MarketState(Map<String, Double> initialPrices) {
        prices.putAll(initialPrices);
    }

    public Map<String, Double> getPrices() {
        return new HashMap<>(prices);
    }

    public void updatePrice(String symbol, double price) {
        prices.put(symbol, price);
    }

    public void recordTrade(Trade trade) {
        recentTrades.add(trade);
        updatePrice(trade.getSymbol(), trade.getPrice());
        agentOrderCounts.merge(trade.getBuyAgent(), 1, Integer::sum);
        agentOrderCounts.merge(trade.getSellAgent(), 1, Integer::sum);
    }

    public List<Trade> getRecentTrades() {
        synchronized (recentTrades) {
            return new ArrayList<>(recentTrades);
        }
    }

    public Map<String, Integer> getAgentOrderCounts() {
        return new HashMap<>(agentOrderCounts);
    }

    public void recordHeartbeat(String nodeId) {
        lastHeartbeat.put(nodeId, Instant.now());
    }

    public Map<String, Instant> getLastHeartbeat() {
        return new HashMap<>(lastHeartbeat);
    }
}
