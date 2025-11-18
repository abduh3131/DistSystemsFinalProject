**Summary**
* Implemented the core distributed trading workflow, including the market node’s Lamport-ordered order book, failure detection, and replication logic plus the autonomous agents that submit randomized orders with heartbeat participation. 【F:src/core/MarketNode.java†L15-L130】【F:src/core/TradingAgent.java†L14-L124】
* Built a Swing-based dashboard with market, agent, and event log panels so instructors can observe prices, heartbeat status, and recent trades during demos. 【F:src/ui/SimulationDashboard.java†L20-L70】【F:src/ui/AgentPanel.java†L14-L35】
* Documented setup, configuration, responsibilities, and a full report draft to satisfy course deliverables. 【F:README.md†L1-L115】【F:TEAM_TASKS.md†L1-L48】【F:REPORT.md†L1-L200】

**Testing**
* ✅ `javac $(find src -name '*.java')` 【42cad9†L1-L1】
