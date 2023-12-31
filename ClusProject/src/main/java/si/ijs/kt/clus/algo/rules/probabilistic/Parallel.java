package si.ijs.kt.clus.algo.rules.probabilistic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel {
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private static ExecutorService forPool;

    public static <T> void For(final Iterable<T> elements, final Operation<T> operation) {
        try {
        	forPool = Executors.newFixedThreadPool(NUM_CORES * 2, new NamedThreadFactory("Parallel.For"));

        	// invokeAll blocks for us until all submitted tasks in the call complete
            forPool.invokeAll(createCallables(elements, operation));
            
            
            forPool.shutdown();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation) {
        List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
        for (final T elem : elements) {
            callables.add(new Callable<Void>() {
                @Override
                public Void call() {
                    operation.perform(elem);
                    return null;
                }
            });
        }

        return callables;
    }

    public static interface Operation<T> {
        public void perform(T pParameter);
    }
}
