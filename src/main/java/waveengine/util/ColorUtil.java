package waveengine.util;

import java.awt.*;

public class ColorUtil {

    public static final int R = 0;
    public static final int G = 1;
    public static final int B = 2;
    public static final int A = 3;


    public static void awtColorToRgb4f(Color color, float[] colorArray) {
        colorArray[R] = (color.getRed()/255.0f);
        colorArray[G] = (color.getGreen()/255.0f);
        colorArray[B] = (color.getBlue()/255.0f);
        colorArray[A] = (color.getAlpha()/255.0f);
    }
}
