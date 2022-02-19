package com.feedback.dao.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.feedback.dao.EventDao;
import com.feedback.model.FeedbackEvent;

public class DynamoDBEventDao implements EventDao {

    private final DynamoDBMapper mapper;

    public DynamoDBEventDao(DynamoDBMapper dynamoDBMapper) {
        mapper = dynamoDBMapper;
    }

    @Override
    public void saveOrUpdateEvent(FeedbackEvent event) {
        mapper.save(event);
    }
}
