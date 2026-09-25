package tidekeeper.service;

import tidekeeper.adapter.SensorSource;
import tidekeeper.bridge.ConservationAssessment;
import tidekeeper.model.*;

/** Application use case: connect both patterns without knowing concrete vendor types. */
public final class AssessmentService {
    public record CompletedAssessment(AssessmentResult result, String report, String contentType,
                                      String extension, String adapterName, String assessmentName, String rendererName) { }
    public CompletedAssessment run(Artifact artifact, SensorSource source, ConservationAssessment assessment) {
        AssessmentResult result = assessment.evaluate(artifact, source.read());
        return new CompletedAssessment(result, assessment.createReport(result), assessment.renderer().contentType(),
                assessment.renderer().extension(), source.getClass().getSimpleName(),
                assessment.getClass().getSimpleName(), assessment.renderer().getClass().getSimpleName());
    }
}
