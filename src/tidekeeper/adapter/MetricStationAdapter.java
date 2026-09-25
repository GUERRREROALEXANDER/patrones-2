package tidekeeper.adapter;

import java.util.Map;
import java.util.Objects;
import tidekeeper.model.Reading;
import tidekeeper.vendor.MetricStation;

/** Object adapter for the metric station's map and fractional humidity. */
public final class MetricStationAdapter implements SensorSource {
    private final MetricStation station;
    public MetricStationAdapter(MetricStation station) { this.station = Objects.requireNonNull(station); }
    @Override public Reading read() {
        Map<String, Double> data = station.fetchMeasurements();
        return new Reading(required(data, "temperature_c"), required(data, "humidity_ratio") * 100,
                required(data, "conductivity_us"));
    }
    private double required(Map<String, Double> data, String key) {
        Double value = data.get(key);
        if (value == null) throw new IllegalArgumentException("Station measurement is missing: " + key);
        return value;
    }
}
