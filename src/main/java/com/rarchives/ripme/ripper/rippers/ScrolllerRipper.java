package com.rarchives.ripme.ripper.rippers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.java_websocket.client.WebSocketClient;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;

public class ScrolllerRipper extends AbstractJSONRipper {

    public ScrolllerRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "scrolller";
    }
    @Override
    public String getDomain() {
        return "scrolller.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // Typical URL is: https://scrolller.com/r/subreddit
        // Parameters like "filter" and "sort" can be passed (ex: https://scrolller.com/r/subreddit?filter=xxx&sort=yyyy)
        Pattern p = Pattern.compile("^https?://scrolller\\.com/r/([a-zA-Z0-9]+).*?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected scrolller.com URL format: " +
                "scrolller.com/r/subreddit OR scroller.com/r/subreddit?filter= - got " + url + "instead");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }


    private JSONObject prepareQuery(String iterator, String gid, String sortByString) throws IOException, URISyntaxException {

        String QUERY_NOSORT = "query SubredditQuery( $url: String! $filter: SubredditPostFilter $iterator: String ) { getSubreddit(url: $url) { children( limit: 50 iterator: $iterator filter: $filter ) { iterator items { __typename url title subredditTitle subredditUrl redditPath isNsfw albumUrl isFavorite mediaSources { url width height isOptimized } } } } }";
        String QUERY_SORT = "subscription SubredditSubscription( $url: String! $sortBy: SubredditSortBy $timespan: SubredditTimespan $iterator: String $limit: Int $filter: SubredditPostFilter ) { fetchSubreddit( url: $url sortBy: $sortBy timespan: $timespan iterator: $iterator limit: $limit filter: $filter ) { __typename ... on Subreddit { __typename url title secondaryTitle description createdAt isNsfw subscribers isComplete itemCount videoCount pictureCount albumCount isFollowing } ... on SubredditPost { __typename url title subredditTitle subredditUrl redditPath isNsfw albumUrl isFavorite mediaSources { url width height isOptimized } } ... on Iterator { iterator } ... on Error { message } } }";

        String filterString = convertFilterString(getParameter(this.url,"filter"));

        JSONObject variablesObject = new JSONObject().put("url", String.format("/r/%s", gid)).put("sortBy", sortByString.toUpperCase());
        JSONObject finalQueryObject = new JSONObject().put("variables", variablesObject).put("query", sortByString.equals("") ? QUERY_NOSORT : QUERY_SORT);

        if (iterator != null) {
            // Iterator is not present on the first page
            variablesObject.put("iterator", iterator);
        }
        if (!filterString.equals("NOFILTER")) {
            variablesObject.put("filter", filterString);
        }

        return sortByString.equals("") ? getPosts(finalQueryObject) : getPostsSorted(finalQueryObject);

    }


    public String convertFilterString(String filterParameter) {
        // Converts the ?filter= parameter of the URL to one that can be used in the GraphQL query
        // I could basically remove the last "s" and call toUpperCase instead of this switch statement but this looks easier to read.
        switch (filterParameter.toLowerCase()) {
            case "pictures":
                return "PICTURE";
            case "videos":
                return "VIDEO";
            case "albums":
                return "ALBUM";
            case "":
                return "NOFILTER";
            default:
                LOGGER.error(String.format("Invalid filter %s using no filter",filterParameter));
                return "";
        }
    }

    public String getParameter(URL url, String parameter) throws MalformedURLException {
        // Gets passed parameters from the URL
        String toReplace = String.format("https://scrolller.com/r/%s?",getGID(url));
        List<NameValuePair> args= URLEncodedUtils.parse(url.toExternalForm(), Charset.defaultCharset());
        for (NameValuePair arg:args) {
            // First parameter contains part of the url so we have to remove it
            // Ex: for the url https://scrolller.com/r/CatsStandingUp?filter=xxxx&sort=yyyy
            // 1) arg.getName() => https://scrolller.com/r/CatsStandingUp?filter
            // 2) arg.getName() => sort

            if (arg.getName().replace(toReplace,"").toLowerCase().equals((parameter))) {
                return arg.getValue();
            }
        }
        return "";
    }

    private JSONObject getPosts(JSONObject data) {
        // The actual GraphQL query call

        try {
            String url = "https://api.scrolller.com/api/v2/graphql";

            URL obj = new URI(url).toURL();
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "scrolller.com");

            conn.setDoOutput(true);

            OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

            w.write(data.toString());
            w.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer jsonString = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                jsonString.append(inputLine);
            }

            in.close();
            conn.disconnect();

            return new JSONObject(jsonString.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject("{}");
    }

    private JSONObject getPostsSorted(JSONObject data) throws MalformedURLException {

        // The actual GraphQL query call (if sort parameter is present)
        try {

            ArrayList<String> postsJsonStrings = new ArrayList<>();

            WebSocketClient wsc = new WebSocketClient(new URI("wss://api.scrolller.com/api/v2/graphql")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // As soon as the WebSocket connects send our query
                    this.send(data.toString());
                }

                @Override
                public void onMessage(String s) {
                    postsJsonStrings.add(s);
                    if (new JSONObject(s).getJSONObject("data").getJSONObject("fetchSubreddit").has("iterator")) {
                        this.close();
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                }

                @Override
                public void onError(Exception e) {
                    LOGGER.error(String.format("WebSocket error, server reported %s", e.getMessage()));
                }
            };
            wsc.connect();

            while (!wsc.isClosed()) {
                // Posts list is not over until the connection closes.
            }

            JSONObject finalObject = new JSONObject();
            JSONArray posts = new JSONArray();

            // Iterator is the last object in the post list, let's duplicate it in his own object for clarity.
            finalObject.put("iterator", new JSONObject(postsJsonStrings.get(postsJsonStrings.size()-1)));

            for (String postString : postsJsonStrings) {
                posts.put(new JSONObject(postString));
            }
            finalObject.put("posts", posts);

            if (finalObject.getJSONArray("posts").length() == 1 && !finalObject.getJSONArray("posts").getJSONObject(0).getJSONObject("data").getJSONObject("fetchSubreddit").has("mediaSources")) {
                // Only iterator, no posts.
                return null;
            }

            return finalObject;


        } catch (URISyntaxException ue) {
            // Nothing to catch, it's an hardcoded URI.
        }

        return null;
    }


    @Override
    protected List<String> getURLsFromJSON(JSONObject json) throws JSONException {

        boolean sortRequested = json.has("posts");

        int bestArea = 0;
        String bestUrl = "";
        List<String> list = new ArrayList<>();

        JSONArray itemsList = sortRequested ? json.getJSONArray("posts") :  json.getJSONObject("data").getJSONObject("getSubreddit").getJSONObject("children").getJSONArray("items");

        for (Object item : itemsList) {

            if (sortRequested && !((JSONObject) item).getJSONObject("data").getJSONObject("fetchSubreddit").has("mediaSources")) {
                continue;
            }

            JSONArray sourcesTMP = sortRequested ? ((JSONObject) item).getJSONObject("data").getJSONObject("fetchSubreddit").getJSONArray("mediaSources") : ((JSONObject) item).getJSONArray("mediaSources");
            for (Object sourceTMP : sourcesTMP)
            {
                int widthTMP = ((JSONObject) sourceTMP).getInt("width");
                int heightTMP = ((JSONObject) sourceTMP).getInt("height");
                int areaTMP = widthTMP * heightTMP;

                if (areaTMP > bestArea) {
                    bestArea = widthTMP;
                    bestUrl = ((JSONObject) sourceTMP).getString("url");
                }
            }
            list.add(bestUrl);
            bestUrl = "";
            bestArea = 0;
        }

        return list;
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        try {
            return prepareQuery(null, this.getGID(url), getParameter(url,"sort"));
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Error obtaining first page: %s", e.getMessage()));
            return null;
        }
    }

    @Override
    public JSONObject getNextPage(JSONObject source) throws IOException {
        // Every call the the API contains an "iterator" string that we need to pass to the API to get the next page
        // Checking if iterator is null is not working for some reason, hence why the weird "iterator.toString().equals("null")"

        Object iterator = null;
        if (source.has("iterator")) {
            // Sort requested, custom JSON.
            iterator = source.getJSONObject("iterator").getJSONObject("data").getJSONObject("fetchSubreddit").get("iterator");
        } else {
            iterator = source.getJSONObject("data").getJSONObject("getSubreddit").getJSONObject("children").get("iterator");
        }

        if (!iterator.toString().equals("null")) {
            // Need to change page.
            try {
                return prepareQuery(iterator.toString(), this.getGID(url), getParameter(url,"sort"));
            } catch (URISyntaxException e) {
                LOGGER.error(String.format("Error changing page: %s", e.getMessage()));
                return null;
            }
        } else {
            return null;
        }
    }
}