package waveengine.guiimplementation.graphicalobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComposedGraphicalObject implements GraphicalObject {
    private List<GraphicalObject> graphicalObjectList = new ArrayList<>();

    public ComposedGraphicalObject(GraphicalObject... graphicalObjects) {
        add(graphicalObjects);
    }

    public ComposedGraphicalObject add(GraphicalObject... graphicalObjects) {
        Collections.addAll(graphicalObjectList, graphicalObjects);
        return this;
    }

    //todo hide
    public List<GraphicalObject> getGraphicalObjectList() {
        return graphicalObjectList;
    }
}
