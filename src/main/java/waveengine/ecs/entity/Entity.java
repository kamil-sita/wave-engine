package waveengine.ecs.entity;


import waveengine.Discriminator;

import java.util.function.Predicate;

public abstract class Entity {
    private int id;

    Entity() {
        id = EntityService.INSTANCE.getNewId();
    }

    public static Entity getEntityActiveOnOneStage(Discriminator stageActive) {
        return new EntityActiveOneStage(stageActive);
    }

    public static Entity getEntityActiveAlways() {
        return new EntityAcceptsAll();
    }

    public static Entity getEntityActivePredicate(Predicate<? super Discriminator> predicate) {
        return new EntityAcceptsPredicate(predicate);
    }


    public int getId() {
        return id;
    }

    public abstract boolean isActive(Discriminator stage);
}
