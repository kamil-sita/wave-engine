package waveengine.guiimplementation;

import waveengine.core.WaveEngineRunning;
import waveengine.guiimplementation.graphicalobject.*;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;
import java.awt.geom.AffineTransform;

public final class RendererImpl implements Renderer {
    private final WaveEngineRunning waveEngineRunning;

    public RendererImpl(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public void render(Graphics2D canvas, GraphicalObject graphicalObject, Parameters parameters, GraphicsCache cache) {

        if (!parameters.isVisible()) return;

        if (graphicalObject instanceof ComposedGraphicalObject) {
            var graphicalObjects = ((ComposedGraphicalObject) graphicalObject).getGraphicalObjectList();
            for (var graphicalObjectElement : graphicalObjects) {
                render(canvas, graphicalObjectElement, parameters, cache);
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

        if (graphicalObject instanceof TextGraphicalObject) {
            graphicalObject.render(canvas, parameters);
            return;
        }

        cannotRender(graphicalObject, parameters);

    }

    private void render(Graphics2D canvas, ShadedRectangleGraphicalObject graphicalObject, Parameters parameters) {
        var paint = canvas.getPaint();
        canvas.setPaint(graphicalObject.getAdditionalColor());
        int xStart = (int) (parameters.getX() - graphicalObject.getWidth()/2);
        int yStart = (int) (parameters.getY() - graphicalObject.getHeight()/2);
        canvas.fillRect(xStart - 1, yStart - 1, graphicalObject.getWidth() + 2, graphicalObject.getHeight() + 4);
        canvas.setPaint(graphicalObject.getMainColor());
        canvas.fillRect(xStart, yStart, graphicalObject.getWidth(), graphicalObject.getHeight());
        canvas.setPaint(paint);
    }

    private void render(Graphics2D canvas, ImageGraphicalObject graphicalObject, Parameters parameters) {
        if (!parameters.requiresAlpha() && !requiresScale(parameters)) {
            var image = graphicalObject.getBufferedImage();

            AffineTransform affineTransform = new AffineTransform();
            affineTransform.translate(parameters.getX() - image.getWidth()/2.0, parameters.getY() - image.getHeight()/2.0);
            final double rotateConst = Math.PI / 180.0;
            affineTransform.rotate(parameters.getRotation() * rotateConst);
            canvas.drawImage(image, affineTransform, null);
            return;
        }
        if (parameters.requiresScale() && !parameters.requiresAlpha()) {
            var image = graphicalObject.getBufferedImage();

            AffineTransform affineTransform = new AffineTransform();
            affineTransform.translate(parameters.getX(), parameters.getY());
            final double rotateConst = Math.PI / 180.0;
            affineTransform.rotate(parameters.getRotation() * rotateConst);
            affineTransform.scale(parameters.getScale(), parameters.getScale());
            canvas.drawImage(image, affineTransform, null);
            return;
        }
        cannotRender(graphicalObject, parameters);
    }

    private void cannotRender(GraphicalObject graphicalObject, Parameters parameters) {
        waveEngineRunning.shutdown("Could not render object " + graphicalObject + " with parameters " + parameters + ".", true);
    }

    private static boolean requiresScale(Parameters parameters) {
        return parameters.getScale() != 1.0;
    }


}
