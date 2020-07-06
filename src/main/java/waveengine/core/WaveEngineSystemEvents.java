package waveengine.core;

import waveengine.Discriminator;

public enum WaveEngineSystemEvents implements Discriminator {
    STAGE_CHANGED,
    EXCEPTION_WITH_SYSTEM,
    WINDOW_CLOSE_REQUEST
}
