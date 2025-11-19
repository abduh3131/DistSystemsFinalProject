package core;

import net.NetworkClient;
import util.LogUtil;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Autonomous trader that periodically submits random market orders and handles
 * market updates from a MarketNode. Each agent maintains its own Lamport clock
 * and participates in the heartbeat protocol.
 */
public class TradingAgent implements Runnable {
    private final String agentId;
    private final SimulationConfig config;
    private final LamportClock clock = new LamportClock();
    private final NetworkClient client;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final Map<String, Double> priceView = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final String marketNodeId;
    private HeartbeatManager heartbeatManager;
    private volatile boolean running = true;
    private int ordersSent;

    public TradingAgent(String agentId, SimulationConfig config, String marketNodeId) {
        this.agentId = agentId;
        this.config = config;
        this.marketNodeId = marketNodeId;
        this.client = new NetworkClient("localhost", config.getMarketPort(), this::handleMessage);
    }

    public void start() {
        client.start();
        heartbeatManager = new HeartbeatManager(config, client, () ->
                new Message(agentId, marketNodeId, clock.tickOnSend(), MessageType.HEARTBEAT));
        heartbeatManager.start();
        executor.scheduleAtFixedRate(this::submitRandomOrder, 2000, config.getOrderIntervalMs(), TimeUnit.MILLISECONDS);
        executor.schedule(this::sendRegistration, 1500, TimeUnit.MILLISECONDS);
    }

    private void sendRegistration() {
        Message register = new Message(agentId, marketNodeId, clock.tickOnSend(), MessageType.REGISTER);
        register.withPayload("role", "agent");
        client.send(register);
    }

    private void submitRandomOrder() {
        if (!running) {
            return;
        }
        String[] symbols = config.getInitialPrices().keySet().toArray(new String[0]);
        if (symbols.length == 0) {
            return;
        }
        String symbol = symbols[random.nextInt(symbols.length)];
        boolean isBuy = random.nextBoolean();
        int quantity = random.nextInt(5) + 1;
        double price = priceView.getOrDefault(symbol, config.getInitialPrices().get(symbol));
        Message orderMessage = new Message(agentId, marketNodeId, clock.tickOnSend(), MessageType.ORDER_SUBMIT);
        orderMessage.withPayload("orderId", UUID.randomUUID().toString())
                .withPayload("symbol", symbol)
                .withPayload("side", isBuy ? "BUY" : "SELL")
                .withPayload("quantity", Integer.toString(quantity))
                .withPayload("price", Double.toString(price));
        client.send(orderMessage);
        ordersSent++;
        LogUtil.log(agentId, "Submitted order for " + symbol + " qty=" + quantity + " side=" + (isBuy ? "BUY" : "SELL"));
    }

    private void handleMessage(Message message) {
        clock.updateOnReceive(message.getLamportTime());
        switch (message.getType()) {
            case MARKET_UPDATE -> {
                Map<String, String> payload = message.getPayload();
                payload.forEach((k, v) -> {
                    if (config.getInitialPrices().containsKey(k)) {
                        priceView.put(k, Double.parseDouble(v));
                    }
                });
            }
            case FAILURE_NOTICE -> LogUtil.log(agentId, "Received failure notice: " + message.getPayload());
            case CONTROL -> {
                String command = message.getPayloadValue("command");
                if ("STOP".equalsIgnoreCase(command)) {
                    shutdown();
                }
            }
            case HEARTBEAT -> {
                // Market node heartbeat acknowledgement
            }
            default -> {
            }
        }
    }

    public void shutdown() {
        running = false;
        executor.shutdownNow();
        if (heartbeatManager != null) {
            heartbeatManager.shutdown();
        }
        client.stop();
    }

    @Override
    public void run() {
        start();
    }

    public String getAgentId() {
        return agentId;
    }

    public int getOrdersSent() {
        return ordersSent;
    }
}
