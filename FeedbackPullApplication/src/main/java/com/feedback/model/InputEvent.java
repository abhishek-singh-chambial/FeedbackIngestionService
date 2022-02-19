package com.feedback.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputEvent {

    @NonNull
    private String query;

    @NonNull
    private String tenant;

    private Integer slotLength;
}
