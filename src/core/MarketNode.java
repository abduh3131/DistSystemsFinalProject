package core;

import net.NetworkClient;
import net.NetworkServer;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core exchange node that maintains the order book, matches trades, and
 * broadcasts market state to agents and the dashboard.
 */
public class MarketNode {
    private final String nodeId;
    private final SimulationConfig config;
    private final MarketState marketState;
    private final LamportClock clock = new LamportClock();
    private final NetworkServer server;
    private final FailureDetector failureDetector;
    private final Map<String, Long> agentStats = new ConcurrentHashMap<>();
    private final PriorityQueue<Order> buyOrders = new PriorityQueue<>(Comparator.comparingLong(Order::getLamportTime));
    private final PriorityQueue<Order> sellOrders = new PriorityQueue<>(Comparator.comparingLong(Order::getLamportTime));
    private final ReplicationManager replicationManager;
    private final NetworkClient backupClient;

    public MarketNode(String nodeId, SimulationConfig config) {
        this.nodeId = nodeId;
        this.config = config;
        this.marketState = new MarketState(config.getInitialPrices());
        this.server = new NetworkServer(config.getMarketPort(), this::handleMessage);
        this.failureDetector = new FailureDetector(config.getHeartbeatTimeoutMs(),
                suspected -> LogUtil.log(nodeId, "Suspected failure of " + suspected));
        if (config.isEnableBackup()) {
            backupClient = new NetworkClient("localhost", config.getBackupPort(), this::handleReplicaMessage);
            backupClient.start();
            replicationManager = new ReplicationManager(nodeId, clock, () -> marketState, backupClient);
        } else {
            backupClient = null;
            replicationManager = null;
        }
    }

    public void start() {
        server.start();
        failureDetector.start();
        if (replicationManager != null) {
            replicationManager.start();
        }
        LogUtil.log(nodeId, "Market node ready");
    }

    private void handleReplicaMessage(Message message) {
        // Backup node receives state replica; demo only logs it.
        LogUtil.log("Backup", "Replica received at t=" + message.getLamportTime() + " payload=" + message.getPayload());
    }

    private void handleMessage(Message message) {
        clock.updateOnReceive(message.getLamportTime());
        switch (message.getType()) {
            case REGISTER -> {
                LogUtil.log(nodeId, "Registered " + message.getFromNodeId());
                agentStats.put(message.getFromNodeId(), 0L);
            }
            case ORDER_SUBMIT -> processOrder(message);
            case HEARTBEAT -> {
                failureDetector.recordHeartbeat(message.getFromNodeId());
                marketState.recordHeartbeat(message.getFromNodeId());
            }
            default -> {
            }
        }
    }

    private void processOrder(Message message) {
        String orderId = message.getPayloadValue("orderId");
        String symbol = message.getPayloadValue("symbol");
        boolean isBuy = "BUY".equalsIgnoreCase(message.getPayloadValue("side"));
        int quantity = Integer.parseInt(message.getPayloadValue("quantity"));
        double price = Double.parseDouble(message.getPayloadValue("price"));
        Order order = new Order(orderId, message.getFromNodeId(), symbol, quantity, isBuy, message.getLamportTime());
        PriorityQueue<Order> book = isBuy ? buyOrders : sellOrders;
        book.add(order);
        agentStats.merge(order.getAgentId(), 1L, Long::sum);
        matchOrders(symbol, price);
        publishUpdate();
    }

    private void matchOrders(String symbol, double referencePrice) {
        List<Trade> executed = new ArrayList<>();
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buy = buyOrders.peek();
            Order sell = sellOrders.peek();
            if (!buy.getSymbol().equals(sell.getSymbol())) {
                break;
            }
            buyOrders.poll();
            sellOrders.poll();
            int quantity = Math.min(buy.getQuantity(), sell.getQuantity());
            double price = (referencePrice + marketState.getPrices().getOrDefault(symbol, referencePrice)) / 2.0;
            Trade trade = new Trade(UUID.randomUUID().toString(), buy.getAgentId(), sell.getAgentId(), symbol, quantity,
                    price, clock.tickOnLocalEvent());
            marketState.recordTrade(trade);
            executed.add(trade);
        }
        if (!executed.isEmpty()) {
            LogUtil.log(nodeId, "Executed trades: " + executed.size());
        }
    }

    private void publishUpdate() {
        Message update = new Message(nodeId, "broadcast", clock.tickOnSend(), MessageType.MARKET_UPDATE);
        for (Map.Entry<String, Double> entry : marketState.getPrices().entrySet()) {
            update.withPayload(entry.getKey(), Double.toString(entry.getValue()));
        }
        server.broadcast(update);
    }

    public MarketState getMarketState() {
        return marketState;
    }

    public Map<String, Long> getAgentStats() {
        return agentStats;
    }
}
