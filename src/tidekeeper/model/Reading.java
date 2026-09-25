package tidekeeper.model;

/** Canonical units shared by all adapters and assessments. */
public record Reading(double temperatureCelsius, double relativeHumidityPercent,
                      double conductivityMicrosiemens) {
    public Reading {
        if (!Double.isFinite(temperatureCelsius) || temperatureCelsius < -50 || temperatureCelsius > 100)
            throw new IllegalArgumentException("Temperature must be between -50 and 100 degrees Celsius.");
        if (!Double.isFinite(relativeHumidityPercent) || relativeHumidityPercent < 0 || relativeHumidityPercent > 100)
            throw new IllegalArgumentException("Relative humidity must be between 0 and 100 percent.");
        if (!Double.isFinite(conductivityMicrosiemens) || conductivityMicrosiemens < 0)
            throw new IllegalArgumentException("Conductivity must be a finite, non-negative number.");
    }
}
