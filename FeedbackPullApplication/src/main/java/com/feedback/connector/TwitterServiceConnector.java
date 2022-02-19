package com.feedback.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.model.TimeSlot;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

import static com.feedback.constant.TwitterServiceConstants.*;

@Log4j2
public class TwitterServiceConnector {

    private final String apiKey;
    private final String apiSecret;
    private final ObjectMapper mapper;

    public TwitterServiceConnector(String key, String secret, ObjectMapper objectMapper) {
        apiKey = key;
        apiSecret = secret;
        mapper = objectMapper;
    }

    /**
     * Calls Twitter's "2/tweets/search/recent" for getting tweets related to a search query.
     *
     * It also fetches the additional information for Geo if available.
     * Can be extended to fetch more info by modifying and adding more fields for query parameter.
     *
     * Currently, returns only 10 recent tweets per call as max_results parameter is not configured due to free account limits
     *
     * Reference: https://developer.twitter.com/en/docs/twitter-api/tweets/search/api-reference/get-tweets-search-recent
     *
     * @param queryString query for twitter search api.
     * @param timeSlot time slot for search query (GMT)
     * @return 10 recent tweets from search api
     */
    public JsonNode getTweets(String queryString, TimeSlot timeSlot) throws IOException, URISyntaxException {

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(RECENT_SEARCH_API_ENDPOINT);

        ArrayList<NameValuePair> queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair(QUERY_PARAMETER, queryString));
        queryParameters.add(new BasicNameValuePair(TWEET_FIELDS_PARAMETER, TWEET_FIELDS_VALUES));
        queryParameters.add(new BasicNameValuePair(EXPANSIONS, EXPANSION_VALUES));
        queryParameters.add(new BasicNameValuePair(PLACE_FIELDS_PARAMETER, PLACE_FIELDS_VALUES));
        queryParameters.add(new BasicNameValuePair(START_TIME, timeSlot.getStartTime()));
        queryParameters.add(new BasicNameValuePair(END_TIME, timeSlot.getEndTime()));

        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());

        log.info("HttpGetRequest: {}", httpGet.toString());
        httpGet.setHeader(AUTHORIZATION, String.format(BEARER_AUTH_HEADER, getAccessToken()));
        httpGet.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String entityString = EntityUtils.toString(entity);

        log.info("Response Status: {}", response.getStatusLine());
        log.info("Response Data: {}", entityString);
        return mapper.readTree(entityString);
    }

    /**
     * Helper method that generates bearer token by calling the /oauth2/token endpoint
     *
     * @return bearer token generated via /oauth2/token endpoint
     */
    private String getAccessToken() throws IOException, URISyntaxException {
        String accessToken = null;

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(OAUTH2_TOKEN_ENDPOINT);

        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair(GRANT_TYPE, CLIENT_CREDENTIALS));
        uriBuilder.addParameters(postParameters);

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader(AUTHORIZATION, String.format(BASIC_AUTH_HEADER, getBase64EncodedString()));
        httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        if (null != entity) {
            try (InputStream inputStream = entity.getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> jsonMap = mapper.readValue(inputStream, Map.class);
                accessToken = jsonMap.get(ACCESS_TOKEN).toString();
            }
        }

        return accessToken;
    }

    /**
     * Helper method that generates the Base64 encoded string to be used to obtain bearer token
     */
    private String getBase64EncodedString() {
        String s = String.format("%s:%s", apiKey, apiSecret);
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

}
