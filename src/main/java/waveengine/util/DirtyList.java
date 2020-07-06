package waveengine.util;

import java.util.AbstractList;

/**
 * List that throws away all guarantees about correctness in favor of speed.
 * @param <T>
 */
public class DirtyList<T> extends AbstractList<T> {

    private int size = 0;
    private int allocatedSize;

    private Object[] array;


    public DirtyList() {
        this(16);
    }

    public DirtyList(int allocatedSize) {
        this.allocatedSize = allocatedSize;
        array = new Object[allocatedSize];
    }

    @Override
    public T get(int index) {
        return (T) array[index];
    }

    @Override
    public int size() {
        return 0;
    }

    public boolean add(T element) {
        assureCapacity(size + 1);
        array[size] = element;
        size++;
        return true;
    }

    public void clear() {
        size = 0;
    }

    private void assureCapacity(int size) {
        if (allocatedSize >= size) {
            return;
        }

        allocatedSize = (int) (allocatedSize * 1.5);
        Object[] newArray = new Object[allocatedSize];

        System.arraycopy(array, 0, newArray, 0, size);

        array = newArray;
    }
}
