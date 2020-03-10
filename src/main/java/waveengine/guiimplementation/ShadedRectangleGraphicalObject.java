package waveengine.guiimplementation;

import java.awt.*;

public class ShadedRectangleGraphicalObject implements GraphicalObject {
    Color mainColor;
    Color additionalColor;

    int width;
    int height;

    public ShadedRectangleGraphicalObject(Color mainColor, Color additionalColor, int width, int height) {
        this.mainColor = mainColor;
        this.additionalColor = additionalColor;
        this.width = width;
        this.height = height;
    }


}
