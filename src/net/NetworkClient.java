package net;

import core.Message;
import util.LogUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Establishes a client-side connection to a server and pushes messages through
 * a NodeConnection wrapper.
 */
public class NetworkClient {
    private final String host;
    private final int port;
    private NodeConnection connection;
    private final Consumer<Message> handler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NetworkClient(String host, int port, Consumer<Message> handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public void start() {
        executor.submit(() -> {
            while (connection == null) {
                try {
                    Socket socket = new Socket(host, port);
                    connection = new NodeConnection(socket, handler);
                    LogUtil.log("NetworkClient", "Connected to " + host + ":" + port);
                } catch (IOException e) {
                    LogUtil.log("NetworkClient", "Retrying connection: " + e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        });
    }

    public void send(Message message) {
        if (connection == null) {
            LogUtil.log("NetworkClient", "Connection not ready");
            return;
        }
        try {
            connection.send(message);
        } catch (IOException e) {
            LogUtil.log("NetworkClient", "Send failed: " + e.getMessage());
        }
    }

    public void stop() {
        if (connection != null) {
            connection.close();
        }
        executor.shutdownNow();
    }
}
