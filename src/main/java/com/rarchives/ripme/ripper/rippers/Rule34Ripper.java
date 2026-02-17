package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class Rule34Ripper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(Rule34Ripper.class);

    public Rule34Ripper(URL url) throws IOException {
        super(url);
    }

    private String apiUrl;
    private int pageNumber = 0;
    private String apiKey;
    private String userId;

    private void loadConfig() {
        apiKey = Utils.getConfigString("rule34.api_key", "");
        userId = Utils.getConfigString("rule34.user_id", "");
        if (apiKey.isEmpty() || userId.isEmpty()) {
            sendUpdate(STATUS.DOWNLOAD_WARN,
                "rule34.xxx requires API credentials. Set rule34.api_key and rule34.user_id in config. "
                + "Get them from https://rule34.xxx/index.php?page=account&s=options");
            logger.warn("Missing rule34 API credentials. Requests may fail with 403.");
        } else {
            logger.info("Using rule34 API credentials for user_id: " + userId);
        }
    }

    @Override
    public String getHost() {
        return "rule34";
    }

    @Override
    public String getDomain() {
        return "rule34.xxx";
    }

    @Override
    public boolean canRip(URL url){
        Pattern p = Pattern.compile("https?://rule34.xxx/index.php\\?page=post&s=list&tags=([\\S]+)");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://rule34.xxx/index.php\\?page=post&s=list&tags=([\\S]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected rule34.xxx URL format: " +
                "rule34.xxx/index.php?page=post&s=list&tags=TAG - got " + url + " instead");
    }

    public URL getAPIUrl() throws MalformedURLException, URISyntaxException {
        String baseUrl = "https://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=100&tags=" + getGID(url);
        if (apiKey != null && !apiKey.isEmpty() && userId != null && !userId.isEmpty()) {
            baseUrl += "&api_key=" + apiKey + "&user_id=" + userId;
        }
        return new URI(baseUrl).toURL();
    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        loadConfig();
        apiUrl = getAPIUrl().toExternalForm();
        return Http.url(apiUrl).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.html().contains("Search error: API limited due to abuse")) {
            throw new IOException("No more pages");
        }
        pageNumber += 1;
        String nextPage = apiUrl + "&pid=" + pageNumber;
        return Http.url(nextPage).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("posts > post")) {
            String imageSource = el.select("post").attr("file_url");
            result.add(imageSource);

        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
