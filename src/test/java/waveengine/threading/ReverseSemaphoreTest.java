package waveengine.threading;


import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReverseSemaphoreTest {

    @RepeatedTest(value = 4)
    void testGetting() {
        AssistedReverseSemaphore reverseSemaphore = new AssistedReverseSemaphore();

        ExecutorService executorService = Executors.newFixedThreadPool(200);

        AtomicLong atomicLong = new AtomicLong(0);

        for (int i = 0; i < 200; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < 50; j++) {
                    reverseSemaphore.doAwaitIfNotPossible(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        atomicLong.incrementAndGet();
                    });
                }
            });
        }

        try {
            Thread.sleep(215);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {
            reverseSemaphore.blockAwaitDo(() -> {
                atomicLong.set(0);
                try {
                    Thread.sleep(215);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertEquals(atomicLong.get(), 0);
            });

            try {
                Thread.sleep(215);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}