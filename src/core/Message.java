package core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an asynchronous message exchanged between nodes. Each message is
 * timestamped using the Lamport logical clock maintained by the sender.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String messageId;
    private final String fromNodeId;
    private final String toNodeId;
    private final long lamportTime;
    private final MessageType type;
    private final Map<String, String> payload;

    public Message(String fromNodeId, String toNodeId, long lamportTime, MessageType type) {
        this(UUID.randomUUID().toString(), fromNodeId, toNodeId, lamportTime, type, new HashMap<>());
    }

    public Message(String messageId, String fromNodeId, String toNodeId, long lamportTime,
                   MessageType type, Map<String, String> payload) {
        this.messageId = messageId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.lamportTime = lamportTime;
        this.type = type;
        this.payload = payload != null ? new HashMap<>(payload) : new HashMap<>();
    }

    public Message withPayload(String key, String value) {
        payload.put(key, value);
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public long getLamportTime() {
        return lamportTime;
    }

    public MessageType getType() {
        return type;
    }

    public Map<String, String> getPayload() {
        return new HashMap<>(payload);
    }

    public String getPayloadValue(String key) {
        return payload.get(key);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + messageId + '\'' +
                ", from='" + fromNodeId + '\'' +
                ", to='" + toNodeId + '\'' +
                ", lamport=" + lamportTime +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}
