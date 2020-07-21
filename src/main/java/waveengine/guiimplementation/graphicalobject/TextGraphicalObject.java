package waveengine.guiimplementation.graphicalobject;


import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.guiimplementation.renderingparameters.Positioning;

import java.awt.*;

public class TextGraphicalObject implements GraphicalObject {
    private final String text;
    private final int size;
    private final Color color;
    private final String font;
    private final int style; //Font.* style
    private final Font fontObj;

    public TextGraphicalObject(String text, int size, Color color, String font, int style) {
        this.text = text;
        this.size = size;
        this.color = color;
        this.font = font;
        this.style = style;
        this.fontObj = new Font(font, style, size);
    }

    public TextGraphicalObject(String text, int size, Color color, String font) {
        this(text, size, color, font, Font.BOLD);
    }

    public TextGraphicalObject(String text, int size, Color color) {
        this(text, size, color, "Arial");
    }

    @Override
    public void render(Graphics2D graphics2D, Parameters parameters) {
        if (parameters.getPositioning() != Positioning.ABSOLUTE) {
            return; //not yet implemented
        }

        graphics2D.setFont(fontObj);
        graphics2D.setColor(color);
        graphics2D.drawString(text, parameters.getX(), parameters.getY());
    }
}
