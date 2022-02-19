package com.feedback.connector;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.feedback.utils.DependencyFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SQSConnector {

    private final AmazonSQS sqsClient;

    public SQSConnector() {
        this.sqsClient = DependencyFactory.getSqsClientInstance();
    }

    /**
     * Gets queue url from client and sends the input message to target queue
     * @param queueName target SQS queue name. Used to get queue url
     * @param message sqs event data to send to queue
     */
    public void sendMessage(String queueName, String message) {
        String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
        SendMessageRequest messageRequest = new SendMessageRequest(queueUrl, message);
        log.info("Sending message: {} to queue: {}", message, queueName);
        sqsClient.sendMessage(messageRequest);
    }
}
