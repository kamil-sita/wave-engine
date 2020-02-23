package waveengine.core;

public enum UpdatePolicy {
    /**
     * System with this update policy will be updated after rendering a frame. Be aware that if this system
     * takes too long, drawFrame might be called late.
     */
    UPDATE_AFTER_DRAWING_FRAME_PARALLEL,
    /**
     * System with this update policy will be updated with according UPS setting. Parallel systems are independent from
     * one another, and one working slower will not slow others.
     */
    UPDATE_PARALLEL,
    /**
     * System with this update policy will never be updated automatically by scheduler, however it still exists as a system
     * and could be triggered by for example NotifyingService.
     */
    NEVER
}
