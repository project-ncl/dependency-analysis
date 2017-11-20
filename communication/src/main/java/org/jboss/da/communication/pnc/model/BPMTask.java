package org.jboss.da.communication.pnc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = BPMTask.BPMTaskBuilder.class)
public class BPMTask {

    private final Integer taskId;

    private final Long processInstanceId;

    private final String processName;

    private List<BpmNotification> events;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BPMTaskBuilder {
    }

}
