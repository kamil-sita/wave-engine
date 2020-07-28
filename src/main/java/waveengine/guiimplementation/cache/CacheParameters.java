package waveengine.guiimplementation.cache;

import java.util.Objects;

public class CacheParameters {
    private float rotation;
    private float scale;
    private float alpha;

    public CacheParameters(float rotation, float scale, float alpha) {
        this.rotation = rotation;
        this.scale = scale;
        this.alpha = alpha;
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheParameters that = (CacheParameters) o;
        return Float.compare(that.rotation, rotation) == 0 &&
                Float.compare(that.scale, scale) == 0 &&
                Float.compare(that.alpha, alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotation, scale, alpha);
    }
}
