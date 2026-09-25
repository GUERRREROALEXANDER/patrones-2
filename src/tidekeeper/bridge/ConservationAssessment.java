package tidekeeper.bridge;

import java.util.Objects;
import tidekeeper.model.Artifact;
import tidekeeper.model.AssessmentResult;
import tidekeeper.model.Reading;

/** Bridge abstraction; holds a renderer rather than inheriting its implementation. */
public abstract class ConservationAssessment {
    private final ReportRenderer renderer;
    protected ConservationAssessment(ReportRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
    }
    public abstract AssessmentResult evaluate(Artifact artifact, Reading reading);
    public final String createReport(AssessmentResult result) { return renderer.render(result); }
    public final ReportRenderer renderer() { return renderer; }
}
