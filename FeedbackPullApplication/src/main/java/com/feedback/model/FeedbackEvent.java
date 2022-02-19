package com.feedback.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.feedback.utils.DependencyFactory;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamoDBTable(tableName = "Feedback-Table")
public class FeedbackEvent {

    @NonNull
    @DynamoDBHashKey
    private String generator;

    @NonNull
    @DynamoDBRangeKey
    private String id;

    @NonNull
    @DynamoDBAttribute
    private String source;

    @NonNull
    @DynamoDBAttribute
    private String tenant;

    @NonNull
    @DynamoDBAttribute
    private String text;

    @NonNull
    @DynamoDBAttribute
    private String createdAt;

    @DynamoDBAttribute
    private String lang;

    @DynamoDBTypeConverted(converter = MetadataTypeConverter.class)
    private Metadata metadata;

    // Converts the complex type Metadata to a string and vice-versa.
    static public class MetadataTypeConverter implements DynamoDBTypeConverter<String, Metadata> {

        @Override
        public String convert(Metadata object) {
            String metadata = null;
            try {
                if (object != null)
                        metadata = DependencyFactory.getObjectMapperInstance().writeValueAsString(object);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return metadata;
        }

        @Override
        public Metadata unconvert(String data) {

            Metadata metadata = new Metadata();
            try {
                if (!data.isEmpty()) {
                    metadata = DependencyFactory.getObjectMapperInstance().convertValue(data, Metadata.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return metadata;
        }
    }
}
