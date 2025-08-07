package ai.shreds.domain.ports;

import ai.shreds.domain.value_objects.DomainValueDomainEvent;
import java.util.function.Consumer;

/**
 * Output port for event bus operations in the domain layer.
 * This port is implemented by the infrastructure layer to provide
 * event publishing and subscription capabilities.
 */
public interface DomainOutputPortEventBus {

    /**
     * Publish a domain event to the event bus.
     * @param event the domain event to publish
     * @throws RuntimeException if event publishing fails
     */
    void publishEvent(DomainValueDomainEvent event);

    /**
     * Subscribe to events of a specific type.
     * @param eventType the class type of events to subscribe to
     * @param handler the event handler function
     * @param <T> the type of event
     * @throws RuntimeException if subscription fails
     */
    <T extends DomainValueDomainEvent> void subscribeToEvent(Class<T> eventType, Consumer<T> handler);
}