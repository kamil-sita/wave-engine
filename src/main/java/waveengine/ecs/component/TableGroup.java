package waveengine.ecs.component;


import waveengine.Discriminator;

public interface TableGroup {
    Table getTable(Discriminator component);

    class Pair<T, U> {
        T first;
        U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (!first.equals(pair.first)) return false;
            return second.equals(pair.second);
        }

        @Override
        public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }
    }

    interface WorkOnTable {
        void doSthOnObject(Integer integer, Object object);
    }

    interface Table {
        void iterate(boolean ordered, WorkOnTable workOnTable);
        Object get(Integer index);
    }
}
