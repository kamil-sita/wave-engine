package waveengine.guiimplementation.renderingparameters;

public class Parameters {
    private float x;
    private float y;
    private float rotation = 0;
    private float alpha = 1;
    private float scale = 1;
    private boolean isVisible = true;
    private int modCount = 0;
    private Positioning positioning = Positioning.ABSOLUTE;

    public int getModCount() {
        return modCount;
    }

    public Parameters(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public Parameters setX(float x) {
        modCount++;
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Parameters setY(float y) {
        modCount++;
        this.y = y;
        return this;
    }

    public float getRotation() {
        return rotation;
    }

    public Parameters setRotation(float rotation) {
        modCount++;
        this.rotation = rotation;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }

    public Parameters setAlpha(float alpha) {
        modCount++;
        this.alpha = alpha;
        return this;
    }

    public float getScale() {
        return scale;
    }

    public Parameters setScale(float scale) {
        modCount++;
        this.scale = scale;
        return this;
    }

    public Positioning getPositioning() {
        return positioning;
    }

    public Parameters setPositioning(Positioning positioning) {
        this.positioning = positioning;
        modCount++;
        return this;
    }

    public boolean isVisible() {
        return isVisible && (scale != 0) && (alpha != 0);
    }

    public boolean requiresAlpha() {
        return alpha != 1;
    }

    public boolean requiresScale() {
        return scale != 1;
    }

    public Parameters setVisible(boolean visible) {
        modCount++;
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

    @Override
    public String toString() {
        return "Parameters{" +
                "x=" + x +
                ", y=" + y +
                ", rotation=" + rotation +
                ", alpha=" + alpha +
                ", scale=" + scale +
                ", isVisible=" + isVisible +
                ", modCount=" + modCount +
                '}';
    }
}
