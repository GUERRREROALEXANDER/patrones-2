package tidekeeper.model;

import java.util.List;

public record AssessmentResult(String assessment, Artifact artifact, Reading reading,
                               String status, List<String> observations) {
    public AssessmentResult { observations = List.copyOf(observations); }
}
