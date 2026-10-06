package com.library.concurrent;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Bounded application-owned executor for recurring library background work. */
public final class LibraryTaskExecutor implements AutoCloseable {
    private static final Logger LOGGER=Logger.getLogger(LibraryTaskExecutor.class.getName());
    private final ScheduledExecutorService executor=Executors.newScheduledThreadPool(2,r->{Thread thread=new Thread(r,"library-background-task");thread.setDaemon(true);return thread;});
    public void scheduleFineCalculation(FineCalculationTask task,Duration interval){if(task==null||interval==null||interval.isZero()||interval.isNegative())throw new IllegalArgumentException("Task and positive interval are required.");executor.scheduleWithFixedDelay(task,0,interval.toMillis(),TimeUnit.MILLISECONDS);}
    @Override public void close(){executor.shutdown();try{if(!executor.awaitTermination(10,TimeUnit.SECONDS)){executor.shutdownNow();if(!executor.awaitTermination(10,TimeUnit.SECONDS))LOGGER.warning("Library task executor did not terminate cleanly.");}}catch(InterruptedException e){executor.shutdownNow();Thread.currentThread().interrupt();LOGGER.log(Level.WARNING,"Interrupted while stopping library task executor.",e);}}
}
