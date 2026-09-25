package tidekeeper.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import tidekeeper.adapter.*;
import tidekeeper.bridge.*;
import tidekeeper.model.*;
import tidekeeper.service.AssessmentService;
import tidekeeper.service.AssessmentService.CompletedAssessment;
import tidekeeper.vendor.*;

/** Swing frontend. It only assembles dependencies; the service owns the use case. */
public final class Dashboard extends JPanel {
    private static final Color INK = new Color(24, 59, 70);
    private static final Color TEAL = new Color(22, 100, 103);
    private static final Color PAPER = new Color(240, 245, 243);
    private final JComboBox<Artifact> artifact = new JComboBox<>(new Artifact[] {
        new Artifact("TK-001", "Oak pulley block", "Wood"),
        new Artifact("TK-002", "Iron rigging ring", "Iron"),
        new Artifact("TK-003", "Ceramic storage jar", "Ceramic")
    });
    private final JComboBox<String> sensor = new JComboBox<>(new String[] {"Legacy probe", "Metric station"});
    private final JComboBox<String> assessment = new JComboBox<>(new String[] {"Storage climate", "Desalination bath"});
    private final JComboBox<String> renderer = new JComboBox<>(new String[] {"Visual report (HTML)", "Field note (plain text)"});
    private final JTextField packet = new JTextField("68;55;0.35");
    private final JTextField temperature = new JTextField("20");
    private final JTextField humidity = new JTextField("0.55");
    private final JTextField conductivity = new JTextField("350");
    private final CardLayout inputLayout = new CardLayout();
    private final JPanel inputCards = new JPanel(inputLayout);
    private final JEditorPane preview = new JEditorPane();
    private final JTextArea trace = new JTextArea();
    private final JLabel status = new JLabel("Ready. Choose your inputs and run an assessment.");
    private final JLabel temperatureValue = new JLabel("-- C");
    private final JLabel humidityValue = new JLabel("-- % RH");
    private final JLabel conductivityValue = new JLabel("-- uS/cm");
    private final JButton runButton = new JButton("Run assessment");
    private final JButton exportButton = new JButton("Save report...");
    private final AssessmentService service = new AssessmentService();
    private CompletedAssessment completed;

    public Dashboard() {
        super(new BorderLayout(20, 20));
        setBackground(PAPER);
        setBorder(new EmptyBorder(24, 28, 18, 28));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = vertical();
        title.add(label("TIDEKEEPER", 30, Font.BOLD));
        title.add(label("Maritime heritage / Conservation workbench", 15, Font.PLAIN));
        header.add(title, BorderLayout.WEST);
        JLabel demo = label("EDUCATIONAL SIMULATION", 12, Font.BOLD);
        demo.setForeground(TEAL);
        header.add(demo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel controls = vertical();
        controls.setBorder(new EmptyBorder(18, 18, 18, 18));
        controls.setBackground(Color.WHITE);
        controls.setOpaque(true);
        controls.add(label("01  Configure assessment", 19, Font.BOLD));
        controls.add(Box.createVerticalStrut(12));
        field(controls, "Collection item", artifact);
        field(controls, "Sensor source", sensor);
        JPanel legacy = vertical();
        field(legacy, "Packet: F ; % RH ; mS/cm", packet);
        legacy.add(label("Example: 68;55;0.35", 12, Font.PLAIN));
        JPanel metric = vertical();
        field(metric, "Temperature (C)", temperature);
        field(metric, "Humidity ratio (0-1)", humidity);
        field(metric, "Conductivity (uS/cm)", conductivity);
        inputCards.setOpaque(false);
        inputCards.add(legacy, "legacy");
        inputCards.add(metric, "metric");
        inputCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        inputCards.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(inputCards);
        field(controls, "Assessment type", assessment);
        field(controls, "Report presentation", renderer);
        runButton.setMnemonic(KeyEvent.VK_R);
        runButton.setBackground(TEAL);
        runButton.setForeground(Color.WHITE);
        runButton.setOpaque(true);
        runButton.setAlignmentX(LEFT_ALIGNMENT);
        runButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        runButton.setPreferredSize(new Dimension(270, 44));
        controls.add(runButton);
        controls.add(Box.createVerticalStrut(10));
        JButton sample = new JButton("Load review example");
        sample.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(sample);
        controls.add(Box.createVerticalStrut(16));
        JTextArea note = new JTextArea("Inspired by real shipwreck conservation. Items, sensors and thresholds are fictional teaching examples.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setOpaque(false);
        note.setForeground(INK);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setAlignmentX(LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        controls.add(note);
        controls.add(Box.createVerticalGlue());
        JScrollPane controlScroll = new JScrollPane(controls);
        controlScroll.setBorder(null);
        controlScroll.setPreferredSize(new Dimension(340, 610));
        controlScroll.setMinimumSize(new Dimension(290, 300));

        JPanel workspace = new JPanel(new BorderLayout(14, 14));
        workspace.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("TEMPERATURE", temperatureValue));
        metrics.add(metricCard("HUMIDITY", humidityValue));
        metrics.add(metricCard("CONDUCTIVITY", conductivityValue));
        workspace.add(metrics, BorderLayout.NORTH);
        preview.setEditable(false);
        preview.setContentType("text/html");
        preview.setText("<html><body style='font-family:Segoe UI;color:#183b46;margin:24px'><h1>A second life for objects<br>recovered from the sea.</h1><p>Select an item, normalize its sensor readings,<br>and generate a conservation note.</p><p>Use the Pattern trace tab to inspect Adapter and Bridge.</p></body></html>");
        preview.getAccessibleContext().setAccessibleName("Assessment report preview");
        trace.setEditable(false);
        trace.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        trace.setMargin(new Insets(20, 20, 20, 20));
        trace.setText("Run an assessment to see the actual objects used by both patterns.");
        trace.getAccessibleContext().setAccessibleName("Design pattern execution trace");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Report preview", new JScrollPane(preview));
        tabs.addTab("Pattern trace", new JScrollPane(trace));
        workspace.add(tabs, BorderLayout.CENTER);
        exportButton.setEnabled(false);
        exportButton.setMnemonic(KeyEvent.VK_S);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(exportButton);
        workspace.add(bottom, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlScroll, workspace);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0);
        split.setDividerLocation(340);
        split.setDividerSize(8);
        add(split, BorderLayout.CENTER);
        status.setForeground(INK);
        status.getAccessibleContext().setAccessibleName("Assessment status");
        add(status, BorderLayout.SOUTH);

        sensor.addActionListener(e -> {
            inputLayout.show(inputCards, sensor.getSelectedIndex() == 0 ? "legacy" : "metric");
            inputCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, sensor.getSelectedIndex() == 0 ? 110 : 220));
            inputCards.revalidate();
        });
        runButton.addActionListener(e -> runAssessment());
        exportButton.addActionListener(e -> saveReport());
        sample.addActionListener(e -> {
            packet.setText("82.4;78;1.2");
            temperature.setText("28"); humidity.setText("0.78"); conductivity.setText("1200");
            artifact.setSelectedIndex(1);
            status.setText("Review example loaded. Run assessment to update the report.");
        });
    }

    private static JPanel vertical() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }
    private static JLabel label(String text, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(INK);
        l.setFont(new Font("Segoe UI", style, size));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
    private static void field(JPanel parent, String name, JComponent component) {
        JLabel l = label(name, 13, Font.BOLD);
        l.setLabelFor(component);
        parent.add(l);
        parent.add(Box.createVerticalStrut(5));
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        component.setPreferredSize(new Dimension(270, 34));
        component.getAccessibleContext().setAccessibleName(name);
        parent.add(component);
        parent.add(Box.createVerticalStrut(12));
    }
    private static JPanel metricCard(String name, JLabel value) {
        JPanel p = vertical();
        p.setOpaque(true); p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.add(label(name, 11, Font.BOLD));
        p.add(Box.createVerticalStrut(8));
        value.setFont(new Font("Segoe UI", Font.BOLD, 22)); value.setForeground(TEAL);
        p.add(value);
        return p;
    }
    private void runAssessment() {
        try {
            SensorSource source = sensor.getSelectedIndex() == 0
                    ? new LegacyProbeAdapter(new LegacyProbe(packet.getText()))
                    : new MetricStationAdapter(new MetricStation(Map.of(
                            "temperature_c", Double.parseDouble(temperature.getText().trim()),
                            "humidity_ratio", Double.parseDouble(humidity.getText().trim()),
                            "conductivity_us", Double.parseDouble(conductivity.getText().trim()))));
            ReportRenderer output = renderer.getSelectedIndex() == 0 ? new HtmlRenderer() : new PlainTextRenderer();
            ConservationAssessment operation = assessment.getSelectedIndex() == 0
                    ? new StorageAssessment(output) : new DesalinationAssessment(output);
            completed = service.run((Artifact) artifact.getSelectedItem(), source, operation);
            preview.setContentType(completed.contentType());
            preview.setText(completed.report()); preview.setCaretPosition(0);
            Reading r = completed.result().reading();
            temperatureValue.setText(String.format(Locale.ROOT, "%.1f C", r.temperatureCelsius()));
            humidityValue.setText(String.format(Locale.ROOT, "%.1f %% RH", r.relativeHumidityPercent()));
            conductivityValue.setText(String.format(Locale.ROOT, "%.1f uS/cm", r.conductivityMicrosiemens()));
            trace.setText("ADAPTER | Normalize an incompatible vendor API\n\n"
                    + (sensor.getSelectedIndex() == 0 ? "LegacyProbe\n  Fahrenheit -> Celsius\n  mS/cm -> uS/cm\n" : "MetricStation\n  Humidity fraction -> percent\n")
                    + "  -> " + completed.adapterName() + "\n  -> SensorSource.read()\n  -> Reading\n\n"
                    + "BRIDGE | Two independently variable hierarchies\n\nConservationAssessment\n  -> "
                    + completed.assessmentName() + "\n  holds ReportRenderer\n  -> " + completed.rendererName()
                    + "\n\nAssessmentService connects both patterns.\nNo vendor-specific assessment subclasses are needed.\n\n"
                    + "Try all 2 sensors x 2 assessments x 2 renderers.\nThe selected artifact is: " + completed.result().artifact().name());
            trace.setCaretPosition(0);
            exportButton.setEnabled(true);
            status.setForeground(INK);
            status.setText(completed.result().status() + " | Report is a snapshot of the last successful assessment.");
        } catch (IllegalArgumentException e) {
            completed = null; exportButton.setEnabled(false);
            temperatureValue.setText("-- C"); humidityValue.setText("-- % RH"); conductivityValue.setText("-- uS/cm");
            preview.setContentType("text/plain"); preview.setText("No valid report. Correct the sensor input and run again.");
            trace.setText("Input rejected before assessment. No report was generated.");
            status.setForeground(new Color(150, 35, 35));
            status.setText("Input error: " + (e instanceof NumberFormatException ? "Use numeric values with a decimal point." : e.getMessage()));
        }
    }
    private void saveReport() {
        if (completed == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save conservation report");
        chooser.setSelectedFile(new java.io.File("tidekeeper-" + completed.result().artifact().id() + completed.extension()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.toString().toLowerCase(Locale.ROOT).endsWith(completed.extension()))
            path = Path.of(path + completed.extension());
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this, "Replace the existing report?", "Confirm replacement",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            Files.writeString(path, completed.report(), StandardCharsets.UTF_8);
            status.setForeground(INK); status.setText("Report saved: " + path.toAbsolutePath());
        } catch (IOException e) {
            status.setForeground(new Color(150, 35, 35)); status.setText("Could not save report: " + e.getMessage());
        }
    }

    /** Exercise the actual button action on the Swing event dispatch thread. */
    public void verifyWorkflow() {
        for (int s = 0; s < 2; s++) for (int a = 0; a < 2; a++) for (int r = 0; r < 2; r++) {
            sensor.setSelectedIndex(s); assessment.setSelectedIndex(a); renderer.setSelectedIndex(r);
            runButton.doClick();
            if (completed == null || !exportButton.isEnabled() || !trace.getText().contains(completed.rendererName()))
                throw new AssertionError("Frontend workflow failed.");
        }
        sensor.setSelectedIndex(0); packet.setText("invalid"); runButton.doClick();
        if (completed != null || exportButton.isEnabled()) throw new AssertionError("Invalid input was accepted.");
        packet.setText("68;55;0.35"); assessment.setSelectedIndex(0); renderer.setSelectedIndex(0); runButton.doClick();
    }
}
