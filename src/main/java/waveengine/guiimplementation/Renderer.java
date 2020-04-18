package waveengine.guiimplementation;

import waveengine.core.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;

public final class Renderer {
    private static boolean firstLogged = false;
    public static void render(Graphics2D canvas, GraphicalObject graphicalObject, Parameters parameters) {

        if (!parameters.isVisible()) return;

        if (graphicalObject instanceof ComposedGraphicalObject) {
            var graphicalObjects = ((ComposedGraphicalObject) graphicalObject).getGraphicalObjectList();
            for (var graphicalObjectElement : graphicalObjects) {
                render(canvas, graphicalObjectElement, parameters);
            }
            return;
        }

        if (graphicalObject instanceof ImageGraphicalObject) {
            render(canvas, (ImageGraphicalObject) graphicalObject, parameters);
            return;
        }

        if (graphicalObject instanceof ShadedRectangleGraphicalObject) {
            render(canvas, (ShadedRectangleGraphicalObject) graphicalObject, parameters);
            return;
        }

        cannotRender(graphicalObject);

    }

    private static void render(Graphics2D canvas, ShadedRectangleGraphicalObject graphicalObject, Parameters parameters) {
        var paint = canvas.getPaint();
        canvas.setPaint(graphicalObject.additionalColor);
        int xStart = (int) (parameters.getX() - graphicalObject.width/2);
        int yStart = (int) (parameters.getY() - graphicalObject.height/2);
        canvas.fillRect(xStart - 1, yStart - 1, graphicalObject.width + 2, graphicalObject.height + 4);
        canvas.setPaint(graphicalObject.mainColor);
        canvas.fillRect(xStart, yStart, graphicalObject.width, graphicalObject.height);
        canvas.setPaint(paint);
    }

    private static void render(Graphics2D canvas, ImageGraphicalObject graphicalObject, Parameters parameters) {
        if (!requiresAlpha(parameters) && !requiresScale(parameters)) {
            var image = graphicalObject.bufferedImage;

            AffineTransform affineTransform = new AffineTransform();
            affineTransform.translate(parameters.getX() - image.getWidth()/2.0, parameters.getY() - image.getHeight()/2.0);
            final double rotateConst = Math.PI / 180.0;
            affineTransform.rotate(parameters.getRotation() * rotateConst);
            canvas.drawImage(image, affineTransform, null);
            return;
        }
        if (requiresScale(parameters) && !requiresAlpha(parameters)) {
            var image = graphicalObject.bufferedImage;

            AffineTransform affineTransform = new AffineTransform();
            affineTransform.translate(parameters.getX(), parameters.getY());
            final double rotateConst = Math.PI / 180.0;
            affineTransform.rotate(parameters.getRotation() * rotateConst);
            affineTransform.scale(parameters.getScale(), parameters.getScale());
            canvas.drawImage(image, affineTransform, null);
            return;
        }
        cannotRender(graphicalObject);
    }

    private static void cannotRender(GraphicalObject graphicalObject) {

        if (!firstLogged) {
            Logger.getLogger().logWarning("Could not render object " + graphicalObject + ". Will not report any more problems from renderer");
            firstLogged = true;
        }
    }

    private static boolean requiresScale(Parameters parameters) {
        return parameters.getScale() != 1.0;
    }

    private static boolean requiresAlpha(Parameters parameters) {
        return parameters.getAlpha() != 1.0;
    }

}
