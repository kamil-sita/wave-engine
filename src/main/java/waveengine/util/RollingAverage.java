package waveengine.util;

public class RollingAverage {
    private final int size;
    private int index = 0;
    private double sum = 0;
    private double values[];

    public RollingAverage(int size) {
        this.size = size;
        values = new double[size];
    }

    public void add(double val) {
        sum = sum - values[index];
        values[index] = val;
        sum = sum + val;

        index++;
        if (index >= size) {
            index = 0;
        }
    }

    public double getAverage() {
        return sum / size;
    }
}
