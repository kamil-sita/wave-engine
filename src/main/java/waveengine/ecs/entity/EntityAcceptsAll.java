package waveengine.ecs.entity;

import waveengine.Discriminator;

public final class EntityAcceptsAll extends Entity {

    EntityAcceptsAll() {
        super();
    }

    @Override
    public boolean isActive(Discriminator stage) {
        return true;
    }
}
