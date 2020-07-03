package waveengine;

public interface Discriminator {
    int hashCode();
    boolean equals(Object o);

    static Discriminator fromString(String str) {
        return new Discriminator() {
            @Override
            public int hashCode() {
                return str.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return str.equals(obj);
            }

            @Override
            public String toString() {
                return str;
            }
        };
    }
}
