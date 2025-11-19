package ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Simple scrolling text area used to visualize simulation events.
 */
public class EventLogPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();

    public EventLogPanel() {
        setLayout(new BorderLayout());
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public void setEvents(List<String> events) {
        StringBuilder builder = new StringBuilder();
        for (String event : events) {
            builder.append(event).append(System.lineSeparator());
        }
        textArea.setText(builder.toString());
    }
}
