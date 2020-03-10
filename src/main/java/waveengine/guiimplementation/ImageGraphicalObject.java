package waveengine.guiimplementation;


import waveengine.library.systems.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageGraphicalObject implements GraphicalObject {


    private static GraphicsConfiguration graphicsConfiguration;

    public static ImageGraphicalObject load(ResourceLocation location) {
        URL url = ImageGraphicalObject.class.getClassLoader().getResource(location.getResourceLocation());
        try {
            var image = ImageIO.read(url);
            if (graphicsConfiguration == null) {
                graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            }
            if (!image.getColorModel().equals(graphicsConfiguration.getColorModel())) {
                BufferedImage newImage = graphicsConfiguration.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
                Graphics2D graphics2D = newImage.createGraphics();
                graphics2D.drawImage(image, 0, 0, null);
                graphics2D.dispose();
                image = newImage;
            }
            return new ImageGraphicalObject(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    BufferedImage bufferedImage;

    private ImageGraphicalObject(BufferedImage image) {
        this.bufferedImage = image;
    }

    @Override
    public void dispose() {

    }
}
