package main.tables;

public class PhysicalAttributes {
    public float x;
    public float y;

    private float initialX;
    private float initialY;

    private float iteration = 0;

    public PhysicalAttributes() {
        initialX = (float) (Math.random() * 1600);
        initialY = (float) (Math.random() * 900);
    }

    public void update(double deltaTime) {
        iteration += deltaTime;
        x = initialX + (float) (Math.sin(iteration + initialX) * 300);
        y = initialY + (float) (Math.cos(iteration + initialY) * 300);
    }
}
