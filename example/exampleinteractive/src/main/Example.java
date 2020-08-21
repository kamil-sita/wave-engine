package main;

import main.discriminators.Stages;
import main.discriminators.Tables;
import main.tables.PhysicalAttributes;
import org.w3c.dom.css.Rect;
import waveengine.WaveEngineParameters;
import waveengine.core.UpdatePolicy;
import waveengine.core.WaveEngine;
import waveengine.core.WaveEngineSystemEvents;
import waveengine.ecs.system.RenderingSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.guiimplementation.graphicalobject.RectangleGraphicalObject;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.library.WaveTables;
import waveengine.library.objects.Clickable;
import waveengine.library.systems.ButtonStateChecker;
import waveengine.library.systems.GraphicalResourceManager;
import waveengine.library.systems.Profiler;

import java.awt.*;

public class Example {
    public static void main(String[] args) {
        var renderingSystem = new RenderingSystem() {
            @Override
            public void update(WaveCanvas canvas, double deltaTime) {
                var tables = getTablesFor(Tables.GRAPHICS, Tables.PHYSICAL_ATTRIBUTES_TABLE);
                var positionTable = tables.getTable(Tables.PHYSICAL_ATTRIBUTES_TABLE, PhysicalAttributes.class);
                var graphicalResourceManager = getSystem(GraphicalResourceManager.class);
                graphicalResourceManager.addResource(Stages.MAIN_STAGE, Resource.TEST_RESOURCE);
                tables.getTable(Tables.GRAPHICS, GraphicalObject.class).iterate(
                        (index, graphObj) -> {
                            //var res = graphicalResourceManager.getResource(Resource.TEST_RESOURCE); todo
                            var res = new RectangleGraphicalObject(Color.GREEN, Color.YELLOW, 50, 50);

                            var parameters = graphObj.getParameters();
                            var position = positionTable.get(index);

                            parameters.setX(position.x);
                            parameters.setY(position.y);

                            canvas.render(res, parameters);
                        });
                Parameters parameters = new Parameters(400, 200);
                canvas.render(new RectangleGraphicalObject(Color.GREEN, Color.BLACK, 150, 150), parameters);
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
        //profiler
        Profiler.addSelf(wave);

        //position system
        wave.addSystem(UpdatePolicy.UPDATE_PARALLEL, new WaveSystem() {
                    @Override
                    public void update(double deltaTime) {
                        var physicsComponent = getTableFor(Tables.PHYSICAL_ATTRIBUTES_TABLE, PhysicalAttributes.class);
                        physicsComponent.iterate(
                                (index, physObj) -> {
                                    physObj.update(deltaTime);
                                });
                }
                }.setName("Position system")
        );

        //shutdown listener is needed, since instead of closing, pressing close button sends message to notifier service
        wave.addListener(WaveEngineSystemEvents.WINDOW_CLOSE_REQUEST, (cause, message, waveEngineRunning) -> waveEngineRunning.shutdown("", false));

        wave.setInitialStage(Stages.MAIN_STAGE);
        wave.launch();

    }
}
