package tidekeeper;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Container;
import java.awt.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import tidekeeper.adapter.*;
import tidekeeper.bridge.*;
import tidekeeper.model.*;
import tidekeeper.service.AssessmentService;
import tidekeeper.ui.Dashboard;
import tidekeeper.vendor.*;

/** Dependency-free contract and integration tests; failures always throw. */
public final class PatternTests {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void rejected(Runnable operation) {
        boolean failed = false;
        try { operation.run(); } catch (IllegalArgumentException e) { failed = true; }
        check(failed, "Expected invalid input rejection");
    }
    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) if (child instanceof Container container) layout(container);
    }
    public static void main(String[] args) throws Exception {
        SensorSource legacy = new LegacyProbeAdapter(new LegacyProbe("68;55;0.35"));
        SensorSource metric = new MetricStationAdapter(new MetricStation(Map.of(
                "temperature_c", 20.0, "humidity_ratio", 0.55, "conductivity_us", 350.0)));
        Reading l = legacy.read(), m = metric.read();
        check(Math.abs(l.temperatureCelsius() - m.temperatureCelsius()) < 0.00001, "Temperature conversion");
        check(Math.abs(l.relativeHumidityPercent() - m.relativeHumidityPercent()) < 0.00001, "Humidity conversion");
        check(Math.abs(l.conductivityMicrosiemens() - m.conductivityMicrosiemens()) < 0.00001, "Conductivity conversion");
        rejected(() -> new LegacyProbeAdapter(new LegacyProbe("broken")).read());
        rejected(() -> new LegacyProbeAdapter(new LegacyProbe("a;55;1")).read());
        rejected(() -> new LegacyProbeAdapter(new LegacyProbe("68;55;1;extra")).read());
        rejected(() -> new Reading(Double.NaN, 55, 350));
        rejected(() -> new Reading(20, 101, 350));
        rejected(() -> new Reading(20, 50, -1));
        rejected(() -> new Reading(20, 50, Double.POSITIVE_INFINITY));
        rejected(() -> new MetricStationAdapter(new MetricStation(Map.of())).read());
        Artifact iron = new Artifact("TEST-1", "Iron ring", "Iron");
        AssessmentService service = new AssessmentService();
        for (SensorSource source : new SensorSource[]{legacy, metric}) {
            for (ReportRenderer renderer : new ReportRenderer[]{new HtmlRenderer(), new PlainTextRenderer()}) {
                for (ConservationAssessment assessment : new ConservationAssessment[]{new StorageAssessment(renderer), new DesalinationAssessment(renderer)}) {
                    var result = service.run(iron, source, assessment);
                    check(result.result().status().equals("WITHIN DEMO RANGE"), "All eight combinations must agree");
                    check(result.report().contains("Iron ring"), "Report contains selected artifact");
                }
            }
        }
        ConservationAssessment storage = new StorageAssessment(new PlainTextRenderer());
        check(storage.evaluate(iron, new Reading(16, 45, 0)).status().equals("WITHIN DEMO RANGE"), "Lower boundary");
        check(storage.evaluate(iron, new Reading(22, 60, 0)).status().equals("WITHIN DEMO RANGE"), "Upper boundary");
        check(storage.evaluate(iron, new Reading(23, 60, 0)).status().equals("REVIEW"), "Temperature alert");
        check(storage.evaluate(iron, new Reading(20, 61, 0)).status().equals("REVIEW"), "Humidity alert");
        ConservationAssessment bath = new DesalinationAssessment(new HtmlRenderer());
        check(bath.evaluate(iron, new Reading(20, 55, 500)).status().equals("WITHIN DEMO RANGE"), "Conductivity boundary");
        check(bath.evaluate(iron, new Reading(20, 55, 501)).status().equals("REVIEW"), "Conductivity alert");
        check(bath.evaluate(new Artifact("W", "Wood block", "Wood"), l).status().equals("NOT APPLICABLE"), "Material restriction");
        String escaped = service.run(new Artifact("X", "<script>&", "Iron"), legacy, bath).report();
        check(!escaped.contains("<script>") && escaped.contains("&lt;script&gt;&amp;"), "HTML escaping");
        SwingUtilities.invokeAndWait(() -> {
            Dashboard dashboard = new Dashboard();
            dashboard.verifyWorkflow();
            dashboard.setSize(1200, 820);
            layout(dashboard);
            BufferedImage image = new BufferedImage(1200, 820, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            dashboard.printAll(graphics); graphics.dispose();
            try {
                Files.createDirectories(Path.of("docs"));
                ImageIO.write(image, "png", Path.of("docs", "dashboard-preview.png").toFile());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: " + checks + " domain checks; eight frontend combinations; invalid-input recovery; Swing preview rendered.");
    }
}
