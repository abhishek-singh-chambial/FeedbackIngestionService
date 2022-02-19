package com.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.enums.Source;
import com.feedback.model.InputEvent;
import com.feedback.connector.TripAdvisorServiceConnector;
import com.feedback.utils.SQSUtil;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.feedback.constant.Constants.*;
import com.feedback.utils.DependencyFactory;

@Log4j2
public class TripAdvisorSearchHandler {

    private final TripAdvisorServiceConnector reviewApiService;
    private final SQSUtil sqsUtil;
    private final ObjectMapper mapper;

    public TripAdvisorSearchHandler() {
        reviewApiService = DependencyFactory.getTripAdvisorServiceInstance();
        sqsUtil = DependencyFactory.getSqsUtilInstance();
        mapper = DependencyFactory.getObjectMapperInstance();
    }

    /**
     * Handler for TripAdvisor Review Search Lambda
     *
     * This gets triggered by event bridge scheduled event and calls reviewApi to get
     * reviews for input query url from last day
     *
     * @param event input event trigger from EventBridge
     *   Sample Event:
     * {
     *   "time": "2022-02-13T00:00:00Z",
     *   "detail": {
     *     "tenant": "Tenant_Name",
     *     "query": "QueryUrl"
     *   }
     * }
     */
    public void handleRequest(ScheduledEvent event, Context context) throws Exception {
        InputEvent inputEvent = mapper.convertValue(event.getDetail(), InputEvent.class);
        String date = getDateForEvent(event.getTime());
        JsonNode reviews = reviewApiService.getReviews(inputEvent.getQuery(), date);
        if(reviews.get(REVIEWS).isEmpty()) {
            log.info("Empty response for reviews url {} for date {}", inputEvent.getQuery(), date);
            return;
        }
        sqsUtil.publishRawFeedbackEventsToSQS(Source.TRIPADVISOR, inputEvent.getTenant(), reviews);
        log.info("Successfully published raw feedback events to EventParserQueue");
    }

    /**
     * Returns date of previous day
     * @param eventTime time from the input event metadata
     * @return TimeSlot containing start and end time for the search request
     */
    private String getDateForEvent(DateTime eventTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date(eventTime.getMillis() -  86400000L)); //Get reviews for previous day
    }
}
