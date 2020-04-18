package waveengine.ecs.component;

import waveengine.Discriminator;
import waveengine.core.Logger;
import waveengine.core.WaveEngineRunning;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Semaphoring {

    private Semaphore modificationLock = new Semaphore(1);
    private Semaphore ownerMapChange = new Semaphore(1);
    private Map<Discriminator, String> ownerMap = new HashMap<>();
    private Map<Discriminator, Semaphore> tablesSemaphoreMap = new ConcurrentHashMap<>();

    private final WaveEngineRunning waveEngineRunning;

    public Semaphoring(WaveEngineRunning waveEngineRunning) {
        this.waveEngineRunning = waveEngineRunning;
    }

    public TableNotOwnedException exclusiveLockObtain(Discriminator[] selectedTables, String owner) {
        var orderedTables = orderTables(selectedTables);
        boolean acquiredAll = true;
        TableNotOwnedException tableNotOwnedException = null;

        List<Discriminator> acquiredTables = new ArrayList<>();

        for (var table : orderedTables) {
            boolean acquired = false;

            tablesSemaphoreMap.putIfAbsent(table, new Semaphore(1));
            long currentTime = System.nanoTime();
            while (true) {
                if (System.nanoTime() - currentTime > waveEngineRunning.getWaveEngineParameters().acquireResourceSingleRequestTime()) {
                    break;
                }

                boolean acquiredSemaphore = tablesSemaphoreMap.get(table).tryAcquire();
                if (acquiredSemaphore) {
                    acquired = true;
                    break;
                }

                Thread.onSpinWait();
            }

            if (!acquired) {
                acquiredAll = false;
                tableNotOwnedException = new TableNotOwnedException(owner + " could not acquire lock on time on: " + table.toString() + ". Current owner is: " + ownerMap.get(table));
                break;
            } else {
                acquiredTables.add(table);
            }
        }

        if (!acquiredAll) {
            for (var table : acquiredTables) {
                tablesSemaphoreMap.get(table).release();
            }
            return tableNotOwnedException;
        }

        ownerMapChange.acquireUninterruptibly();
        for (var table : orderedTables) {
            ownerMap.put(table, owner);
        }
        ownerMapChange.release();

        return null;
    }

    public void exclusiveLockRelease(Discriminator[] selectedTables, String owner) {
        ownerMapChange.acquireUninterruptibly();
        for (var table : selectedTables) {
            tablesSemaphoreMap.get(table).release();
            ownerMap.put(table, "NO OWNER");
        }
        ownerMapChange.release();
    }

    public void modificationLockObtain() {
        modificationLock.acquireUninterruptibly();
    }

    public void modificationLockRelease() {
        modificationLock.release();
    }



    private List<Discriminator> orderTables(Discriminator[] tables) {
        List<Discriminator> tablesAsList = Arrays.asList(tables);
        tablesAsList.sort(Comparator.comparingLong(Discriminator::hashCode));
        return tablesAsList;
    }





    public static class TableNotOwnedException extends Exception {
        public TableNotOwnedException(String message) {
            super(message);
        }
    }
}
