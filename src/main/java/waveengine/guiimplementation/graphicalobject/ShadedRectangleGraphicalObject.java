package waveengine.guiimplementation.graphicalobject;

import java.awt.*;

public class ShadedRectangleGraphicalObject implements GraphicalObject {
    private Color mainColor;
    private Color additionalColor;

    private int width;
    private int height;

    public ShadedRectangleGraphicalObject(Color mainColor, Color additionalColor, int width, int height) {
        this.mainColor = mainColor;
        this.additionalColor = additionalColor;
        this.width = width;
        this.height = height;
    }

    //todo private
    public Color getMainColor() {
        return mainColor;
    }

    public Color getAdditionalColor() {
        return additionalColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
