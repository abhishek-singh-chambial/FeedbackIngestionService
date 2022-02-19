package com.feedback.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.connector.SQSConnector;
import com.feedback.enums.Source;
import com.feedback.model.FeedbackEvent;
import com.feedback.model.InputEvent;
import com.feedback.model.RawFeedbackEvent;

import static com.feedback.constant.Constants.DYNAMO_DB_WRITE_QUEUE;
import static com.feedback.constant.Constants.EVENT_PARSER_QUEUE;
import static com.feedback.utils.DependencyFactory.*;

public class SQSUtil {

    private final SQSConnector sqsConnector;
    private final ObjectMapper mapper;

    public SQSUtil() {
        sqsConnector = getSqsConnectorInstance();
        mapper = getObjectMapperInstance();
    }

    public void publishRawFeedbackEventsToSQS(Source source, String tenant, JsonNode eventsData)
            throws JsonProcessingException {
        RawFeedbackEvent sqsEvent = RawFeedbackEvent.builder()
                .source(source)
                .tenant(tenant)
                .eventsData(eventsData)
                .build();
        String message = mapper.writeValueAsString(sqsEvent);
        sqsConnector.sendMessage(EVENT_PARSER_QUEUE, message);
    }

    /**
     *
     * @param event Feedback event to publish to DynamoDb Write Queue
     * @throws JsonProcessingException
     */
    public void publishEventToSQS(FeedbackEvent event) throws JsonProcessingException {
        String message = mapper.writeValueAsString(event);
        sqsConnector.sendMessage(DYNAMO_DB_WRITE_QUEUE, message);
    }
}
