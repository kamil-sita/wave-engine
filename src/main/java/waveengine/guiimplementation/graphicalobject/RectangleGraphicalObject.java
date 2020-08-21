package waveengine.guiimplementation.graphicalobject;

import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.guiimplementation.renderingparameters.Positioning;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static waveengine.util.ColorUtil.*;

public class RectangleGraphicalObject implements GraphicalObject {
    private Color mainColor;
    private Color additionalColor;

    private static final float[] colorArray = new float[4];

    private int width;
    private int height;

    public RectangleGraphicalObject(Color mainColor, Color additionalColor, int width, int height) {
        this.mainColor = mainColor;
        this.additionalColor = additionalColor;
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public void render(Parameters parameters, float[] bounds) {

        awtColorToRgb4f(mainColor, colorArray);
        glColor4f(colorArray[R], colorArray[G], colorArray[B], colorArray[A]);

        glBegin(GL_QUADS);
        {
           glVertex2f(bounds[Positioning.X_START], bounds[Positioning.Y_END]);
           glVertex2f(bounds[Positioning.X_END], bounds[Positioning.Y_END]);
           glVertex2f(bounds[Positioning.X_END], bounds[Positioning.Y_START]);
           glVertex2f(bounds[Positioning.X_START], bounds[Positioning.Y_START]);
        }
        glEnd();
    }
}
