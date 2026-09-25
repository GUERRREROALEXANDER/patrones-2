package tidekeeper.bridge;

import java.util.List;
import tidekeeper.model.*;

/** Another refined abstraction, usable with every ReportRenderer implementation. */
public final class DesalinationAssessment extends ConservationAssessment {
    public DesalinationAssessment(ReportRenderer renderer) { super(renderer); }
    @Override public AssessmentResult evaluate(Artifact artifact, Reading reading) {
        if (artifact.material().equals("Wood"))
            return new AssessmentResult("Desalination bath", artifact, reading, "NOT APPLICABLE",
                    List.of("This classroom bath screen only supports iron and ceramic items.",
                            "Wood requires a separate conservation protocol."));
        boolean review = reading.conductivityMicrosiemens() > 500;
        return new AssessmentResult("Desalination bath", artifact, reading,
                review ? "REVIEW" : "WITHIN DEMO RANGE", List.of(
                review ? "Conductivity exceeds the fictional 500 uS/cm classroom threshold."
                       : "Conductivity is at or below the fictional 500 uS/cm classroom threshold.",
                "One reading cannot determine treatment completion. A real protocol requires trends and specialist interpretation."));
    }
}
