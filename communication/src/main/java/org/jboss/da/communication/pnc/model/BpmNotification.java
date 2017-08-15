package org.jboss.da.communication.pnc.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@Getter
public class BpmNotification {

    private final String eventType;

    @JsonIgnore
    private final Map<String, Object> data = new HashMap<>();

    @JsonCreator
    public BpmNotification(@JsonProperty("eventType") String eventType) {
        this.eventType = eventType;
    }

    @JsonAnySetter
    public void other(String key, Object value) {
        data.put(key, value);
    }
}
