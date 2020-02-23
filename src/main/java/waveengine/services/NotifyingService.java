package waveengine.services;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineSystemEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotifyingService {
    private Map<Discriminator, List<Notifier>> listeners = new HashMap<>();

    public void addListener(Discriminator discriminator, Notifier notifier) {
        if (!listeners.containsKey(discriminator)) {
            listeners.put(discriminator, new ArrayList<>());
        }
        var listenersForDiscriminator = listeners.get(discriminator);
        listenersForDiscriminator.add(notifier);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void asyncNotifyListeners(Discriminator cause, Object message) {
        executorService.submit(() -> {
            if (cause instanceof WaveEngineSystemEvents) {
                Logger.getLogger().log("System Event: " + ((WaveEngineSystemEvents) cause).name() + ", " + message);
            }

            if (listeners.containsKey(cause)) {
                for (var notifier : listeners.get(cause)) {
                    notifier.notifyListener(cause, message);
                }
            }
        });
    }

    public void removeListener(Discriminator discriminator, Notifier notifier) {
        listeners.get(discriminator).remove(notifier);
    }


    public interface Notifier {
        void notifyListener(Discriminator cause, Object message);
    }

}
