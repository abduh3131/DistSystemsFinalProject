package core;

import net.NetworkClient;
import util.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Periodically sends state snapshots to a backup market node. The backup node
 * can then resume processing with a recent view when promoted.
 */
public class ReplicationManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Supplier<MarketState> stateSupplier;
    private final NetworkClient backupClient;
    private final String nodeId;
    private final LamportClock clock;

    public ReplicationManager(String nodeId, LamportClock clock, Supplier<MarketState> stateSupplier,
                              NetworkClient backupClient) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.stateSupplier = stateSupplier;
        this.backupClient = backupClient;
    }

    public void start() {
        if (backupClient == null) {
            return;
        }
        scheduler.scheduleAtFixedRate(() -> {
            try {
                MarketState snapshot = stateSupplier.get();
                Message message = new Message(nodeId, "backup", clock.tickOnSend(), MessageType.STATE_REPLICA);
                message.withPayload("snapshot", Integer.toString(snapshot.getRecentTrades().size()));
                backupClient.send(message);
            } catch (Exception e) {
                LogUtil.log("Replication", "Unable to send replica: " + e.getMessage());
            }
        }, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
