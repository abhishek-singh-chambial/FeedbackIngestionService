package com.feedback.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.connector.SQSConnector;
import com.feedback.constant.TripAdviserServiceConstants;
import com.feedback.constant.TwitterServiceConstants;
import com.feedback.dao.EventDao;
import com.feedback.dao.impl.DynamoDBEventDao;
import com.feedback.utils.parse.Parser;
import com.feedback.utils.parse.imp.TripAdvisorEventsParser;
import com.feedback.utils.parse.imp.TwitterEventsParser;
import com.feedback.connector.TripAdvisorServiceConnector;
import com.feedback.connector.TwitterServiceConnector;
import java.util.Objects;


public class DependencyFactory {
    private static TwitterServiceConnector twitterService;
    private static TripAdvisorServiceConnector tripAdvisorService;
    private static ObjectMapper objectMapper;
    private static AmazonSQS sqsClient;
    private static AmazonDynamoDB dynamoDBClient;
    private static SQSConnector sqsConnector;
    private static SQSUtil sqsUtil;
    private static Parser twitterEventParser;
    private static Parser tripAdvisorEventParser;
    private static EventDao dynamoDBDao;
    private static DynamoDBMapper dynamoDBMapper;

    public static EventDao getDynamoDBDaoInstance() {
        if(Objects.isNull(dynamoDBDao)) {
            dynamoDBDao = new DynamoDBEventDao(getDynamoDBMapperInstance());
        }
        return dynamoDBDao;
    }

    public static DynamoDBMapper getDynamoDBMapperInstance() {
        if(Objects.isNull(dynamoDBMapper)) {
            dynamoDBMapper = new DynamoDBMapper(getDynamoDBClientInstance());
        }
        return dynamoDBMapper;
    }

    public static AmazonDynamoDB getDynamoDBClientInstance() {
        if(Objects.isNull(dynamoDBClient)) {
            dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withClientConfiguration(new ClientConfiguration()
                            .withRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicy()))
                    .withRegion(Regions.AP_SOUTH_1)
                    .build();
        }
        return dynamoDBClient;
    }

    public static TwitterServiceConnector getTwitterServiceInstance() {
        if(Objects.isNull(twitterService)) {
            twitterService = new TwitterServiceConnector(getTwitterApiKey(), getTwitterApiSecret(), getObjectMapperInstance());
        }
        return twitterService;
    }

    public static TripAdvisorServiceConnector getTripAdvisorServiceInstance() {
        if(Objects.isNull(tripAdvisorService)) {
            tripAdvisorService = new TripAdvisorServiceConnector(getReviewApiKey(), getObjectMapperInstance());
        }
        return tripAdvisorService;
    }

    public static ObjectMapper getObjectMapperInstance() {
        if(Objects.isNull(objectMapper)) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    public static SQSConnector getSqsConnectorInstance() {
        if(Objects.isNull(sqsConnector)) {
            sqsConnector = new SQSConnector();
        }
        return sqsConnector;
    }

    public static SQSUtil getSqsUtilInstance() {
        if(Objects.isNull(sqsUtil)) {
            sqsUtil = new SQSUtil();
        }
        return sqsUtil;
    }

    public static Parser getTwitterEventsParserInstance() {
        if(Objects.isNull(twitterEventParser)) {
            twitterEventParser = new TwitterEventsParser(getObjectMapperInstance());
        }
        return twitterEventParser;
    }

    public static Parser getTripAdvisorEventParserInstance() {
        if(Objects.isNull(tripAdvisorEventParser)) {
            tripAdvisorEventParser = new TripAdvisorEventsParser(getObjectMapperInstance());
        }
        return tripAdvisorEventParser;
    }

    public static AmazonSQS getSqsClientInstance() {
        if(Objects.isNull(sqsClient)) {
            sqsClient = AmazonSQSClientBuilder.standard().withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .withClientConfiguration(new ClientConfiguration()
                            .withRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicy()))
                    .withRegion(Regions.AP_SOUTH_1)
                    .build();
        }
        return sqsClient;
    }

    public static String getTwitterApiKey() {
        return System.getenv(TwitterServiceConstants.TWITTER_API_KEY);
    }

    public static String getTwitterApiSecret() {
        return System.getenv(TwitterServiceConstants.TWITTER_API_SECRET);
    }

    public static String getReviewApiKey() {
        return System.getenv(TripAdviserServiceConstants.REVIEW_API_KEY_ENV);
    }
}
