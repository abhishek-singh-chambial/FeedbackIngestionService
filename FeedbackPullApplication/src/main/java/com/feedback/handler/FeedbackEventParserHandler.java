package com.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.model.FeedbackEvent;
import com.feedback.model.RawFeedbackEvent;
import com.feedback.utils.DependencyFactory;
import com.feedback.utils.SQSUtil;
import com.feedback.parse.Parser;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static com.feedback.utils.DependencyFactory.*;

@Log4j2
public class FeedbackEventParserHandler {

    private final SQSUtil sqsUtil;
    private final ObjectMapper mapper;

    public FeedbackEventParserHandler() {
        sqsUtil = DependencyFactory.getSqsUtilInstance();
        mapper = getObjectMapperInstance();
    }

    /**
     * Handler for Raw feedback events parser lambda
     * Checks for the event source and redirects the event to required parser.
     *
     * After parsing, publishes the event to DynamoDB write queue.
     *
     * A separate lambda is required as parsing can become bottleneck for search events producers
     * due to lambda time execution limits and scale of events for a common query.
     *
     * @param event input raw feedback event from EventParserQueue
     */
    public void handleRequest(SQSEvent event, Context context) throws Exception {
        SQSEvent.SQSMessage record = event.getRecords().get(0);
        log.info("Input event: {}", record.getBody());
        Parser parser = null;

        RawFeedbackEvent rawFeedbackEvent = mapper.readValue(record.getBody(), RawFeedbackEvent.class);

        switch(rawFeedbackEvent.getSource()) {
            case TWITTER:
                parser = getTwitterEventsParserInstance();
                break;
            case TRIPADVISOR:
                parser = getTripAdvisorEventParserInstance();
                break;
        }

        List<FeedbackEvent> feedbacks = parser.parse(rawFeedbackEvent);

        for(FeedbackEvent feedback: feedbacks) {
            sqsUtil.publishEventToSQS(feedback);
        }
        log.info("Successfully parsed and published {} feedbacks to DynamoDBWriteQueue", feedbacks.size());
    }
}
