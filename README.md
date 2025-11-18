# Multi-Agent Stock Trading Simulation System

SOFE 4790U – Distributed Systems course project that showcases concurrency,
logical clock synchronization, asynchronous message passing, and failure
handling through a distributed stock trading simulation. The system includes a
primary market node, autonomous trading agents, optional backup/replication
behaviour, and a Swing dashboard for visualization.

## Prerequisites
- Java 17 or later (tested with OpenJDK 17)
- A POSIX-like shell (Linux/macOS/WSL) for the provided commands

No external libraries are required beyond the standard JDK.

## Project Structure
```
src/
  core/                Core distributed logic (agents, market node, clocks)
  net/                 TCP communication helpers
  ui/                  Swing dashboard components
  util/                Logging helpers
README.md              Setup and run instructions
TEAM_TASKS.md          Team responsibility breakdown
REPORT.md              Draft final report
logs/                  Runtime log files (created at run-time)
```

## Compilation
From the repository root:

```
javac $(find src -name '*.java')
```

This produces `.class` files alongside the sources. For IDEs (IntelliJ, Eclipse)
create a new Java project and mark the `src` folder as a source root.

## Running – Integrated Mode
The simplest demonstration launches all components inside one JVM but they still
communicate using TCP sockets on localhost.

```
java -cp src core.Launcher
```

What happens:
1. `Launcher` instantiates `SimulationConfig`, a `MarketNode`, three agents, and
   the Swing `SimulationDashboard`.
2. The market node listens on port `5050` while agents connect as independent
   threads.
3. Agents submit randomized BUY/SELL market orders that the market matches using
   Lamport time ordering.
4. Heartbeats and the `FailureDetector` keep track of live agents. The dashboard
   shows prices, agent status, and recent trades in real time.
5. Use the “Stop Simulation” button (or close the window) to terminate agents.

## Running – Multi-process mode (optional)
Because the networking layer uses standard TCP sockets, any component can be run
in a separate JVM or even on different machines as long as the ports are
reachable. Example (run each command in its own terminal):

```
# Terminal 1 – Market node
java -cp src core.Launcher  # or write a tiny wrapper that only instantiates MarketNode

# Terminal 2 – Agent
java -cp src core.TradingAgent agent-X
```

For convenience, the integrated `Launcher` is the recommended approach for the
course demo. When running standalone agents or nodes, adjust host/port in
`SimulationConfig` or extend the constructors accordingly.

## Configuration
Modify `src/core/SimulationConfig.java` to tune:
- `agentCount` – number of trading agents started by `Launcher`.
- `orderIntervalMs` – how often agents consider placing orders.
- `heartbeatIntervalMs` and `heartbeatTimeoutMs` – heartbeat frequency and
  failure detection threshold.
- `marketPort`/`backupPort` – TCP ports for the primary/backup market nodes.
- `initialPrices` – initial stock symbols and prices (map in the constructor).
- `enableBackup` – toggle state replication logic.

## Distributed Systems Features
- **Concurrency:** Each market node and trading agent runs in its own thread or
  process, using executors for periodic order submission and monitoring.
- **Logical clocks:** `LamportClock` timestamps every message; the market uses
  Lamport ordering for its order book and event log.
- **Message passing:** `NetworkClient`/`NetworkServer` wrap TCP sockets and
  exchange serialized `Message` objects asynchronously.
- **Fault tolerance:** `HeartbeatManager` and `FailureDetector` send and monitor
  periodic heartbeats; suspected failures are logged and shown in the dashboard.
- **Replication:** `ReplicationManager` streams simplified state snapshots from
  the primary to the backup market node (for demo purposes).
- **Visualization:** `SimulationDashboard` renders the market state, per-agent
  statistics, and textual event log to help with demos and grading.

## Logs
Runtime logs are written to the console and `logs/events.log`. Each line
contains an ISO timestamp plus the component name. Remove or rotate the file
between runs if needed.

## Troubleshooting
- **Port already in use:** change the `marketPort`/`backupPort` values in
  `SimulationConfig` if another application is occupying the defaults.
- **Firewall/antivirus prompts:** allow Java to open local sockets; all traffic
  stays on `localhost` in integrated mode.
- **UI not updating:** ensure Swing is running on a desktop-capable environment;
  when using a remote server, enable X forwarding or stick to headless logging.
- **Slow machines:** increase `orderIntervalMs` to reduce CPU load.

## Extending the Simulation
Suggested directions include adding additional market nodes, experimenting with
network partitions, expanding agent strategies, or persisting the replicated
state to disk for more robust recovery demos.
