package tidekeeper.vendor;

import java.util.Map;

/** A second simulated SDK, using Celsius, a humidity fraction, and uS/cm. */
public final class MetricStation {
    private final Map<String, Double> values;
    public MetricStation(Map<String, Double> values) { this.values = Map.copyOf(values); }
    public Map<String, Double> fetchMeasurements() { return values; }
}
