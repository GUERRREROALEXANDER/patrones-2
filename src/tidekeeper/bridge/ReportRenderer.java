package tidekeeper.bridge;

import tidekeeper.model.AssessmentResult;

/** Bridge implementor: presentation can evolve independently of assessment logic. */
public interface ReportRenderer {
    String render(AssessmentResult result);
    String contentType();
    String extension();
}
