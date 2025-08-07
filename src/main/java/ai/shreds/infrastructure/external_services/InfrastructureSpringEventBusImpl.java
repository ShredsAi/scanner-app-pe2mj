package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortEventBus;
import ai.shreds.domain.value_objects.DomainValueDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Spring-based implementation of the domain event bus port.
 * Bridges domain events with Spring's application event system.
 */
@Component
public class InfrastructureSpringEventBusImpl implements DomainOutputPortEventBus {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureSpringEventBusImpl.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Map<Class<?>, List<Consumer<?>>> eventListeners;

    public InfrastructureSpringEventBusImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventListeners = new ConcurrentHashMap<>();
        
        logger.info("Spring event bus implementation initialized");
    }

    @Override
    public void publishEvent(DomainValueDomainEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null domain event");
            return;
        }
        
        try {
            // Wrap the domain event in a Spring ApplicationEvent
            SpringDomainEventWrapper wrappedEvent = wrapDomainEvent(event);
            
            // Publish through Spring's event system
            applicationEventPublisher.publishEvent(wrappedEvent);
            
            // Also notify direct subscribers
            notifyDirectSubscribers(event);
            
            logger.debug("Published domain event: {} with ID: {}", 
                        event.getEventType(), event.getEventId());
            
        } catch (Exception e) {
            handleEventError(event, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainValueDomainEvent> void subscribeToEvent(Class<T> eventType, Consumer<T> handler) {
        if (eventType == null || handler == null) {
            logger.warn("Cannot subscribe with null event type or handler");
            return;
        }
        
        eventListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                     .add((Consumer<Object>) handler);
        
        logger.debug("Subscribed handler for event type: {}", eventType.getSimpleName());
    }

    /**
     * Wraps a domain event in a Spring ApplicationEvent.
     */
    private SpringDomainEventWrapper wrapDomainEvent(DomainValueDomainEvent event) {
        return new SpringDomainEventWrapper(this, event);
    }

    /**
     * Notifies direct subscribers (non-Spring listeners).
     */
    @SuppressWarnings("unchecked")
    private void notifyDirectSubscribers(DomainValueDomainEvent event) {
        Class<?> eventClass = event.getClass();
        List<Consumer<?>> handlers = eventListeners.get(eventClass);
        
        if (handlers != null && !handlers.isEmpty()) {
            for (Consumer<?> handler : handlers) {
                try {
                    ((Consumer<DomainValueDomainEvent>) handler).accept(event);
                } catch (Exception e) {
                    logger.error("Error in direct event handler for {}: {}", 
                                eventClass.getSimpleName(), e.getMessage(), e);
                }
            }
        }
        
        // Also check for handlers registered for parent classes
        notifyParentClassHandlers(event, eventClass.getSuperclass());
    }

    /**
     * Notifies handlers registered for parent event classes.
     */
    @SuppressWarnings("unchecked")
    private void notifyParentClassHandlers(DomainValueDomainEvent event, Class<?> parentClass) {
        if (parentClass == null || parentClass == Object.class) {
            return;
        }
        
        List<Consumer<?>> handlers = eventListeners.get(parentClass);
        if (handlers != null && !handlers.isEmpty()) {
            for (Consumer<?> handler : handlers) {
                try {
                    ((Consumer<DomainValueDomainEvent>) handler).accept(event);
                } catch (Exception e) {
                    logger.error("Error in parent class event handler for {}: {}", 
                                parentClass.getSimpleName(), e.getMessage(), e);
                }
            }
        }
        
        // Recursively check parent classes
        notifyParentClassHandlers(event, parentClass.getSuperclass());
    }

    /**
     * Handles errors that occur during event publishing.
     */
    private void handleEventError(DomainValueDomainEvent event, Exception error) {
        logger.error("Error publishing domain event {} with ID {}: {}", 
                    event.getEventType(), event.getEventId(), error.getMessage(), error);
        
        // Could implement retry logic, dead letter queue, etc. here
        // For now, just log the error
    }

    /**
     * Gets the number of registered event listeners.
     */
    public int getListenerCount() {
        return eventListeners.values().stream()
                           .mapToInt(List::size)
                           .sum();
    }

    /**
     * Gets the number of event types with registered listeners.
     */
    public int getEventTypeCount() {
        return eventListeners.size();
    }

    /**
     * Clears all registered event listeners.
     */
    public void clearAllListeners() {
        eventListeners.clear();
        logger.info("Cleared all event listeners");
    }

    /**
     * Spring ApplicationEvent wrapper for domain events.
     */
    public static class SpringDomainEventWrapper extends ApplicationEvent {
        
        private final DomainValueDomainEvent domainEvent;
        
        public SpringDomainEventWrapper(Object source, DomainValueDomainEvent domainEvent) {
            super(source);
            this.domainEvent = domainEvent;
        }
        
        public DomainValueDomainEvent getDomainEvent() {
            return domainEvent;
        }
        
        @Override
        public String toString() {
            return String.format("SpringDomainEventWrapper[eventType=%s, eventId=%s, timestamp=%s]",
                               domainEvent.getEventType(),
                               domainEvent.getEventId(),
                               domainEvent.getOccurredAt());
        }
    }
}