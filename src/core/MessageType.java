package core;

/**
 * Enumeration describing the supported message types between agents, market
 * nodes, and the dashboard.
 */
public enum MessageType {
    REGISTER,
    ORDER_SUBMIT,
    MARKET_UPDATE,
    HEARTBEAT,
    STATE_REPLICA,
    FAILURE_NOTICE,
    EVENT_LOG,
    CONTROL
}
