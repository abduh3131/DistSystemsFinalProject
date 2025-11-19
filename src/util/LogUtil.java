package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Minimal logging helper that writes to console and log files. Log entries are
 * timestamped to make tracing Lamport time easier when correlating with events.
 */
public final class LogUtil {
    private static final Path LOG_DIR = Path.of("logs");
    private static final Path EVENT_LOG = LOG_DIR.resolve("events.log");

    static {
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }
        } catch (IOException e) {
            System.err.println("Unable to create log directory: " + e.getMessage());
        }
    }

    private LogUtil() {
    }

    public static synchronized void log(String component, String message) {
        String line = String.format("%s [%s] %s", DateTimeFormatter.ISO_INSTANT.format(Instant.now()), component, message);
        System.out.println(line);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(EVENT_LOG, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND))) {
            writer.println(line);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
