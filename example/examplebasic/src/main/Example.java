package main;

import waveengine.Discriminator;
import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.WaveEngineParameters;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.entity.Entity;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.library.systems.GraphicalResourceManager;
import waveengine.library.WaveLibSystemDiscriminator;

public class Example {
    public static void main(String[] args) {
        var renderingSystem = new RenderingSystem() {
            @Override
            public void update(WaveCanvas canvas, double deltaTime) {
                var tables = getTablesFor(DiscComponent.GRAPHICS, DiscComponent.POSITION, DiscComponent.SCALE);
                var positionTable = tables.getTable(DiscComponent.POSITION, ComponentPosition.class);
                var scaleTable = tables.getTable(DiscComponent.SCALE, ComponentScale.class);
                tables.getTable(DiscComponent.GRAPHICS, ComponentGraphics.class).iterate(true,
                        (index, graphObj) -> {

                            var graphicalResourceManager = (GraphicalResourceManager) getSystem(WaveLibSystemDiscriminator.GRAPHICAL_RESOURCE_MANAGER);
                            var res = graphicalResourceManager.getResourceOrLoad(Resource.TEST_RESOURCE);

                            var parameters = graphObj.getParameters();
                            var position = positionTable.get(index);
                            var optionalScale = scaleTable.get(index);

                            parameters.setX((float) position.getX());
                            parameters.setY((float) position.getY());

                            //scale might not be present, since objects in second phase of the loop do not have it
                            if (optionalScale != null) {
                                parameters.setScale((float) optionalScale.getScale());
                            }

                            canvas.render(res.getResource(), parameters);
                        });
                freeComponents(); //optional - will cause tables to be opened to other systems faster, if there is still some work done on things other than tables
            }
        };
        var wave = WaveEngine.newInstance(new WaveEngineParameters(), renderingSystem);
        GraphicalResourceManager.addSelf(wave);

        var cf = wave.getComponentManager();

        //objects for first stage of the loop
        for (int i = 0; i < 50000; i++) {
            var entity = Entity.getEntityActiveOnOneStage(DiscStages.MAIN_LOOP0);
            cf.addEntityToComponent(entity, DiscComponent.POSITION, new ComponentPosition());
            cf.addEntityToComponent(entity, DiscComponent.GRAPHICS, new ComponentGraphics());
            cf.addEntityToComponent(entity, DiscComponent.SCALE, new ComponentScale());
        }


        //objects for first stage of the loop
        for (int i = 0; i < 10; i++) {
            var entity = Entity.getEntityActiveOnOneStage(DiscStages.MAIN_LOOP1);
            cf.addEntityToComponent(entity, DiscComponent.POSITION, new ComponentPosition());
            cf.addEntityToComponent(entity, DiscComponent.GRAPHICS, new ComponentGraphics());
        }

        //position system
        wave.addSystem(DiscSystems.PHYSIC_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
            @Override
            public void update(double deltaTime) {
                var physicsComponent = getTableFor(DiscComponent.POSITION, ComponentPosition.class);
                physicsComponent.iterate(true,
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
                var scaleTable = getTableFor(DiscComponent.SCALE, ComponentScale.class);
                scaleTable.iterate(true, (integer, scaleObj) -> {
                    scaleObj.iterate(deltaTime);
                });
            }
        });

        //shutdown listener is needed, since instead of closing, pressing close button sends message to notifier service
        wave.addListener(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, (cause, message) -> System.exit(0));

        wave.addSystem(DiscSystems.STAGE_CHANGING_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
            double time = 0;
            int state = 0;
            @Override
            protected void update(double deltaTime) {
                time += deltaTime;
                if (state == 0 && time > 10) {
                    time = 0;
                    getWaveEngineRunning().setCurrentStage(DiscStages.MAIN_LOOP1);
                    state = 1;
                }
                if (state == 1 && time > 2) {
                    time = 0;
                    getWaveEngineRunning().setCurrentStage(DiscStages.MAIN_LOOP0);
                    state = 0;
                }
            }
        });

        wave.setInitialStage(DiscStages.MAIN_LOOP0);
        wave.launch();

    }
}
