package main;

public class ComponentPosition {
    private double it = 0;

    private int x;
    private int y;
    private double size = 60 * Math.random() + 20;
    private double spdModifier = (3 * Math.random() + 0.5);

    public ComponentPosition() {
        x = (int) (Math.random() * 1600);
        y = (int) (Math.random() * 900);
    }

    public void update(double val) {
        it += val * spdModifier;
    }

    public double getX() {
        return x + size * Math.sin(it) * Math.sin(it * 0.01 + 1.6);
    }

    public double getY() {
        return y + size * Math.cos(it) * Math.sin(it * 0.01 + 1.6);
    }
}
