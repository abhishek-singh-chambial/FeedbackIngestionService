package com.feedback.parse.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.enums.Source;
import com.feedback.model.FeedbackEvent;
import com.feedback.model.Metadata;
import com.feedback.model.RawFeedbackEvent;
import com.feedback.model.tripadvisor.EventData;
import com.feedback.model.tripadvisor.Review;
import com.feedback.parse.Parser;

import java.util.List;
import java.util.stream.Collectors;

public class TripAdvisorEventsParser implements Parser {

    private final ObjectMapper mapper;

    public TripAdvisorEventsParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<FeedbackEvent> parse(RawFeedbackEvent rawFeedbackEvent) throws JsonProcessingException {
        String tenant = rawFeedbackEvent.getTenant();
        JsonNode events = rawFeedbackEvent.getEventsData();
        EventData eventData = mapper.treeToValue(events, EventData.class);
        return eventData.getReviews().stream()
                .map( review -> getFeedbackEvent(review, tenant))
                .collect(Collectors.toList());
    }

    private FeedbackEvent getFeedbackEvent(Review review, String tenant) {
        Metadata metadata = Metadata.builder()
                        .rating(review.getRating())
                        .title(review.getTitle())
                        .build();

        return FeedbackEvent.builder()
                .source(String.valueOf(Source.TRIPADVISOR))
                .tenant(tenant)
                .generator(String.format("%s_%s", Source.TRIPADVISOR, tenant))
                .createdAt(review.getTimestamp())
                .id(review.getId())
                .metadata(metadata)
                .text(review.getText())
                .build();
    }
}
