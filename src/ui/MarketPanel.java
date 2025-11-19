package ui;

import core.MarketState;
import core.Trade;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

/**
 * Shows the current prices and recently executed trades.
 */
public class MarketPanel extends JPanel {
    private final DefaultTableModel priceModel;
    private final JTextArea tradesArea;

    public MarketPanel() {
        setLayout(new BorderLayout());
        priceModel = new DefaultTableModel(new Object[]{"Symbol", "Price"}, 0);
        JTable table = new JTable(priceModel);
        tradesArea = new JTextArea();
        tradesArea.setEditable(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JScrollPane(tradesArea), BorderLayout.SOUTH);
    }

    public void updateMarket(MarketState state) {
        Map<String, Double> prices = state.getPrices();
        priceModel.setRowCount(0);
        prices.forEach((symbol, price) -> priceModel.addRow(new Object[]{symbol, String.format("%.2f", price)}));

        List<Trade> trades = state.getRecentTrades();
        StringBuilder builder = new StringBuilder();
        for (int i = Math.max(0, trades.size() - 5); i < trades.size(); i++) {
            Trade trade = trades.get(i);
            builder.append(String.format("%s %s %d @ %.2f%n", trade.getSymbol(), trade.getBuyAgent(),
                    trade.getQuantity(), trade.getPrice()));
        }
        tradesArea.setText(builder.toString());
    }
}
