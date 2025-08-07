package ai.shreds.infrastructure.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for event processing and asynchronous event handling.
 * Configures Spring's event multicaster, task executors, and error handling.
 */
@Configuration
@EnableAsync
public class InfrastructureEventConfiguration {

    @Value("${scanner.events.async-processing:true}")
    private Boolean asyncEventProcessing;

    @Value("${scanner.events.queue-capacity:1000}")
    private Integer eventQueueCapacity;

    @Value("${scanner.events.processor-threads:4}")
    private Integer eventProcessorThreads;

    @Value("${scanner.events.core-pool-size:2}")
    private Integer corePoolSize;

    @Value("${scanner.events.max-pool-size:8}")
    private Integer maxPoolSize;

    @Value("${scanner.events.keep-alive-seconds:60}")
    private Integer keepAliveSeconds;

    @Value("${scanner.events.thread-name-prefix:event-processor-}")
    private String threadNamePrefix;

    /**
     * Configures the application event multicaster for handling Spring events.
     */
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        
        if (asyncEventProcessing) {
            eventMulticaster.setTaskExecutor(taskExecutor());
            configureAsyncProcessing(eventMulticaster);
        }
        
        eventMulticaster.setErrorHandler(errorHandler());
        
        return eventMulticaster;
    }

    /**
     * Configures the task executor for asynchronous event processing.
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(eventQueueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // Configure thread factory
        executor.setThreadFactory(createEventThreadFactory());
        
        // Configure rejection policy
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Configures error handling for event processing.
     */
    @Bean
    public ErrorHandler errorHandler() {
        return new EventProcessingErrorHandler();
    }

    /**
     * Configures async exception handler for event processing.
     */
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return new EventAsyncExceptionHandler();
    }

    /**
     * Configures asynchronous processing settings.
     */
    private void configureAsyncProcessing(SimpleApplicationEventMulticaster eventMulticaster) {
        // Additional async configuration can be added here
        System.out.println("Configured asynchronous event processing with " + 
                          eventProcessorThreads + " processor threads");
    }

    /**
     * Creates a custom thread factory for event processing threads.
     */
    private ThreadFactory createEventThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadNamePrefix + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.setUncaughtExceptionHandler((t, e) -> {
                    System.err.println("Uncaught exception in event processing thread " + t.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                });
                return thread;
            }
        };
    }

    /**
     * Gets event configuration summary for monitoring.
     */
    public EventConfigurationSummary getConfigurationSummary() {
        return new EventConfigurationSummary(
            asyncEventProcessing,
            eventQueueCapacity,
            eventProcessorThreads,
            corePoolSize,
            maxPoolSize
        );
    }

    // Getters for configuration values
    public Boolean getAsyncEventProcessing() {
        return asyncEventProcessing;
    }

    public Integer getEventQueueCapacity() {
        return eventQueueCapacity;
    }

    public Integer getEventProcessorThreads() {
        return eventProcessorThreads;
    }

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Custom error handler for event processing errors.
     */
    public static class EventProcessingErrorHandler implements ErrorHandler {
        
        @Override
        public void handleError(Throwable t) {
            System.err.println("Error in event processing: " + t.getMessage());
            t.printStackTrace();
            
            // Could implement additional error handling logic here:
            // - Send to dead letter queue
            // - Retry logic
            // - Alerting/monitoring
        }
    }

    /**
     * Custom async exception handler for event processing.
     */
    public static class EventAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable ex, java.lang.reflect.Method method, Object... params) {
            System.err.println("Async event processing exception in method " + method.getName() + ": " + ex.getMessage());
            ex.printStackTrace();
            
            // Log method parameters for debugging
            if (params != null && params.length > 0) {
                System.err.println("Method parameters: ");
                for (int i = 0; i < params.length; i++) {
                    System.err.println("  [" + i + "]: " + (params[i] != null ? params[i].toString() : "null"));
                }
            }
        }
    }

    /**
     * Event configuration summary for monitoring and diagnostics.
     */
    public static class EventConfigurationSummary {
        private final boolean asyncProcessing;
        private final int queueCapacity;
        private final int processorThreads;
        private final int corePoolSize;
        private final int maxPoolSize;

        public EventConfigurationSummary(boolean asyncProcessing, int queueCapacity, 
                                       int processorThreads, int corePoolSize, int maxPoolSize) {
            this.asyncProcessing = asyncProcessing;
            this.queueCapacity = queueCapacity;
            this.processorThreads = processorThreads;
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
        }

        public boolean isAsyncProcessing() {
            return asyncProcessing;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public int getProcessorThreads() {
            return processorThreads;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        @Override
        public String toString() {
            return String.format("EventConfig{async=%s, queue=%d, threads=%d, core=%d, max=%d}",
                               asyncProcessing, queueCapacity, processorThreads, corePoolSize, maxPoolSize);
        }
    }
}