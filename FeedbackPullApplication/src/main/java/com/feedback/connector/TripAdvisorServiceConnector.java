package com.feedback.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.feedback.constant.TripAdviserServiceConstants.*;

@Log4j2
public class TripAdvisorServiceConnector {

    private final String apiKey;
    private final ObjectMapper mapper;

    public TripAdvisorServiceConnector(String reviewApiKey, ObjectMapper objectMapper) {
        apiKey = reviewApiKey;
        mapper = objectMapper;
    }

    /**
     * Calls reviewsApi to get reviews from profile url. Reviews API supports multiple review platforms like
     * amazon.com, wish.com, aliexpress, tripAdvisor and many more.
     * More details can be found at "https://app.reviewapi.io/documentation#supportedPlatforms"
     *
     * For this assignment are only using TripAdvisor as other platforms are having some issues with integration
     *
     * @param reviewProfileURL url from which reviews have to be fetched
     * @param fromDate date from which reviews have to be fetched
     * @return reviews from ReviewsAPI for input query
     */
    public JsonNode getReviews(String reviewProfileURL, String fromDate) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder(API_ENDPOINT);

        ArrayList<NameValuePair> queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair(URL, reviewProfileURL));
        queryParameters.add(new BasicNameValuePair(API_KEY, apiKey));
        queryParameters.add(new BasicNameValuePair(DATE_FROM, fromDate));

        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());

        log.info("HttpGetRequest: {}", httpGet.toString());

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String entityString = EntityUtils.toString(entity);

        log.info("Response Status: {}", response.getStatusLine());
        log.info("Response Data: {}", entityString);
        return mapper.readTree(entityString);
    }
}
