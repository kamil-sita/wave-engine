package waveengine.guiimplementation.renderingparameters;

public enum Positioning {
    RELATIVE,
    ABSOLUTE_TOP_LEFT;

    public static int X_START = 0;
    public static int X_END = 1;
    public static int Y_START = 2;
    public static int Y_END = 3;


    public void getBounds(float[] boundsArray, float x, float y, float width, float height, float projectWidth, float projectHeight) {
        if (this == RELATIVE) {
            getBoundsRelative(boundsArray, x, y, width, height, projectWidth, projectHeight);
        } else if (this == ABSOLUTE_TOP_LEFT) {
            getBoundsAbsolute(boundsArray, x, y, width, height);
        } else {
            throw new RuntimeException("Positioning does not have getBounds implementation");
        }
    }

    private void getBoundsRelative(float[] boundsArray, float x, float y, float width, float height, float projectWidth, float projectHeight) {
        boundsArray[X_START] = x - (width/2);
        boundsArray[X_END] = x + (width/2);
        boundsArray[Y_START] = y - (height/2);
        boundsArray[Y_END] = y + (height/2);

        boundsArray[X_START] = normalize(boundsArray[X_START], projectWidth);
        boundsArray[X_END] = normalize(boundsArray[X_END], projectWidth);
        boundsArray[Y_START] = normalize(boundsArray[Y_START], projectHeight);
        boundsArray[Y_END] = normalize(boundsArray[Y_END], projectHeight);
    }

    private float normalize(float value, float max) {
        float v = value / max;
        return (v * 2) - 1;
    }

    private void getBoundsAbsolute(float[] boundsArray, float x, float y, float width, float height) {
        boundsArray[X_START] = x - (width/2);
        boundsArray[X_END] = x + (width/2);
        boundsArray[Y_START] = y - (height/2);
        boundsArray[Y_END] = y + (height/2);
    }

}
