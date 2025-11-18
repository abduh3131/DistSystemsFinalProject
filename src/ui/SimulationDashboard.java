package ui;

import core.MarketNode;
import core.MarketState;
import core.Trade;
import core.TradingAgent;
import core.SimulationConfig;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Swing-based dashboard that visualizes the market state, agent activity, and
 * recent events.
 */
public class SimulationDashboard extends JFrame {
    private final AgentPanel agentPanel = new AgentPanel();
    private final MarketPanel marketPanel = new MarketPanel();
    private final EventLogPanel eventPanel = new EventLogPanel();
    private final List<TradingAgent> agents;
    private final MarketNode marketNode;
    private final Timer refreshTimer;

    public SimulationDashboard(MarketNode marketNode, List<TradingAgent> agents, SimulationConfig config) {
        super("Multi-Agent Stock Trading Simulation");
        this.marketNode = marketNode;
        this.agents = agents;
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(marketPanel);
        splitPane.setBottomComponent(agentPanel);
        add(splitPane, BorderLayout.CENTER);
        add(eventPanel, BorderLayout.EAST);

        JPanel controls = new JPanel();
        JButton stopButton = new JButton("Stop Simulation");
        stopButton.addActionListener(event -> agents.forEach(TradingAgent::shutdown));
        controls.add(stopButton);
        add(controls, BorderLayout.SOUTH);

        refreshTimer = new Timer(1000, e -> refresh());
        refreshTimer.start();
    }

    private void refresh() {
        MarketState state = marketNode.getMarketState();
        marketPanel.updateMarket(state);
        agentPanel.updateAgents(agents, marketNode.getAgentStats(), state.getLastHeartbeat());
        List<Trade> trades = state.getRecentTrades();
        List<String> events = new ArrayList<>();
        events.add("Total trades: " + trades.size());
        events.addAll(trades.stream()
                .skip(Math.max(0, trades.size() - 5))
                .map(trade -> String.format("%s buy=%s sell=%s qty=%d price=%.2f", trade.getSymbol(),
                        trade.getBuyAgent(), trade.getSellAgent(), trade.getQuantity(), trade.getPrice()))
                .collect(Collectors.toList()));
        eventPanel.setEvents(events);
    }
}
