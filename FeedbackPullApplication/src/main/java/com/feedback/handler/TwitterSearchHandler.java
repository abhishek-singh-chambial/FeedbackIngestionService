package com.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.constant.Constants;
import com.feedback.enums.Source;
import com.feedback.model.InputEvent;
import com.feedback.model.TimeSlot;
import com.feedback.connector.TwitterServiceConnector;
import com.feedback.utils.DependencyFactory;
import com.feedback.utils.SQSUtil;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j2
public class TwitterSearchHandler {
    private final TwitterServiceConnector twitterService;
    private final SQSUtil sqsUtil;
    private final ObjectMapper mapper;

    public TwitterSearchHandler() {
        twitterService = DependencyFactory.getTwitterServiceInstance();
        sqsUtil = DependencyFactory.getSqsUtilInstance();
        mapper = DependencyFactory.getObjectMapperInstance();
    }

    /**
     * Handler for Twitter search lambda.
     * This lambda gets triggered by EventBridge scheduled event
     * @param event input Scheduled event
     * Sample Event:
     * {
     *   "time": "2022-02-13T00:00:00Z",
     *   "detail": {
     *     "tenant": "Tenant_Name",
     *     "query": "query",
     *     "slotLength": "time in minutes for which query has to be made"
     *   }
     * }
     */
    public void handleRequest(ScheduledEvent event, Context context) throws Exception {
        InputEvent inputEvent = mapper.convertValue(event.getDetail(), InputEvent.class);
        log.info("Input event:{}", inputEvent.toString());

        TimeSlot timeSlot = getTimeSlot(event.getTime(), inputEvent.getSlotLength());

        JsonNode tweets = twitterService.getTweets(inputEvent.getQuery(), timeSlot);

        if(!tweets.has(Constants.DATA)) {
            log.info("Empty response: {} for request {} with time slot {}",
                     tweets.toPrettyString(), inputEvent.getQuery(), timeSlot.toString());
            return;
        }
        sqsUtil.publishRawFeedbackEventsToSQS(Source.TWITTER, inputEvent.getTenant(), tweets);
        log.info("Successfully published Raw Feedback event to EventParserQueue");
    }

    /**
     *
     * @param eventTime time from the input event metadata
     * @param slotLength time range for which search request has to be made
     * @return TimeSlot containing start and end time for the search request
     */
    private TimeSlot getTimeSlot(DateTime eventTime, Integer slotLength) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startTime = sdf.format(new Date(eventTime.getMillis() - TimeUnit.MINUTES.toMillis(slotLength)));
        String endTime = sdf.format(new Date(eventTime.getMillis()));
        return TimeSlot.builder().startTime(startTime).endTime(endTime).build();
    }
}
