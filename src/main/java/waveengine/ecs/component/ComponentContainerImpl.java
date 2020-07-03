package waveengine.ecs.component;

import waveengine.Discriminator;

import java.util.*;

public class ComponentContainerImpl implements TableGroup {
    private final Map<Discriminator, Table> map = new HashMap<>();
    private final ComponentManager componentManager;

    public ComponentContainerImpl(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public void add(Discriminator component, Map<Integer, Object> el) {
        map.put(component, new TableImpl(el));
    }


    @Override
    public <T> Table<T> getTable(Discriminator table, Class<T> classOfT) {
        return map.get(table);
    }

    @Override
    public ComponentManager getComponentManager() {
        return componentManager;
    }


    public static class TableImpl implements Table {

        private Map<Integer, Object> objectMap;
        private List<Integer> orderList;

        public TableImpl(Map<Integer, Object> objectMap) {
            this.objectMap = objectMap;
        }

        @Override
        public void iterate(WorkOnTable workOnTable) {
            //todo thread safe
            if (orderList == null) {
                buildOrderList();
            }
            for (var index : orderList) {
                var obj = objectMap.get(index);
                workOnTable.doSthOnObject(index, obj);
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
