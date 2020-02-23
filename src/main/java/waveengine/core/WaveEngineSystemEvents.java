package waveengine.core;

import waveengine.Discriminator;

public enum WaveEngineSystemEvents implements Discriminator {
    LOW_ON_MEMORY,
    STAGE_CHANGED,
    WINDOW_CLOSE_REQUEST
}
