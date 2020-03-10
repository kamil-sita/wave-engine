package waveengine.ecs.component;

import waveengine.Discriminator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ComponentContainerImpl implements TableGroup {
    HashMap<Discriminator, Table> map = new HashMap<>();

    public ComponentContainerImpl() {

    }

    public void add(Discriminator component, HashMap<Integer, Object> el) {
        map.put(component, new TableImpl(el));
    }


    @Override
    public Table getTable(Discriminator table) {
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
