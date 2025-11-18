package core;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a matched trade between two orders.
 */
public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tradeId;
    private final String buyAgent;
    private final String sellAgent;
    private final String symbol;
    private final int quantity;
    private final double price;
    private final long lamportTime;
    private final Instant executedAt;

    public Trade(String tradeId, String buyAgent, String sellAgent, String symbol, int quantity,
                 double price, long lamportTime) {
        this.tradeId = tradeId;
        this.buyAgent = buyAgent;
        this.sellAgent = sellAgent;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.lamportTime = lamportTime;
        this.executedAt = Instant.now();
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyAgent() {
        return buyAgent;
    }

    public String getSellAgent() {
        return sellAgent;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getLamportTime() {
        return lamportTime;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
