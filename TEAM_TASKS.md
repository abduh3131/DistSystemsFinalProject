# Team Responsibilities – SOFE 4790U Distributed Systems Project

| Member | Role | Code Focus | Report Focus | Demo Focus |
| --- | --- | --- | --- | --- |
| Abdallah Hanoosh | Team Lead / Coder | MarketNode, ReplicationManager, system integration | Abstract, Architecture overview | Opening statement, live overview |
| Malyka Sardar | Lead Coder | TradingAgent logic, Heartbeat/Fault components | Detailed design (agents, heartbeat) | Demonstrate agent behaviour |
| Mohammad Al-Lozy | Note Taker / Coder | Network stack (NetworkServer, NetworkClient, Message) | Related work, requirements | Narrate message flow |
| Alexy Pichette | Formatter / Coder | Swing UI (SimulationDashboard + panels) | Evaluation & Results formatting | Showcase dashboard views |
| Ethan McLeod | Presenter Lead / Coder | SimulationConfig, Launcher, MarketState utilities | Introduction, conclusions | Final summary & Q/A |

## Individual Task Breakdown

### Abdallah Hanoosh – Team Lead / Coder
- **Code:** Owns `MarketNode`, `ReplicationManager`, and ensures Lamport ordering is respected in matching logic. Coordinates the integration between primary and backup nodes.
- **Report:** Drafts the Abstract and System Architecture sections, emphasizing how the node layout maps to course concepts.
- **Demo:** Introduces the project, explains the design goals, and narrates the high-level architecture before handing off to the coders.

### Malyka Sardar – Lead Coder
- **Code:** Implements and tunes `TradingAgent`, `HeartbeatManager`, and `FailureDetector`. Focuses on autonomous strategies and robust heartbeat propagation.
- **Report:** Authors the Detailed Design subsections that describe agent behaviour, Lamport clocks, and synchronization.
- **Demo:** Runs the live agent demo showing order flow, pauses to inject a failure, and explains how the detector responds.

### Mohammad Al-Lozy – Note Taker / Coder
- **Code:** Builds the messaging stack (`Message`, `MessageType`, `NetworkServer`, `NetworkClient`, `NodeConnection`, `MessageSerializer`). Maintains protocol documentation.
- **Report:** Handles Related Work and System Requirements, documenting assumptions and constraints gathered during meetings.
- **Demo:** Explains how messages traverse the network and references the logical clocks visible in logs.

### Alexy Pichette – Formatter / Coder
- **Code:** Develops the Swing dashboard (`SimulationDashboard`, `AgentPanel`, `MarketPanel`, `EventLogPanel`) and assists with log formatting utilities.
- **Report:** Leads the Evaluation & Results section, ensuring tables/figures are clean and consistent.
- **Demo:** Operates the GUI during the presentation, pointing to agent state changes, price charts, and event logs.

### Ethan McLeod – Presenter Lead / Coder
- **Code:** Configures `SimulationConfig`, `Launcher`, and supporting utilities like `MarketState`. Ensures the project is easy to run by TAs.
- **Report:** Writes the Introduction, Discussion, and Conclusion/Future Work sections, tying technical details back to course learning outcomes.
- **Demo:** Provides the concluding remarks, summarizes lessons learned, and fields Q&A.

## Contribution Matrix

| Member | Estimated Contribution |
| --- | --- |
| Abdallah Hanoosh | 22% |
| Malyka Sardar | 21% |
| Mohammad Al-Lozy | 19% |
| Alexy Pichette | 19% |
| Ethan McLeod | 19% |

Totals sum to 100%. The distribution reflects the slightly higher coordination workload handled by Abdallah while the remaining work is split evenly across coding, reporting, and presentation prep.
