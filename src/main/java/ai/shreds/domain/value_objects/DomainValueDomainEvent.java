package ai.shreds.domain.value_objects;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all domain events.
 */
public abstract class DomainValueDomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final String eventType;
    private final Instant occurredAt;
    private final String correlationId;

    protected DomainValueDomainEvent(String aggregateId, String eventType) {
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Aggregate ID cannot be null or empty");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId.trim();
        this.eventType = eventType.trim();
        this.occurredAt = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainValueDomainEvent that = (DomainValueDomainEvent) other;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "DomainValueDomainEvent{" +
               "eventId='" + eventId + '\'' +
               ", aggregateId='" + aggregateId + '\'' +
               ", eventType='" + eventType + '\'' +
               ", occurredAt=" + occurredAt +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}