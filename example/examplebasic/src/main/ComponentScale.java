package main;

public class ComponentScale {
    private double scale = 1.0;
    private double iteration = 0;

    public double getScale() {
        return scale;
    }

    public void iterate(double delta) {
        iteration += delta;
        scale = 0.5 + 0.5 * Math.sin(iteration);
    }
}
