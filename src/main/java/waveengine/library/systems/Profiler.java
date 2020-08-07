package waveengine.library.systems;

import waveengine.core.Logger;
import waveengine.core.WaveEngine;
import waveengine.ecs.system.ProfilerSystem;
import waveengine.ecs.system.WaveSystem;
import waveengine.ecs.system.WaveSystemBase;
import waveengine.guiimplementation.WaveCanvas;
import waveengine.guiimplementation.graphicalobject.TextGraphicalObject;
import waveengine.guiimplementation.renderingparameters.Parameters;
import waveengine.util.RollingAverage;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Profiler extends ProfilerSystem {

    private long lastUpdate = System.nanoTime();
    private final Map<WaveSystemBase, RollingAverage> rollingAverageMap = new ConcurrentHashMap<>();
    private final Set<WaveSystemBase> waveSystemSet = Collections.synchronizedSet(new HashSet<>());

    public Profiler() {
        setName("Game Profiler");
    }

    @Override
    public String getCreator() {
        return "WAVE";
    }

    public static void addSelf(WaveEngine waveEngine) {
        Profiler profiler = new Profiler();
        waveEngine.addProfiler(profiler);
    }

    @Override
    public void reportUps(double v, WaveSystemBase system) {
        waveSystemSet.add(system);

        if (!rollingAverageMap.containsKey(system)) {
            rollingAverageMap.put(system, new RollingAverage(30));
        }
        rollingAverageMap.get(system).add(v);

    }

    public void render(WaveCanvas waveCanvas) {
        boolean log = false;
        if (System.nanoTime() - lastUpdate < 1_000_000_000) {
            log = true;
        }


        synchronized (waveSystemSet) {
            int y = 10;
            int textSize = 15;
            for (WaveSystemBase waveSystem : waveSystemSet) {
                TextGraphicalObject tgo = new TextGraphicalObject("UPS " + waveSystem.getName() + ":" + String.format("%.2f", rollingAverageMap.get(waveSystem).getAverage()), textSize, Color.GREEN);
                waveCanvas.renderDebug(tgo, new Parameters(10, y));
                y += textSize + 5;
                Logger.getLogger().log("UPS for " + waveSystem.getName() + " is " + rollingAverageMap.get(waveSystem).getAverage());
            }
        }
    }
}
