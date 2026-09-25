package tidekeeper.bridge;

import java.util.ArrayList;
import java.util.List;
import tidekeeper.model.*;

/** Refined abstraction. All limits are fictional classroom demonstration values. */
public final class StorageAssessment extends ConservationAssessment {
    public StorageAssessment(ReportRenderer renderer) { super(renderer); }
    @Override public AssessmentResult evaluate(Artifact artifact, Reading reading) {
        List<String> notes = new ArrayList<>();
        if (reading.temperatureCelsius() < 16 || reading.temperatureCelsius() > 22)
            notes.add("Temperature is outside the demo storage band (16-22 C).");
        if (reading.relativeHumidityPercent() < 45 || reading.relativeHumidityPercent() > 60)
            notes.add("Humidity is outside the demo storage band (45-60%).");
        boolean review = !notes.isEmpty();
        if (!review) notes.add("Temperature and humidity are inside the demo storage bands.");
        notes.add("Storage screening assumes a previously treated item; it does not apply to untreated waterlogged wood.");
        return new AssessmentResult("Storage climate", artifact, reading, review ? "REVIEW" : "WITHIN DEMO RANGE", notes);
    }
}
