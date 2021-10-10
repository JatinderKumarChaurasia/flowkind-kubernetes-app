package app.flowkind.microservices.api.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.key.ZonedDateTimeKeySerializer;

import java.time.ZonedDateTime;

public record EventRecord<K,T> (Type eventType, K key, T data, ZonedDateTime eventCreatedAt) {
    enum Type {
        CREATE,DELETE
    }
    public EventRecord() {
        this(null,null,null,null);
    }
    public EventRecord {
        eventCreatedAt = ZonedDateTime.now();
    }

    @JsonSerialize(using = ZonedDateTimeKeySerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}