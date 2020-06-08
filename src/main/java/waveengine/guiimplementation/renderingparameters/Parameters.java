package waveengine.guiimplementation.renderingparameters;

public class Parameters {
    private float x;
    private float y;
    private float rotation = 0;
    private float alpha = 1;
    private float scale = 1;
    private boolean isVisible = true;

    public Parameters(float x, float y) {
        this.x = x;
        this.y = y;
    }

    //




    //todo helper functions: position from top left, from center

    public float getX() {
        return x;
    }

    public Parameters setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Parameters setY(float y) {
        this.y = y;
        return this;
    }

    public float getRotation() {
        return rotation;
    }

    public Parameters setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }

    public Parameters setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public float getScale() {
        return scale;
    }

    public Parameters setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Parameters setVisible(boolean visible) {
        isVisible = visible;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameters that = (Parameters) o;

        if (Float.compare(that.x, x) != 0) return false;
        if (Float.compare(that.y, y) != 0) return false;
        if (Float.compare(that.rotation, rotation) != 0) return false;
        if (Float.compare(that.alpha, alpha) != 0) return false;
        if (Float.compare(that.scale, scale) != 0) return false;
        return isVisible == that.isVisible;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (rotation != +0.0f ? Float.floatToIntBits(rotation) : 0);
        result = 31 * result + (alpha != +0.0f ? Float.floatToIntBits(alpha) : 0);
        result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
        result = 31 * result + (isVisible ? 1 : 0);
        return result;
    }
}
