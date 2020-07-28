package waveengine.guiimplementation.cache;

import waveengine.guiimplementation.graphicalobject.GraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * This cache rounds parameters with given scale to avoid caching infinite amount of objects.
 */
public class GraphicsCacheImpl implements GraphicsCache {

    private double degreeStep = 0.5;
    private double pxStep = 0.5;
    private double alphaStep = 0.1;


    private final Map<GraphicalObject, Map<CacheParameters, BufferedImage>> cache = new HashMap<>();

    public GraphicsCacheImpl() {
    }

    public GraphicsCacheImpl(double degreeStep, double pxStep, double alphaStep) {
        this.degreeStep = degreeStep;
        this.pxStep = pxStep;
        this.alphaStep = alphaStep;
    }

    @Override
    public BufferedImage get(GraphicalObject graphicalObject, Parameters parameters) {

        if (graphicalObject.supportsCache()) {
            //todo
        }

        if (parameters.getScale() == 0) {
            //todo
        }

        if (parameters.getAlpha() == 0) {
            //todo
        }


        //assuming that scale changes smaller than 0.5px (or pxStep) are unnoticeable, we need to check how much it differs,
        //round it to this value and then recalculate scale back for caching purposes
        int biggerDimension = Math.max(graphicalObject.width(), graphicalObject.height());

        double approximatedSize = biggerDimension * parameters.getScale();
        approximatedSize = roundWithStep((float) approximatedSize, (float) pxStep);
        double recalculatedScale = approximatedSize / biggerDimension;

        CacheParameters cacheParameters = new CacheParameters(
                roundWithStep(parameters.getRotation(), (float) degreeStep),
                (float) recalculatedScale,
                roundWithStep(parameters.getAlpha(), (float) alphaStep)
        );

        if (cacheParameters.getScale() == 0) {
            //todo
        }

        if (cacheParameters.getAlpha() == 0) {
            //todo
        }


        if (cache.containsKey(graphicalObject)) {
            var goc = cache.get(graphicalObject);

            if (goc.containsKey(cacheParameters)) {
                return goc.get(cacheParameters);
            }
        } else {
            cache.put(graphicalObject, new HashMap<>());
        }

        double cosa = Math.cos(parameters.getRotation());
        double sina = Math.sin(parameters.getRotation());
        double oldWidth = graphicalObject.width();
        double oldHeight = graphicalObject.height();

        double newWidth = parameters.getScale() * (oldHeight * sina + oldWidth * cosa);
        double newHeight = parameters.getScale() * (oldHeight * cosa + oldWidth * sina);


        int newWidthInt = (int) Math.ceil(newWidth);
        int newHeightInt = (int) Math.ceil(newHeight);

        BufferedImage image = new BufferedImage(newWidthInt, newHeightInt, BufferedImage.TYPE_INT_ARGB);


        Graphics2D g2 = image.createGraphics();

        graphicalObject.renderCache(g2, cacheParameters.getRotation(), cacheParameters.getScale(), cacheParameters.getAlpha());

        g2.dispose();

        cache.get(graphicalObject).put(cacheParameters, image);

        return image;
    }

    private static float roundWithStep(float input, float step) {
        return ((Math.round(input / step)) * step);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
