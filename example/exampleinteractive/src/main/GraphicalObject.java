package main;

import waveengine.guiimplementation.Parameters;

public class GraphicalObject {
    private Parameters parameters = new Parameters(0, 0);

    public void setX(float val) {
        parameters.setX(val);
    }
    public void setY(float val) {
        parameters.setY(val);
    }

    public Parameters getParameters() {
        return parameters;
    }
}
