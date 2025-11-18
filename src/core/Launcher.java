package core;

import ui.SimulationDashboard;
import util.LogUtil;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the integrated simulation demo. All components run within a
 * single JVM but still communicate via the asynchronous messaging stack to keep
 * the architecture faithful to the distributed specification.
 */
public class Launcher {
    public static void main(String[] args) {
        SimulationConfig config = new SimulationConfig();
        MarketNode marketNode = new MarketNode("market-primary", config);
        marketNode.start();

        List<TradingAgent> agents = new ArrayList<>();
        for (int i = 0; i < config.getAgentCount(); i++) {
            TradingAgent agent = new TradingAgent("agent-" + i, config, "market-primary");
            agents.add(agent);
            new Thread(agent, "AgentThread-" + i).start();
        }

        SwingUtilities.invokeLater(() -> {
            SimulationDashboard dashboard = new SimulationDashboard(marketNode, agents, config);
            dashboard.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtil.log("Launcher", "Shutdown initiated");
            agents.forEach(TradingAgent::shutdown);
        }));
    }
}
