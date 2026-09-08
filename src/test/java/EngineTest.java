import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EngineTest {
    public static void main(String[] args) throws Exception {
        callsAreCountedForOneInstance();
        callsAreCountedAcrossInstances();
        callsAreCountedAcrossThreadsAndInstances();
    }

    static void callsAreCountedForOneInstance() throws Exception {
        Engine engine = new Engine();

        engine.doSomething();
        engine.doSomething();
        engine.doSomething();

        var callCount = //calls to doSomething

        assertEquals(3, callCount);
    }

    static void callsAreCountedAcrossInstances() throws Exception {
        Engine first = new Engine();
        Engine second = new Engine();

        first.doSomething();
        first.doSomething();
        second.doSomething();

        var callCount = //calls to doSomething

        assertEquals(3, callCount);
    }

    static void callsAreCountedAcrossThreadsAndInstances() throws Exception {
        int threadCount = 8;
        int callsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        Engine[] engines = new Engine[threadCount];

        for (int i = 0; i < threadCount; i++) {
            engines[i] = new Engine();
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int call = 0; call < callsPerThread; call++) {
                        engines[index].doSomething();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Timed out waiting for worker threads");
        }

        var callCount = //calls to doSomething

        assertEquals(threadCount * callsPerThread, callCount);
    }
}
