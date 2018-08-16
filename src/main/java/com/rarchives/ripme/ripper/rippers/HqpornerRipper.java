package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rarchives.ripme.App.logger;

public class HqpornerRipper extends AbstractSingleFileRipper {


        public HqpornerRipper(URL url) throws IOException {
            super(url);
        }

        private String getVideoFromMyDaddycc(String url) {
            Pattern p = Pattern.compile("(//[a-zA-Z0-9\\.]+/pub/cid/[a-z0-9]+/1080.mp4)");
            try {
                logger.info("Downloading " + url);
                Document page = Http.url(url).referrer(url).get();
                Matcher m = p.matcher(page.html());
                logger.info(page.html());
                if (m.find()) {
                    return m.group(0);
                }


            } catch (IOException e) {
                logger.error("Unable to get page with video");
            }
            return null;
        }

        private String getVideoName() {
            try {
                String filename = getGID(url);
                return filename;
            } catch (MalformedURLException e) {
                return "1080";
            }
        }

        @Override
        public String getHost() {
            return "hqporner";
        }

        @Override
        public String getDomain() {
            return "hqporner.com";
        }

        @Override
        public String getGID(URL url) throws MalformedURLException {
            Pattern p = Pattern.compile("https?://hqporner.com/hdporn/([a-zA-Z0-9_-]*).html/?$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return m.group(1);
            }
            throw new MalformedURLException("Expected hqporner URL format: " +
                    "hqporner.com/hdporn/NAME - got " + url + " instead");
        }

        @Override
        public Document getFirstPage() throws IOException {
            // "url" is an instance field of the superclass
            return Http.url(url).get();
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<>();
            result.add("https:" + getVideoFromMyDaddycc("https:" + doc.select("div.videoWrapper > iframe").attr("src")));
            return result;
        }

        @Override
        public boolean tryResumeDownload() {return true;}

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, "", "", "", null, getVideoName(), "mp4");
        }


}
