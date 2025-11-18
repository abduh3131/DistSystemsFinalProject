package core;

import java.io.Serializable;

/**
 * Represents a simplified market order placed by an agent. All orders are
 * treated as market orders for clarity of the simulation.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String orderId;
    private final String agentId;
    private final String symbol;
    private final int quantity;
    private final boolean isBuy;
    private final long lamportTime;

    public Order(String orderId, String agentId, String symbol, int quantity, boolean isBuy, long lamportTime) {
        this.orderId = orderId;
        this.agentId = agentId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.isBuy = isBuy;
        this.lamportTime = lamportTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public long getLamportTime() {
        return lamportTime;
    }
}
