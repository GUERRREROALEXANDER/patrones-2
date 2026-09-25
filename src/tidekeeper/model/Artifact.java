package tidekeeper.model;

import java.util.Objects;

/** A fictional collection item inspired by maritime archaeological conservation. */
public record Artifact(String id, String name, String material) {
    public Artifact {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(material);
    }
    @Override public String toString() { return id + " | " + name; }
}
