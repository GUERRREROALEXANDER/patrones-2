package tidekeeper.adapter;

import tidekeeper.model.Reading;

/** Adapter target: clients never depend on vendor formats or units. */
public interface SensorSource {
    Reading read();
}
