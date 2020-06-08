package main;

import main.discriminators.Stages;
import main.discriminators.Tables;
import main.tables.PhysicalAttributes;
import waveengine.WaveEngineParameters;
import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.component.Semaphoring;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.guiimplementation.ShadedRectangleGraphicalObject;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.library.WaveTables;
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
                tables.getTable(Tables.GRAPHICS, GraphicalObject.class).iterate(
                        (index, graphObj) -> {
                            var graphicalResourceManager = getSystem(GraphicalResourceManager.class);
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

        var addButtonClickable = new Clickable(true, 400, 200, 150, 150, () -> {
            wave.getEntityBuilder()
                    .oneStage(Stages.MAIN_STAGE)
                    .addToComponent(Tables.PHYSICAL_ATTRIBUTES_TABLE, new PhysicalAttributes())
                    .addToComponent(Tables.GRAPHICS, new GraphicalObject());
        });
        wave.getEntityBuilder()
                .oneStage(Stages.MAIN_STAGE)
                .addToComponent(WaveTables.BUTTONS, addButtonClickable);

        //button state checker system
        ButtonStateChecker.addSelf(wave);

        //position system
        wave.addSystem(UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
                    @Override
                    public void update(double deltaTime) throws Semaphoring.TableNotOwnedException {
                        var physicsComponent = getTableFor(Tables.PHYSICAL_ATTRIBUTES_TABLE, PhysicalAttributes.class);
                        physicsComponent.iterate(
                                (index, physObj) -> {
                                    physObj.update(deltaTime);
                                });
                }
                }.setName("Position system")
        );

        //shutdown listener is needed, since instead of closing, pressing close button sends message to notifier service
        wave.addListener(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, (cause, message) -> System.exit(0));

        wave.setInitialStage(Stages.MAIN_STAGE);
        wave.launch();

    }
}
