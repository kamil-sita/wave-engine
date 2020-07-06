package main;

import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.WaveEngineParameters;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.entity.Entity;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.library.systems.GraphicalResourceManager;

import java.util.ArrayList;
import java.util.List;

public class Example {
    public static void main(String[] args) {

        var renderingSystem = new RenderingSystem() {
            @Override
            public void update(WaveCanvas canvas, double deltaTime) {
                var tables = getTablesFor(ComponentPosition.class, ComponentScale.class, ComponentGraphics.class);
                var positionTable = tables.getTable(ComponentPosition.class);
                var scaleTable = tables.getTable(ComponentScale.class);
                var graphicalResourceManager = getSystem(GraphicalResourceManager.class);

                graphicalResourceManager.addResource(DiscStages.MAIN_LOOP0, Resource.TEST_RESOURCE);
                graphicalResourceManager.addResource(DiscStages.MAIN_LOOP1, Resource.TEST_RESOURCE);

                tables.getTable(ComponentGraphics.class).iterate(
                        (index, graphObj) -> {
                            var res = graphicalResourceManager.getResource(Resource.TEST_RESOURCE);

                            var parameters = graphObj.getParameters();
                            var position = positionTable.get(index);
                            var optionalScale = scaleTable.get(index);

                            parameters.setX((float) position.getX());
                            parameters.setY((float) position.getY());

                            //scale might not be present, since objects in second phase of the loop do not have it
                            if (optionalScale != null) {
                                parameters.setScale((float) optionalScale.getScale());
                            }

                            canvas.render(res, parameters);
                        });
                freeComponents(); //optional - will cause tables to be opened to other systems faster, if there is still some work done on things other than tables
            }
        };
        var wave = WaveEngine.newInstance(new WaveEngineParameters(), renderingSystem);
        GraphicalResourceManager.addSelf(wave);

        //objects for first stage of the loop
        for (int i = 0; i < 50000; i++) {
            wave.getEntityBuilder()
                    .oneStage(DiscStages.MAIN_LOOP0)
                    .addToComponent(new ComponentPosition(), ComponentPosition.class)
                    .addToComponent(new ComponentGraphics(), ComponentGraphics.class)
                    .addToComponent(new ComponentScale(), ComponentScale.class);
        }


        //objects for second stage of the loop
        for (int i = 0; i < 10; i++) {
            wave.getEntityBuilder()
                    .oneStage(DiscStages.MAIN_LOOP1)
                    .addToComponent(new ComponentPosition(), ComponentPosition.class)
                    .addToComponent(new ComponentGraphics(), ComponentGraphics.class);
        }

        //position system
        wave.addSystem(DiscSystems.PHYSIC_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
            @Override
            public void update(double deltaTime) {
                var physicsComponent = getTableFor(ComponentPosition.class);
                physicsComponent.iterate(
                        (index, physObj) -> {
                            physObj.update(deltaTime);
                        });
                freeComponents();
            }
        }
        );

        //scale system - objects size change in time
        wave.addSystem(DiscSystems.SCALE_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
            @Override
            protected void update(double deltaTime) {
                var scaleTable = getTableFor(ComponentScale.class);
                scaleTable.iterate((integer, scaleObj) -> {
                    scaleObj.iterate(deltaTime);
                });
            }
        });

        //shutdown listener is needed, since instead of closing, pressing close button sends message to notifier service
        wave.addListener(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, (cause, message) -> System.exit(0));

        List<Entity> addedEntities = new ArrayList<>();


        wave.addSystem(DiscSystems.STAGE_CHANGING_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
            double time = 0;
            int state = 0;
            @Override
            protected void update(double deltaTime) {
                time += deltaTime;
                if (state == 0 && time > 4) {
                    time = 0;
                    getWaveEngineRunning().setNextStage(DiscStages.MAIN_LOOP1);
                    state = 1;
                }
                if (state == 1 && time > 4) {
                    time = 0;
                    getWaveEngineRunning().setNextStage(DiscStages.MAIN_LOOP0);
                    state = 0;
                }
                if (state == 1) {
                    double random = Math.random();
                    if (random > 0.95) {
                        Entity entity = wave.getEntityBuilder()
                                .oneStage(DiscStages.MAIN_LOOP1)
                                .addToComponent(new ComponentPosition(), ComponentPosition.class)
                                .addToComponent(new ComponentGraphics(), ComponentGraphics.class)
                                .getEntity();
                        addedEntities.add(entity);
                    } else if (random < 0.05) {
                        if (addedEntities.size() != 0) {
                            Entity entity = addedEntities.get(0);
                            getWaveEngineRunning().getComponentManager().removeEntity(entity);
                            addedEntities.remove(0);
                        }
                    }
                }
            }
        });

        wave.setInitialStage(DiscStages.MAIN_LOOP0);
        wave.launch();

    }
}
