package tidekeeper.vendor;

/** Simulated, incompatible vendor SDK. Its values use Fahrenheit and mS/cm. */
public final class LegacyProbe {
    private final String packet;
    public LegacyProbe(String packet) { this.packet = packet; }
    public String downloadPacket() { return packet; }
}
