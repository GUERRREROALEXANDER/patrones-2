package tidekeeper.adapter;

import java.util.Objects;
import tidekeeper.model.Reading;
import tidekeeper.vendor.LegacyProbe;

/** Object adapter: wraps the legacy SDK and converts its packet and units. */
public final class LegacyProbeAdapter implements SensorSource {
    private final LegacyProbe probe;
    public LegacyProbeAdapter(LegacyProbe probe) { this.probe = Objects.requireNonNull(probe); }
    @Override public Reading read() {
        String[] fields = probe.downloadPacket().split(";", -1);
        if (fields.length != 3)
            throw new IllegalArgumentException("Legacy packet requires temperature F;humidity %;conductivity mS/cm.");
        try {
            double celsius = (Double.parseDouble(fields[0].trim()) - 32) * 5 / 9;
            return new Reading(celsius, Double.parseDouble(fields[1].trim()),
                    Double.parseDouble(fields[2].trim()) * 1000);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Every legacy packet field must be numeric.", e);
        }
    }
}
