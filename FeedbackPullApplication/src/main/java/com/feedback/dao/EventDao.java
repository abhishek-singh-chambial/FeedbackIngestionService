package com.feedback.dao;

import com.feedback.model.FeedbackEvent;

public interface EventDao {

    void saveOrUpdateEvent(FeedbackEvent event);

}
