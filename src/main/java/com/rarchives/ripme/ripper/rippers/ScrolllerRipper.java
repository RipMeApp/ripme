package com.rarchives.ripme.ripper.rippers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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


    private JSONObject prepareQuery(String iterator, String gid) throws IOException {

        // Prepares the JSONObject we need to pass to the GraphQL query.

        String queryString = "query SubredditQuery( $url: String! $filter: SubredditPostFilter $iterator: String ) { getSubreddit(url: $url) { children( limit: 50 iterator: $iterator filter: $filter ) { iterator items { __typename url title subredditTitle subredditUrl redditPath isNsfw albumUrl isFavorite mediaSources { url width height isOptimized } } } } }";
        String filterString = convertFilterString(getParameter(this.url,"filter"));

        JSONObject variablesObject = new JSONObject().put("url", String.format("/r/%s", gid));

        if (iterator != null) {
            // Iterator is not present on the first page
            variablesObject.put("iterator", iterator);
        }
        if (!filterString.equals("NOFILTER")) {
            // We could also pass filter="" but not including it if not present is cleaner
            variablesObject.put("filter", filterString);
        }

        JSONObject finalQueryObject = new JSONObject().put("variables", variablesObject).put("query", queryString);

        return getPosts(finalQueryObject);

    }


    public String convertFilterString(String filterParameter) {
        // Converts the ?filter= parameter of the URL to one that can be used in the GraphQL query
        // I could basically remove the last "s" and uppercase instead of this switch statement but this looks easier to read.
        switch (filterParameter) {
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

            if (arg.getName().replace(toReplace,"").equals((parameter))) {
                return arg.getValue();
            }
        }
        return "";
    }

    private JSONObject getPosts(JSONObject data) {
        // The actual GraphQL query call

        // JSoup wants POST data in key=value but I need to write a JSON body so I can't use it...
        try {

            String url = "https://api.scrolller.com/api/v2/graphql";

            URL obj = new URL(url);
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
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }

            in.close();
            conn.disconnect();

            return new JSONObject(html.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject("{}");
}


    @Override
    protected List<String> getURLsFromJSON(JSONObject json) throws JSONException {
        JSONArray itemsList = json.getJSONObject("data").getJSONObject("getSubreddit").getJSONObject("children").getJSONArray("items");
        int bestArea = 0;
        String bestUrl = "";
        List<String> list = new ArrayList<>();


        for (Object item : itemsList) {
            JSONArray sourcesTMP = ((JSONObject) item).getJSONArray("mediaSources");
            for (Object sourceTMP : sourcesTMP)
            {
                int widthTMP = ((JSONObject) sourceTMP).getInt("width");
                int heightTMP = ((JSONObject) sourceTMP).getInt("height");
                int areaTMP = widthTMP * heightTMP;

                if (areaTMP > bestArea) {
                    // Better way to determine best image?
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
        if (getParameter(url,"sort") != null) {
            // I need support for the WebSocket protocol to implement sorting.
            // A GraphQL query to the API with the "sortBy" variable can't come from a POST request or it will return error 500, it has to come from a WebSocket.
            LOGGER.warn("Sorting is not currently implemented and it will be ignored");
        }
        return prepareQuery(null, this.getGID(url));
    }

    @Override
    public JSONObject getNextPage(JSONObject source) throws IOException {
        // Every call the the API contains an "iterator" string that we need to pass to the API to get the next page
        // Checking if iterator is null is not working for some reason, hence why the weird "iterator.toString().equals("null")"
        Object iterator = source.getJSONObject("data").getJSONObject("getSubreddit").getJSONObject("children").get("iterator");
        if (!iterator.toString().equals("null")) {
            return prepareQuery(iterator.toString(), this.getGID(url));
        } else {
            return null;
        }

    }

}