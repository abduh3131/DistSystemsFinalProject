package net;

import core.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Thin wrapper around Java serialization used to keep message transport logic in
 * a single place. The class is intentionally simple because we only exchange
 * {@link Message} objects between JVMs.
 */
public final class MessageSerializer {
    private MessageSerializer() {
    }

    public static void write(ObjectOutputStream out, Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public static Message read(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof Message message) {
            return message;
        }
        throw new IOException("Invalid message payload");
    }
}
