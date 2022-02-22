package com.feedback.parse.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.enums.Source;
import com.feedback.model.FeedbackEvent;
import com.feedback.model.Metadata;
import com.feedback.model.RawFeedbackEvent;
import com.feedback.model.twitter.EventData;
import com.feedback.model.twitter.Place;
import com.feedback.model.twitter.Tweet;
import com.feedback.parse.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TwitterEventsParser implements Parser {

    private final ObjectMapper mapper;

    public TwitterEventsParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<FeedbackEvent> parse(RawFeedbackEvent rawFeedbackEvent) throws JsonProcessingException {
        String tenant = rawFeedbackEvent.getTenant();
        JsonNode events = rawFeedbackEvent.getEventsData();
        EventData eventData = mapper.treeToValue(events, EventData.class);
        Map<String, Place> placeMap = new HashMap<>();

        if(eventData.getIncludes() != null) {
            List<Place> places = eventData.getIncludes().getPlaces();
            places.forEach(place -> {
                placeMap.put(place.getId(), place);
            });
        }
        return eventData.getTweets().stream()
                .map( tweet -> getFeedbackEvent(tweet, tenant, placeMap))
                .collect(Collectors.toList());
    }

    private FeedbackEvent getFeedbackEvent(Tweet tweet, String tenant, Map<String, Place> placeMap) {
        Metadata metadata = null;
        if(null != tweet.getGeo()) {
            String placeId = tweet.getGeo().getPlaceId();
            if(placeId!=null || placeMap.size()!=0) {
                Place place = placeMap.get(placeId);
                metadata = Metadata.builder()
                        .country(place.getCountry())
                        .city(place.getName())
                        .build();
            }
        }

        return FeedbackEvent.builder()
                .source(String.valueOf(Source.TWITTER))
                .tenant(tenant)
                .generator(String.format("%s_%s", Source.TWITTER, tenant))
                .createdAt(tweet.getCreatedAt())
                .id(tweet.getId())
                .metadata(metadata)
                .text(tweet.getText())
                .lang(tweet.getLang())
                .build();
    }
}
