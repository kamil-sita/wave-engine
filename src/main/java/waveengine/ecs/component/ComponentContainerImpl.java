package waveengine.ecs.component;

import waveengine.Discriminator;

import java.util.*;

public class ComponentContainerImpl implements TableGroup {
    HashMap<Discriminator, Table> map = new HashMap<>();

    public ComponentContainerImpl() {

    }

    public void add(Discriminator component, HashMap<Integer, Object> el) {
        map.put(component, new TableImpl(el));
    }


    @Override
    public <T> Table<T> getTable(Discriminator table, Class<T> classOfT) {
        return map.get(table);
    }


    public static class TableImpl implements Table {

        private HashMap<Integer, Object> objectMap;
        private List<Integer> orderList;

        public TableImpl(HashMap<Integer, Object> objectMap) {
            this.objectMap = objectMap;
        }

        @Override
        public void iterate(boolean ordered, WorkOnTable workOnTable) {
            if (ordered) {
                if (orderList == null) {
                    buildOrderList();
                }
                for (var index : orderList) {
                    var obj = objectMap.get(index);
                    workOnTable.doSthOnObject(index, obj);
                }
            } else {
                for (var obj : objectMap.entrySet()) {
                    workOnTable.doSthOnObject(obj.getKey(), obj.getValue());
                }
            }
        }

        @Override
        public void iterateReverse(WorkOnTable workOnTable) {
            if (orderList == null) {
                buildOrderList();
            }
            var reverseIterator = orderList.listIterator(orderList.size());
            while (reverseIterator.hasPrevious()) {
                var index = reverseIterator.previous();
                var obj = objectMap.get(index);
                workOnTable.doSthOnObject(index, obj);
            }
        }

        private void buildOrderList() {
            var indexList = new ArrayList<>(objectMap.keySet());

            indexList.sort(Comparator.comparingInt(x -> x));

            orderList = indexList;
        }

        @Override
        public Object get(Integer index) {
            return objectMap.getOrDefault(index, null);
        }
    }
}
