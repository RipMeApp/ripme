package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class TwitchVideoRipper extends VideoRipper {

    private static final Logger logger = LogManager.getLogger(TwitchVideoRipper.class);

    private static final String HOST = "twitch";

    public TwitchVideoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https://clips\\.twitch\\.tv/.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https://clips\\.twitch\\.tv/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(m.groupCount());
        }

        throw new MalformedURLException(
                "Expected Twitch.tv format:"
                        + "https://clips.twitch.tv/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        logger.info("Retrieving " + this.url);
        Document doc = Http.url(url).get();

        //Get user friendly filename from page title
        String title = doc.title();

        Elements script = doc.select("script");
        if (script.isEmpty()) {
            throw new IOException("Could not find script code at " + url);
        }
        //Regex assumes highest quality source is listed first
        Pattern p = Pattern.compile("\"source\":\"(.*?)\"");

        for (Element element : script) {
            Matcher m = p.matcher(element.data());
            if (m.find()){
                String vidUrl = m.group(1);
                addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + title);
            }
        }
        waitForRipperThreads();
    }
}
