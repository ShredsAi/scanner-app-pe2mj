package ai.shreds.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Utility class for JSON serialization and deserialization operations.
 * Provides centralized JSON handling with proper configuration for the application.
 */
@Component
public class SharedUtilJsonSerializer {
    
    private final ObjectMapper objectMapper;
    
    public SharedUtilJsonSerializer() {
        this.objectMapper = new ObjectMapper();
        configureObjectMapper();
    }
    
    /**
     * Serializes an object to JSON string.
     * @param object the object to serialize
     * @return JSON string representation
     * @throws RuntimeException if serialization fails
     */
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * Deserializes JSON string to object of specified class.
     * @param json the JSON string
     * @param clazz the target class
     * @param <T> the type parameter
     * @return deserialized object
     * @throws RuntimeException if deserialization fails
     */
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }
    
    /**
     * Deserializes JSON string to list of objects of specified class.
     * @param json the JSON string
     * @param clazz the target class
     * @param <T> the type parameter
     * @return list of deserialized objects
     * @throws RuntimeException if deserialization fails
     */
    public <T> List<T> deserializeList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to List<" + clazz.getSimpleName() + ">", e);
        }
    }
    
    /**
     * Deserializes JSON string to Map.
     * @param json the JSON string
     * @return Map representation
     * @throws RuntimeException if deserialization fails
     */
    public Map<String, Object> deserializeMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to Map", e);
        }
    }
    
    /**
     * Configures the ObjectMapper with appropriate settings for the application.
     */
    private void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    
    /**
     * Gets the configured ObjectMapper instance.
     * @return the ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}