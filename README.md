# Feedback Ingestion Pipeline

## Current Design
![Design](image/FeedbackIngestionPipelineDesign.png?raw=true "Feedback Ingestion Pipeline Design")

##Assumptions/Decisions Made for this project
1. Same event of a source for different tenant will be treated as a different event
2. There can be updates in an event (eg. Review) so we will update the event in DB and will not ignore it.
3. For this project I am using my own models assuming that *Twitter* and *ReviewsAPI* will not break their SLA with an update
4. Currently, *Scheduled events* can only be created from the console only. This can be extended to use API Gateway and cloudformation to automate this by providing a API.

## Content

1. Why do we need 3 stages?
2. Steps to add a new Source
3. Steps to add a new Tenant for a source
4. Event Generation
    1. Scheduled Events (EventBridge)
    2. Event Search Lambdas
        1. TwitterEventSearch Lambda
        2. TripAdvisorEventSearch Lambda
5. Event Parsing
    1. Lambda Triggering Queue
    2. Lambda to parse raw feedback events
6. Event Storage
    1. Lambda Triggering Queue
    2. DynamoDB Table to store feedback data
    3. Lambda to write event to DynamoDB table
7. Dead letter Queues

## Why do we need 3 stages?

Although we can do every step in a single lambda, It will become a bottleneck when parsing and writing to DB when the query output is huge.

To overcome this we can have source search lambdas to only make API calls and publish events for parsing.

Similarly, Writing to DB will become a bottleneck for parsing. We have only a limited number of write capacity units and if we cross that threshold, DynamoDB will throttle and start throwing errors.

We can overcome this by having separate lambdas from parsing and DynamoDB writes. And we can limit the DynamoDB write lambda’s concurrent executions which will ensure that our DB never gets throttled.

## Steps to add a new Source

### 1.  Add a new pull source:

1. Cloudformation Template changes to create a new lambda in the application stack
2. Event handler and service calls for a new source
3. Event parsing logic for a new source response

### 2.  Add a new Push source:

1. Push source can directly publish events to EventParserQueue in RawFeedbackEvent format.
2. Else a new lambda can be added as a buffer to convert push events to raw feedback events and then that lambda can publish events to EventParserQueue

## Steps to add a new Tenant for a pull source

1. Create a scheduled event with required details and Done. Refer Scheduled event section for more details.

## Event Generation

### Scheduled Events

Works as a trigger to invoke pull functionality lambdas. This will be used for new tenant onboarding to a Source.

Event properties:

1. Schedule rate: Rate at which lambda will be triggered for a tenant query
2. Input: Input to the lambda. This will contain the required data for lambda functioning. Like tenant info, query, time range
3. Lambda Trigger: This will invoke the target lambda

Sample Input for Twitter:

```
{
  "time": "2022-02-20T12:00:00Z",
  "detail": {
    "query": "Tesla -is:retweet", //Query for searching 
    "tenant": "Tesla",
    "slotLength": 30
  }
}
```

This will invoke TwitterEventSearch lambda with for tenant “Tesla” with input query from “2022-02-20 11:30:00” to “2022-02-20 12:00:00” GMT


### Event Search Lambdas

Functions for individual sources. Each source will have its own Lambda. These lambdas will act as pullers for their respective sources.

**Input**: Scheduled event
**Output**: Raw feedback event containing source info, tenant info, and Source API response
**Failure**:  Event Lands to Search Event DQL

#### 1. TwitterEventSearch Lambda

* Queries Twitter’s /2/*tweets/*search/recent API
* Can query for a specific time slot
* If receives a non-empty response, writes to Parser queue. The single response can contain multiple events.

#### 2. TripAdvisorEventSearch Lambda

* Queries reviewsApi /2/*tweets/*search/recent API
* Only has support for searching from a date
* If receives a non-empty response, writes to Parser queue

## Event Parsing

### Lambda Triggering Queue/EventParserQueue

This queue gets messages from the pull source and also provides push mechanisms for push-based events.
Producers(Event Pullers/Event Pushers) can push the raw feedback events to this queue. This queue triggers the FeedbackEventParser Lambda

### Lambda/FeedbackEventParser

1. Parses input raw feedback events and converts them into processed ***Feedback Events***.
2. Raw feedback event contains the data in API response format. This lambda converts it to a uniform structure to store for all sources.
3. Contains parsing logic for each source. Parses the event based on the source field present in the raw event.
4. Finally, publishes every event to DynamoDBWriteQueue

## Event Storage

### Lambda Triggering Queue/DynamoDBWriteQueue

This queue gets messages from the FeedbackEventParser lambda. This queue triggers the SaveFeedback Lambda

### Lambda/SaveFeedback

1. Converts SQS Event to DynamoDB Mapper object (FeedbackEvent in this case as using the same POJO for both lambdas)
2. Writes the event to DynamoDB Table “Feedback-Table

### Storage/Feedback-Table

DynamoDB is used as storage due to its high scalability, fault tolerance, and availability

**Partition key: generator**
It is the combination of source and tenant as SOURCE_TENANT. This will ensure that all the events from a source for a tenant will be stored in the same partition reducing the search overhead.

**Range/Sort Key: id**
This is the unique id for an event at the source.

*Together both of them make a **primary key** that will make an event **idempotent**. Here we are assuming that **each event will correspond to a tenant**. If the **same event comes for another tenan**t, we will treat it as a **new event**.*

## **Dead Letter Queues**

## **Dead Letter Queues**

A dead letter queue is required to store all failed events which can be used to rerun the message,  create alarms and debug through CloudWatch logs.


