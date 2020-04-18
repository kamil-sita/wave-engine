package main;

import main.discriminators.Stages;
import main.discriminators.Systems;
import main.discriminators.Tables;
import main.tables.PhysicalAttributes;
import waveengine.WaveEngineParameters;
import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.component.Semaphoring;
import waveengine.ecs.entity.Entity;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.Parameters;
import waveengine.guiimplementation.ShadedRectangleGraphicalObject;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.library.WaveTables;
import waveengine.library.WaveSystems;
import waveengine.library.objects.Clickable;
import waveengine.library.systems.ButtonStateChecker;
import waveengine.library.systems.GraphicalResourceManager;

import java.awt.*;

public class Example {
    public static void main(String[] args) {
        var renderingSystem = new RenderingSystem() {
            @Override
            public void update(WaveCanvas canvas, double deltaTime) throws Semaphoring.TableNotOwnedException {
                var tables = getTablesFor(Tables.GRAPHICS, Tables.PHYSICAL_ATTRIBUTES_TABLE);
                var positionTable = tables.getTable(Tables.PHYSICAL_ATTRIBUTES_TABLE, PhysicalAttributes.class);
                tables.getTable(Tables.GRAPHICS, GraphicalObject.class).iterate(true,
                        (index, graphObj) -> {

                            var graphicalResourceManager = (GraphicalResourceManager) getSystem(WaveSystems.GRAPHICAL_RESOURCE_MANAGER);
                            var res = graphicalResourceManager.getResourceOrLoad(Resource.TEST_RESOURCE);

                            var parameters = graphObj.getParameters();
                            var position = positionTable.get(index);

                            parameters.setX(position.x);
                            parameters.setY(position.y);

                            canvas.render(res.getResource(), parameters);
                        });
                Parameters parameters = new Parameters(400, 200);
                canvas.render(new ShadedRectangleGraphicalObject(Color.GREEN, Color.BLACK, 150, 150), parameters);
            }
        };
        var wave = WaveEngine.newInstance(new WaveEngineParameters(), renderingSystem);
        GraphicalResourceManager.addSelf(wave);

        var cf = wave.getComponentManager();

        var addButtonClickable = new Clickable(true, 400, 200, 150, 150, () -> {
            System.out.println("FIRE!");
            var newEntity = Entity.getEntityActiveOnOneStage(Stages.MAIN_STAGE);
            wave.getComponentManager().addEntityToComponent(newEntity, Tables.PHYSICAL_ATTRIBUTES_TABLE, new PhysicalAttributes());
            wave.getComponentManager().addEntityToComponent(newEntity, Tables.GRAPHICS, new GraphicalObject());
        });
        cf.addEntityToComponent(Entity.getEntityActiveOnOneStage(Stages.MAIN_STAGE), WaveTables.BUTTONS, addButtonClickable);

        //button state checker system
        ButtonStateChecker.addSelf(wave);

        //position system
        wave.addSystem(Systems.PHYSIC_SYSTEM, UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
                    @Override
                    public void update(double deltaTime) throws Semaphoring.TableNotOwnedException {
                        var physicsComponent = getTableFor(Tables.PHYSICAL_ATTRIBUTES_TABLE, PhysicalAttributes.class);
                        physicsComponent.iterate(true,
                                (index, physObj) -> {
                                    physObj.update(deltaTime);
                                });
                    }
                }
        );

        //shutdown listener is needed, since instead of closing, pressing close button sends message to notifier service
        wave.addListener(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, (cause, message) -> System.exit(0));

        wave.setInitialStage(Stages.MAIN_STAGE);
        wave.launch();

    }
}
