package waveengine.ecs.entity;

import waveengine.Discriminator;

public final class EntityActiveOneStage extends Entity {

    private Discriminator discriminator;

    EntityActiveOneStage(Discriminator discriminator) {
        super();
        this.discriminator = discriminator;
    }

    @Override
    public boolean isActive(Discriminator stage) {
        return stage == discriminator;
    }

    public Discriminator getDiscriminator() {
        return discriminator;
    }
}
