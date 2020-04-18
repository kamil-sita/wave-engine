package waveengine.core;

import waveengine.Discriminator;

public enum WaveEngineSystemEvents implements Discriminator {
    LOW_ON_MEMORY,
    STAGE_CHANGED,
    EXCEPTION_WITH_SYSTEM,
    WINDOW_CLOSE_REQUEST
}
