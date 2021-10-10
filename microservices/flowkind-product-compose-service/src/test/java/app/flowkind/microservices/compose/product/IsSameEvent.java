package app.flowkind.microservices.compose.product;

import app.flowkind.microservices.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IsSameEvent extends TypeSafeMatcher<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsSameEvent.class);
    private ObjectMapper objectMapper= new ObjectMapper();
    private Event<Integer,Object> expectedEvent;

    private IsSameEvent(Event<Integer,Object> expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String item) {
        if (expectedEvent == null) {
            return false;
        }
        LOGGER.trace("Convert the following json string to a map: {}", item);
        Map mapEvent = convertJsonStringToMap(item);
        mapEvent.remove("eventCreatedAt");
        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);
        LOGGER.trace("Got the map: {}", mapEvent);
        LOGGER.trace("Compare to the expected map: {}", mapExpectedEvent);
        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    private Map getMapWithoutCreatedAt(Event event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map convertObjectToMap(Object object) {
        JsonNode node = objectMapper.convertValue(object, JsonNode.class);
        return objectMapper.convertValue(node, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    private Map convertJsonStringToMap(String jsonEventString) {
        try {
           return objectMapper.readValue(jsonEventString, new TypeReference<HashMap>() {});
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
