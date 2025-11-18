# Multi-Agent Stock Trading Simulation System

**Course:** SOFE 4790U – Distributed Systems  
**Project Type:** Applied distributed application / simulation  
**Team:** Abdallah Hanoosh, Malyka Sardar, Mohammad Al-Lozy, Alexy Pichette, Ethan McLeod  
**Date:** April 2025

## Abstract
This report describes the design and implementation of a distributed stock
trading simulation that demonstrates key concepts taught in SOFE 4790U:
concurrency, logical clock synchronization, asynchronous message passing, fault
tolerance, and user-facing visualization. Multiple autonomous trading agents
interact with one or more market nodes through TCP-based message passing, using
Lamport timestamps to preserve causality. Heartbeat-driven failure detection and
basic replication illustrate resilience strategies. A Swing dashboard provides a
live view of prices, trades, and system health. The resulting system serves as an
educational sandbox for experimenting with distributed behaviour in financial
markets without touching real assets.

## 1. Introduction
Centralized trading simulators often assume a global clock and single process,
limiting their ability to illustrate distributed coordination challenges. This
project aims to remove those assumptions by building a decentralized simulation
where independent trading agents and market nodes communicate asynchronously.
The objectives are threefold: (1) demonstrate concurrency and synchronization
via Lamport clocks, (2) showcase message-passing patterns, heartbeats, and
failure detection, and (3) deliver a tangible visualization for class demos. The
system maps directly to distributed systems course outcomes by combining
concepts such as logical time, replication, and GUI-driven observability.

## 2. Related Work
Academic trading simulators typically use centralized event loops, while
industry-grade systems rely on complex distributed infrastructures (e.g., FIX
networks, exchange co-location). Multi-agent research platforms like JADE or
MATSim provide agent frameworks but often hide low-level networking details. Our
project positions itself as a lightweight, instructional tool: it exposes the
message protocol, synchronization, and failure-handling logic, enabling students
to reason about every distributed interaction without external dependencies.

## 3. System Requirements and Design Goals
### Functional Requirements
- Launch N autonomous trading agents and at least one market node.
- Exchange orders, heartbeats, and market updates using TCP sockets.
- Maintain Lamport logical clocks per node and attach timestamps to all
  messages.
- Detect failed nodes via missing heartbeats and log suspected failures.
- Provide a live dashboard with stock prices, agent health, and event logs.

### Non-Functional Requirements
- **Usability:** single-command launcher plus clear README instructions.
- **Visibility:** logs and GUI should expose Lamport time, orders, and failure
  events for grading.
- **Robustness:** graceful handling of agent failures and optional state
  replication to a backup market node.
- **Extensibility:** clean Java packages for core, networking, UI, and utilities
  so that future enhancements (e.g., new strategies) remain manageable.

## 4. System Architecture
The architecture consists of three node types: trading agents, market nodes, and
an optional UI node. Each node runs as its own thread or process, maintaining a
Lamport clock and network endpoints.

```
+-------------------+         TCP Messages         +---------------------+
|   Trading Agents  | <-------------------------> |     Market Node     |
| (agent-0..agent-N)|                             |  (primary/backup)   |
+-------------------+                             +----------+----------+
                                                            |
                                                            v
                                                    +---------------+
                                                    | Swing UI Node |
                                                    +---------------+
```

**Trading Agents** submit orders and process market updates. **Market Nodes**
match orders, maintain state, broadcast updates, and replicate to backups.
**Dashboard** components connect to the market node via shared state to render
prices and events.

### Message Flow
All messages conform to the `Message` class with fields for sender, receiver,
Lamport timestamp, type, and payload map. `NetworkClient` and `NetworkServer`
wrap TCP sockets to exchange serialized `Message` objects. Order submissions are
sent from agents to the market, the market broadcasts `MARKET_UPDATE` events,
and periodic `HEARTBEAT` messages close the monitoring loop.

### Logical Clocks
`LamportClock` objects increment on each local event and on send operations;
`updateOnReceive` is invoked when processing inbound messages. The market node
uses Lamport ordering in its priority queues to sequence orders deterministically
regardless of actual arrival time.

### Fault Tolerance
Heartbeat timers send `HEARTBEAT` messages at configurable intervals. The
`FailureDetector` records the last heartbeat per node and raises a suspected
failure when a timeout elapses. The `ReplicationManager` periodically serializes
market state summaries and sends them to the backup node, which can later replay
state if promoted.

## 5. Detailed Design and Implementation
### Concurrency Model
- Each trading agent runs inside its own `Thread`, using a
  `ScheduledExecutorService` for periodic order submission and heartbeat tasks.
- Market nodes rely on `NetworkServer` accept loops, executor-backed listener
  threads inside `NodeConnection`, and synchronized access to `MarketState`.
- The dashboard employs a Swing `Timer` to refresh UI components every second.

### Message Passing
`Message` is a serializable POJO with payload key-value pairs. Messages travel
through the `NetworkClient`/`NetworkServer` pair, which rely on
`NodeConnection` objects per socket. `MessageSerializer` wraps Java serialization
for clarity. Messages include types like `REGISTER`, `ORDER_SUBMIT`,
`MARKET_UPDATE`, `HEARTBEAT`, `STATE_REPLICA`, `FAILURE_NOTICE`, and `CONTROL`.

### Synchronization via Lamport Clocks
Every send increments the local Lamport clock; every receive updates the clock
with `max(local, received) + 1`. Orders are inserted into buy/sell priority
queues keyed by Lamport time, ensuring consistent ordering even when network
latency reorders packets.

### Fault Tolerance
- **Heartbeats:** `HeartbeatManager` sends periodic heartbeats using the network
  client; the market node records receipt times in `MarketState`.
- **Failure detection:** `FailureDetector` scans timestamps every timeout
  interval and logs suspected failures, which also feed the dashboard.
- **Replication:** `ReplicationManager` snapshots trade counts and pushes them to
  a backup port. The backup currently logs the state but can be extended to take
  over order processing.

### Market Logic
Orders are modeled as simple market orders. When a new buy order arrives, it is
queued and compared against the earliest sell order (and vice versa). When both
queues have matching symbols, a trade is executed at the midpoint between the
incoming reference price and the current market price. Trades update
`MarketState`, which tracks symbol prices, recent trades, and per-agent order
counts.

### GUI
`SimulationDashboard` contains three subpanels:
- `MarketPanel` – table of stock prices plus a scrollable list of recent trades.
- `AgentPanel` – per-agent statistics including heartbeat timestamps.
- `EventLogPanel` – textual summary of recent trades and system events.
A control panel offers a stop button to halt agents during demos.

## 6. Evaluation and Results
### Test Scenarios
1. **Baseline trading:** Three agents operate with default intervals. Observation
   metrics include number of orders sent and trades executed per minute.
2. **Stress test:** Increase agent count to 8 and reduce `orderIntervalMs` to
   750 ms. The Lamport-ordered queue continues to process trades correctly,
   though CPU usage rises modestly.
3. **Failure scenario:** Manually stop an agent or block its heartbeats. The
   market node logs a suspected failure within ~4 seconds (default timeout), and
   the dashboard highlights the missing heartbeat.
4. **Replication check:** Enable backup node port and verify that snapshot
   messages arrive (confirmed via logs showing `Replica received`).

### Qualitative Results
- **Concurrency:** Logs show overlapping order submissions and matches, proving
  that nodes operate independently.
- **Logical time preservation:** Lamport timestamps in logs confirm that the
  market processes orders in causally consistent order even when messages arrive
  close together.
- **Failure handling:** Removing an agent results in timely failure notices and
  prevents stale heartbeats from lingering. Market continues operating with
  remaining agents.
- **Visualization:** Dashboard panels update once per second, showing price
  fluctuations, agent heartbeats, and event counts that correlate with logs.

## 7. Discussion
The current system intentionally simplifies many production trading concepts.
Orders are market-only with immediate execution, there is no concept of depth or
partial fills beyond the minimum quantity, and network latencies are simulated by
thread scheduling rather than physical separation. Nevertheless, the platform
highlights distributed systems principles clearly. Trade-offs include using Java
serialization (simpler than JSON but less interoperable) and single-machine
execution for ease of grading. The replication mechanism currently transmits
summaries rather than full order books; future work could expand this.

## 8. Conclusions and Future Work
The Multi-Agent Stock Trading Simulation successfully demonstrates concurrency,
Lamport-clock synchronization, asynchronous message passing, and basic fault
handling within an accessible Java project. It provides a reusable template for
labs or assignments exploring distributed coordination. Future enhancements may
include:
- Richer agent strategies (momentum, arbitrage) with tunable parameters.
- Simulation of network partitions, message delays, or packet loss to stress
  logical clock ordering.
- More sophisticated replication (e.g., full state transfer, Raft-based leader
  election) and automated backup promotion.
- Persistent logging and replay tooling for offline analysis.

## 9. References
[1] L. Lamport, “Time, Clocks, and the Ordering of Events in a Distributed
System,” Communications of the ACM, vol. 21, no. 7, 1978.  
[2] A. Tanenbaum and M. van Steen, *Distributed Systems: Principles and
Paradigms*, 2nd ed., Pearson, 2007.  
[3] S. Krause et al., “Agent-based Simulation of Financial Markets,” Journal of
Economic Interaction and Coordination, 2012.

## 10. Contribution Matrix
| Member | Contribution | Highlights |
| --- | --- | --- |
| Abdallah Hanoosh | 22% | MarketNode logic, replication design, abstract/architecture sections |
| Malyka Sardar | 21% | TradingAgent, heartbeat/failure design, detailed synchronization write-up |
| Mohammad Al-Lozy | 19% | Messaging stack, related work and requirements documentation |
| Alexy Pichette | 19% | Dashboard UI, evaluation narrative |
| Ethan McLeod | 19% | Configuration/launcher utilities, introduction and conclusion |
