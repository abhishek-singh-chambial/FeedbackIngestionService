package com.feedback.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.feedback.model.FeedbackEvent;
import com.feedback.model.RawFeedbackEvent;

import java.util.List;

public interface Parser {
    public List<FeedbackEvent> parse(RawFeedbackEvent rawFeedbackEvent) throws JsonProcessingException;
}
