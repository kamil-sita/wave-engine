package waveengine.core;

public enum UpdatePolicy {
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
