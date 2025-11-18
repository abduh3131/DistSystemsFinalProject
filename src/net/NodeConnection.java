package net;

import core.Message;
import util.LogUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Wraps a socket connection and delivers messages asynchronously.
 */
public class NodeConnection {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Consumer<Message> handler;
    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
    private volatile String remoteNodeId;

    public NodeConnection(Socket socket, Consumer<Message> handler) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.handler = handler;
        startReader();
    }

    private void startReader() {
        readerExecutor.submit(() -> {
            try {
                while (!socket.isClosed()) {
                    Message message = MessageSerializer.read(inputStream);
                    if (remoteNodeId == null) {
                        remoteNodeId = message.getFromNodeId();
                    }
                    handler.accept(message);
                }
            } catch (Exception e) {
                LogUtil.log("NodeConnection", "Reader stopped: " + e.getMessage());
            } finally {
                close();
            }
        });
    }

    public synchronized void send(Message message) throws IOException {
        MessageSerializer.write(outputStream, message);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        readerExecutor.shutdownNow();
    }

    public String getRemoteNodeId() {
        return remoteNodeId;
    }
}
