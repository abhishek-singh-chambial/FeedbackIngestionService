package com.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.dao.EventDao;
import com.feedback.model.FeedbackEvent;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import static com.feedback.utils.DependencyFactory.*;

@Log4j2
public class SaveFeedbackHandler {

    private final EventDao eventDao;
    private final ObjectMapper mapper;

    public SaveFeedbackHandler() {
        this.eventDao = getDynamoDBDaoInstance();
        mapper = getObjectMapperInstance();
    }

    /**
     * Handler for Save feedback event.
     * Parses and publishes feedback event to Feedback-Table
     *
     * A separate lambda is required to limit DynamoDB write capacity units.
     *
     * @param event Batch of max 10 SQS events containing metadata and feedback events in body
     */
    public void handleRequest(SQSEvent event, Context context) throws Exception {
        List<SQSEvent.SQSMessage> records = event.getRecords();
        for(SQSEvent.SQSMessage record:records) {
            log.info("Input Event: {}", record.getBody());
            FeedbackEvent feedbackEvent = mapper.readValue(record.getBody(), FeedbackEvent.class);
            saveOrUpdateEvent(feedbackEvent);
            log.info("Successfully saved feedback event with id: {} for tenant: {} to Feedback-Table",
                    feedbackEvent.getId(), feedbackEvent.getTenant());
        }
    }

    public void saveOrUpdateEvent(FeedbackEvent event) {
        if (null == event) {
            log.error("Invalid feedback event. event cannot be null");
            throw new IllegalArgumentException("Cannot save null object");
        }
        eventDao.saveOrUpdateEvent(event);
    }
}
