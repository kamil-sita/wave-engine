package waveengine.ecs.component;

import waveengine.Discriminator;
import waveengine.core.WaveEngineRunning;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Semaphoring {

    private final Semaphore modificationLock = new Semaphore(1);
    private final Semaphore discriminatorForClassLock = new Semaphore(1);
    private final Semaphore ownerMapChange = new Semaphore(1);
    private final Map<Discriminator, String> ownerMap = new HashMap<>();
    private final Map<Discriminator, Semaphore> tablesSemaphoreMap = new ConcurrentHashMap<>();

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

            tablesSemaphoreMap.putIfAbsent(table, new Semaphore(1, true));
            while (true) {
                try {
                    boolean acquiredSemaphore = tablesSemaphoreMap.get(table).tryAcquire(
                            waveEngineRunning.getWaveEngineParameters().acquireResourceRequestTimeMiliseconds(),
                            TimeUnit.MILLISECONDS
                    );
                    if (acquiredSemaphore) {
                        acquired = true;
                    }
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    public void discriminatorForClassAcquire() {
        discriminatorForClassLock.acquireUninterruptibly();
    }

    public void discriminatorForClassRelease() {
        discriminatorForClassLock.release();
    }


    public static class TableNotOwnedException extends Exception {
        public TableNotOwnedException(String message) {
            super(message);
        }
    }
}
