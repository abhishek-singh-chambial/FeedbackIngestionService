package com.feedback.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.feedback.enums.Source;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RawFeedbackEvent {

    @NonNull
    private Source source;

    @NonNull
    private String tenant;

    @NonNull
    private JsonNode eventsData;
}
