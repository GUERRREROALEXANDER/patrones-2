package tidekeeper.bridge;

import java.util.Locale;
import tidekeeper.model.AssessmentResult;

public final class PlainTextRenderer implements ReportRenderer {
    @Override public String render(AssessmentResult r) {
        return String.format(Locale.ROOT,
                "TIDEKEEPER | CONSERVATION NOTE%n%n%s%n%s | %s | %s%n%nStatus: %s%n%nTemperature: %.1f C%nHumidity: %.1f %% RH%nConductivity: %.1f uS/cm%n%n%s%n%nEducational simulation. Not a treatment prescription.%n",
                r.assessment(), r.artifact().id(), r.artifact().name(), r.artifact().material(), r.status(),
                r.reading().temperatureCelsius(), r.reading().relativeHumidityPercent(),
                r.reading().conductivityMicrosiemens(), String.join("\n\n", r.observations()));
    }
    @Override public String contentType() { return "text/plain"; }
    @Override public String extension() { return ".txt"; }
}
