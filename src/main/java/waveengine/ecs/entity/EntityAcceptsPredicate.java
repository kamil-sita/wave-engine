package waveengine.ecs.entity;

import waveengine.Discriminator;

import java.util.function.Predicate;

public final class EntityAcceptsPredicate extends Entity {


    private Predicate<? super Discriminator> predicate;

    public EntityAcceptsPredicate(Predicate<? super Discriminator> predicate) {
        super();
        this.predicate = predicate;
    }

    @Override
    public boolean isActive(Discriminator stage) {
        return predicate.test(stage);
    }
}
