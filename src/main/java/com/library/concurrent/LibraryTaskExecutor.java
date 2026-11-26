package com.library.concurrent;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Bounded application-owned executor for recurring library background work. */
public final class LibraryTaskExecutor implements AutoCloseable {
    private static final org.slf4j.Logger LOGGER=org.slf4j.LoggerFactory.getLogger(LibraryTaskExecutor.class);
    private final ScheduledExecutorService executor=Executors.newScheduledThreadPool(2,r->{Thread thread=new Thread(r,"library-background-task");thread.setDaemon(true);return thread;});
    public void scheduleFineCalculation(FineCalculationTask task,Duration interval){if(task==null||interval==null||interval.isZero()||interval.isNegative())throw new IllegalArgumentException("Task and positive interval are required.");executor.scheduleWithFixedDelay(task,0,interval.toMillis(),TimeUnit.MILLISECONDS);}
    public void schedulePoolMetrics(com.zaxxer.hikari.HikariDataSource pool){executor.scheduleWithFixedDelay(()->{var m=pool.getHikariPoolMXBean();if(m!=null)LOGGER.info("db_pool_metrics active={} idle={} total={} waiting={}",m.getActiveConnections(),m.getIdleConnections(),m.getTotalConnections(),m.getThreadsAwaitingConnection());},1,60,TimeUnit.SECONDS);}
    @Override public void close(){executor.shutdown();try{if(!executor.awaitTermination(10,TimeUnit.SECONDS)){executor.shutdownNow();if(!executor.awaitTermination(10,TimeUnit.SECONDS))LOGGER.warn("background_executor_shutdown_timeout");}}catch(InterruptedException e){executor.shutdownNow();Thread.currentThread().interrupt();LOGGER.warn("background_executor_shutdown_interrupted",e);}}
}
