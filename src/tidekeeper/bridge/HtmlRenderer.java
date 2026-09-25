package tidekeeper.bridge;

import java.util.Locale;
import java.util.stream.Collectors;
import tidekeeper.model.AssessmentResult;

public final class HtmlRenderer implements ReportRenderer {
    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    @Override public String render(AssessmentResult r) {
        return String.format(Locale.ROOT, """
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:Segoe UI,sans-serif;color:#183b46;background:#ffffff;margin:24px">
                <p style="color:#326a6b">TIDEKEEPER / CONSERVATION NOTE</p>
                <h1>%s</h1><p>%s | %s | %s</p>
                <h2>%s</h2><hr>
                <p>Temperature: <b>%.1f C</b> &nbsp; Humidity: <b>%.1f %% RH</b></p>
                <p>Conductivity: <b>%.1f uS/cm</b></p><ul>%s</ul><hr>
                <p>Educational simulation. Not a treatment prescription.</p></body></html>
                """, escape(r.assessment()), escape(r.artifact().id()), escape(r.artifact().name()),
                escape(r.artifact().material()), escape(r.status()), r.reading().temperatureCelsius(),
                r.reading().relativeHumidityPercent(), r.reading().conductivityMicrosiemens(),
                r.observations().stream().map(n -> "<li>" + escape(n) + "</li>").collect(Collectors.joining()));
    }
    @Override public String contentType() { return "text/html"; }
    @Override public String extension() { return ".html"; }
}
