package main;

import waveengine.library.systems.ResourceLocation;

public enum Resource implements ResourceLocation {
    TEST_RESOURCE("point.png");

    Resource(String loc) {
        this.loc = loc;
    }

    private String loc;

    @Override
    public String getResourceLocation() {
        return loc;
    }
}
