package waveengine;

import java.awt.*;

//todo listeners for change
public class WaveEngineRuntimeSettings {
    public int getTargetFramerate() {
        return 144;
    }

    public int getTargetUPS() {
        return 144;
    }

    public String windowName() {
        return "TEST";
    }

    public Color repaintColor() {
        return Color.WHITE;
    }
}
