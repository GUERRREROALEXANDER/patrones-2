# Architecture

```mermaid
classDiagram
    class SensorSource {
        <<interface>>
        +read() Reading
    }
    class LegacyProbe {
        +downloadPacket() String
    }
    class MetricStation {
        +fetchMeasurements() Map
    }
    SensorSource <|.. LegacyProbeAdapter
    SensorSource <|.. MetricStationAdapter
    LegacyProbeAdapter *-- LegacyProbe : wraps
    MetricStationAdapter *-- MetricStation : wraps
    class ConservationAssessment {
        <<abstract>>
        -ReportRenderer renderer
        +evaluate(Artifact, Reading) AssessmentResult
        +createReport(AssessmentResult) String
    }
    class ReportRenderer {
        <<interface>>
        +render(AssessmentResult) String
        +contentType() String
        +extension() String
    }
    ConservationAssessment <|-- StorageAssessment
    ConservationAssessment <|-- DesalinationAssessment
    ConservationAssessment o-- ReportRenderer : bridge
    ReportRenderer <|.. HtmlRenderer
    ReportRenderer <|.. PlainTextRenderer
    Dashboard --> AssessmentService : runs
    AssessmentService --> SensorSource : reads
    AssessmentService --> ConservationAssessment : evaluates and renders
    AssessmentService --> AssessmentResult : returns snapshot
    SensorSource --> Reading
    ConservationAssessment --> Artifact
    ConservationAssessment --> AssessmentResult
```

## Runtime example

```text
Dashboard
  -> AssessmentService.run(artifact, source, assessment)
       -> LegacyProbeAdapter.read()
            -> LegacyProbe.downloadPacket(): "68;55;0.35"
            -> Reading(20 C, 55% RH, 350 uS/cm)
       -> StorageAssessment.evaluate(artifact, reading)
            -> AssessmentResult
       -> ConservationAssessment.createReport(result)
            -> HtmlRenderer.render(result)
  -> Show normalized metrics, report, and pattern trace
```

Both patterns participate in every successful frontend assessment. Selecting another sensor changes Adapter composition; selecting another assessment or report format changes one axis of the Bridge.
