package com.feedback.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSlot {
    private String startTime;
    private String endTime;
}
