package net;

import core.Message;
import util.LogUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Listens for incoming TCP connections and relays messages to the registered
 * handler.
 */
public class NetworkServer {
    private final int port;
    private final Consumer<Message> messageHandler;
    private final Map<String, NodeConnection> connections = new ConcurrentHashMap<>();
    private final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    public NetworkServer(int port, Consumer<Message> handler) {
        this.port = port;
        this.messageHandler = handler;
    }

    public void start() {
        running = true;
        acceptExecutor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                LogUtil.log("NetworkServer", "Listening on port " + port);
                while (running) {
                    Socket socket = serverSocket.accept();
                    final NodeConnection[] holder = new NodeConnection[1];
                    NodeConnection connection = new NodeConnection(socket, message -> {
                        NodeConnection ref = holder[0];
                        if (ref != null) {
                            connections.putIfAbsent(message.getFromNodeId(), ref);
                        }
                        messageHandler.accept(message);
                    });
                    holder[0] = connection;
                }
            } catch (IOException e) {
                LogUtil.log("NetworkServer", "Stopped: " + e.getMessage());
            }
        });
    }

    public void sendTo(String nodeId, Message message) {
        NodeConnection connection = connections.get(nodeId);
        if (connection == null) {
            LogUtil.log("NetworkServer", "No connection for node " + nodeId);
            return;
        }
        try {
            connection.send(message);
        } catch (IOException e) {
            LogUtil.log("NetworkServer", "Failed to send to " + nodeId + ": " + e.getMessage());
        }
    }

    public void broadcast(Message message) {
        connections.values().forEach(connection -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                LogUtil.log("NetworkServer", "Broadcast failed: " + e.getMessage());
            }
        });
    }

    public void stop() {
        running = false;
        acceptExecutor.shutdownNow();
        connections.values().forEach(NodeConnection::close);
        connections.clear();
    }
}
