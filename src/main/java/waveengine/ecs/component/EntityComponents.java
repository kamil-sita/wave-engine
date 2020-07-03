package waveengine.ecs.component;

import waveengine.Discriminator;

import java.util.HashMap;
import java.util.Map;

public class EntityComponents {

    //todo explore optimizations - map probably will be overkill for small sizes
    private Map<Discriminator, Object> map = new HashMap<>();

    public EntityComponents() {

    }

    public void addComponent(Discriminator discriminator, Object object) {
        map.put(discriminator, object);
    }

    public Object getComponent(Discriminator discriminator) {
        return map.get(discriminator);
    }

    public boolean hasComponent(Discriminator discriminator) {
        return map.containsKey(discriminator);
    }
}
