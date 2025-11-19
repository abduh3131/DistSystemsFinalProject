package ui;

import core.TradingAgent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Displays per-agent statistics including Lamport-ordered order counts and last
 * heartbeat timestamps.
 */
public class AgentPanel extends JPanel {
    private final DefaultTableModel model;

    public AgentPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"Agent", "Orders", "Last Heartbeat"}, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateAgents(List<TradingAgent> agents, Map<String, Long> orderStats, Map<String, Instant> heartbeats) {
        model.setRowCount(0);
        for (TradingAgent agent : agents) {
            String id = agent.getAgentId();
            long orders = orderStats.getOrDefault(id, 0L);
            Instant heartbeat = heartbeats.get(id);
            model.addRow(new Object[]{id, orders, heartbeat != null ? heartbeat.toString() : "--"});
        }
    }
}
