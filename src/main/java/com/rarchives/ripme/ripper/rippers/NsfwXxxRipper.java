package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NsfwXxxRipper extends AbstractJSONRipper {

    public NsfwXxxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "nsfw.xxx";
    }

    @Override
    public String getHost() {
        return "nsfw_xxx";
    }


    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String u = url.toExternalForm();
        // https://nsfw.xxx/user/kelly-kat/foo -> https://nsfw.xxx/user/kelly-kat
        // https://nsfw.xxx/user/kelly-kat -> https://nsfw.xxx/user/kelly-kat
        // keep up to and including the username
        u = u.replaceAll("https?://nsfw.xxx/user/([^/]+)/?.*", "https://nsfw.xxx/user/$1");
        if (!u.contains("nsfw.xxx/user")) {
            throw new MalformedURLException("Invalid URL: " + url);
        }

        return new URI(u).toURL();
    }

    String getUser() throws MalformedURLException {
        return getGID(url);
    }

    URL getPage(int page) throws MalformedURLException, URISyntaxException {
        return new URI("https://nsfw.xxx/slide-page/" + page + "?nsfw%5B%5D=0&types%5B%5D=image&types%5B%5D=video&types%5B%5D=gallery&slider=1&jsload=1&user=" + getUser()).toURL();
    }


    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://nsfw.xxx/user/([^/]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected URL format: " +
                "nsfw.xxx/user/USER - got " + url + " instead");
    }


    int currentPage = 1;

    @Override
    protected JSONObject getFirstPage() throws IOException, URISyntaxException {
        return Http.url(getPage(1)).getJSON();
    }

    List<String> descriptions = new ArrayList<>();

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        currentPage++;
        JSONObject nextPage = Http.url(getPage(doc.getInt("page") + 1)).getJSON();
        JSONArray items = nextPage.getJSONArray("items");
        if (items.isEmpty()) {
            throw new IOException("No more pages");
        }
        return nextPage;
    }

    class ApiEntry {
        String srcUrl;
        String author;
        String title;

        public ApiEntry(String srcUrl, String author, String title) {
            this.srcUrl = srcUrl;
            this.author = author;
            this.title = title;
        }
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        JSONArray items = json.getJSONArray("items");
        List<ApiEntry> data = IntStream
                .range(0, items.length())
                .mapToObj(items::getJSONObject)
                .map(o -> {
                    String srcUrl;
                    if(o.has("src")) {
                        srcUrl = o.getString("src");
                    } else {
                        // video source
                        Pattern videoHtmlSrcPattern = Pattern.compile("src=\"([^\"]+)\"");
                        Matcher matches = videoHtmlSrcPattern.matcher(o.getString("html"));
                        matches.find();
                        srcUrl = StringEscapeUtils.unescapeHtml(matches.group(1));
                    }

                    return new ApiEntry(srcUrl, o.getString("author"), o.getString("title"));
                })
                .toList();

        data.forEach(e -> descriptions.add(e.title));
        return data.stream().map(e -> e.srcUrl).collect(Collectors.toList());
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index) + descriptions.get(index - 1) + "_" , "", "", null);
    }
}
