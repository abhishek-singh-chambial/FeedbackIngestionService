package com.feedback.constant;

public class TwitterServiceConstants {

    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String BASIC_AUTH_HEADER = "Basic %s";
    public static final String BEARER_AUTH_HEADER = "Bearer %s";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_CREDENTIALS = "client_credentials";

    public static final String OAUTH2_TOKEN_ENDPOINT = "https://api.twitter.com/oauth2/token";
    public static final String RECENT_SEARCH_API_ENDPOINT = "https://api.twitter.com/2/tweets/search/recent";

    public static final String QUERY_PARAMETER = "query";
    public static final String TWEET_FIELDS_PARAMETER = "tweet.fields";
    public static final String TWEET_FIELDS_VALUES = "created_at,geo,lang";
    public static final String EXPANSIONS = "expansions";
    public static final String EXPANSION_VALUES = "geo.place_id";
    public static final String PLACE_FIELDS_PARAMETER = "place.fields";
    public static final String PLACE_FIELDS_VALUES = "country,country_code,id,name";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";

    public static final String TWITTER_API_KEY = "TWITTER_API_KEY";
    public static final String TWITTER_API_SECRET = "TWITTER_API_SECRET";
}
