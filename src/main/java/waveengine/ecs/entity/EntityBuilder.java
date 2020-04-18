package waveengine.ecs.entity;

import waveengine.Discriminator;
import waveengine.ecs.component.ComponentManager;

import java.util.function.Predicate;

public final class EntityBuilder {
    private final ComponentManager componentManager;

    public EntityBuilder(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public EntityBuilderWithEntity oneStage(Discriminator stage) {
        return new EntityBuilderWithEntity(new EntityActiveOneStage(stage));
    }

    public EntityBuilderWithEntity always() {
        return new EntityBuilderWithEntity(new EntityAcceptsAll());
    }

    public EntityBuilderWithEntity predicate(Predicate<? super Discriminator> predicate) {
        return new EntityBuilderWithEntity(new EntityAcceptsPredicate(predicate));
    }

    public class EntityBuilderWithEntity {
        private Entity entity;

        public EntityBuilderWithEntity(Entity entity) {
            this.entity = entity;
        }

        public <T> EntityBuilderWithEntity addToComponent(Discriminator component, T obj) {
            componentManager.addEntityToComponent(entity, component, obj);
            return this;
        }

        public EntityBuilderWithEntity setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

}
