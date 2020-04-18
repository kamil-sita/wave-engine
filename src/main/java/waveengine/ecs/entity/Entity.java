package waveengine.ecs.entity;


import waveengine.Discriminator;

import java.util.function.Predicate;

public abstract class Entity {
    private int id;

    Entity() {
        id = EntityService.INSTANCE.getNewId();
    }

    public int getId() {
        return id;
    }

    public abstract boolean isActive(Discriminator stage);
}
