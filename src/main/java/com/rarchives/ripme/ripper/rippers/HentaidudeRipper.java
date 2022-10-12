package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HentaidudeRipper extends AbstractSingleFileRipper {

    private Pattern p1 = Pattern.compile("https?://hentaidude\\.com/([a-zA-Z0-9_-]*)/?$"); // to match URLs.
    private Pattern p2 = Pattern.compile("data:\\s?(\\{.*?\\})", Pattern.DOTALL);

    public DownloadThreadPool hentaidudeThreadPool = new DownloadThreadPool("hentaidudeThreadPool");

    public HentaidudeRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hentaidude";
    }

    @Override
    public String getDomain() {
        return "hentaidude.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {

        Matcher m = p1.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected hqporner URL format: " + "hentaidude.com/VIDEO - got " + url + " instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Matcher m1 = p1.matcher(url.toString());
        if (m1.matches()) {
            result.add(url.toString());
        }

        // Can add support for search page.
        return result;
    }

    @Override
    public boolean tryResumeDownload() {
        return true;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // addURLToDownload(url, "", "", "", null, getVideoName(), "mp4");
        hentaidudeThreadPool.addThread(new HentaidudeDownloadThread(url, index));
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return hentaidudeThreadPool;
    }

    private class HentaidudeDownloadThread implements Runnable {

        private URL url;

        public HentaidudeDownloadThread(URL url, int index) {
            this.url = url;
            // this.index = index;
        }

        @Override
        public void run() {
            try {
                Document doc = Http.url(url).get();
                URL videoSourceUrl = new URL(getVideoUrl(doc));
                addURLToDownload(videoSourceUrl, "", "", "", null, getVideoName(), "mp4");
            } catch (Exception e) {
                LOGGER.error("Could not get video url for " + getVideoName(), e);
            }
        }

        private String getVideoName() {
            try {
                return getGID(url);
            } catch (MalformedURLException e) {
                LOGGER.error("Unable to get video title from " + url.toExternalForm());
                e.printStackTrace();
            }
            return "unknown";
        }

        /*
         * TO find data object: $.ajax({ url:
         * 'https://hentaidude.com/wp-admin/admin-ajax.php', type: 'post', data: {
         * action: 'msv-get-sources', id: '48227', nonce: '907f1bd45c' }
         */
        public String getVideoUrl(Document doc) throws IOException {
            String jsonString = null;
            Matcher m = p2.matcher(doc.html());

            while (m.find()) {
                jsonString = m.group(1);
                if (jsonString.contains("msv-get-sources"))
                    break;
            }

            if (jsonString != null) {
                // send POST request to https://hentaidude.com/wp-admin/admin-ajax.php with the
                // data object parameters.
                JSONObject dataObject = new JSONObject(jsonString);
                Map<String, String> dataMap = new HashMap<>();
                for (String key : JSONObject.getNames(dataObject)) {
                    dataMap.put(key, dataObject.getString(key));
                }
                JSONObject jsonResopnse = Http.url("https://hentaidude.com/wp-admin/admin-ajax.php").data(dataMap)
                        .method(Method.POST).getJSON();
                // return source url from below JSON.
                /*
                 * success true sources { video-source-0
                 * https://cdn1.hentaidude.com/index.php?data=
                 * 2f4a576957694872754d6736466f6c585579704b4d584e4a434372546c51346d4f4c697a6c734f6678307a59324c5458624f4675664863323768397a3371452f41384b62375246643243466f744447536b2b6250565a3859306a41506d366942713066336c6659386d78513d
                 * video-source-1 <iframe src="https://openload.co/embed/iaJ_zDCTW0M/"
                 * scrolling="no" frameborder="0" width="100%" height="430"
                 * allowfullscreen="true" webkitallowfullscreen="true"
                 * mozallowfullscreen="true"></iframe> }
                 */

                if (jsonResopnse.getBoolean("success")) {
                    // get the hentaidude video source
                    for (String key : JSONObject.getNames(jsonResopnse.getJSONObject("sources"))) {
                        if (jsonResopnse.getJSONObject("sources").getString(key).contains("hentaidude.com")) {
                            return jsonResopnse.getJSONObject("sources").getString(key);
                        }
                    }
                } else {
                    throw new IOException("Could not get video url from JSON response.");
                }

            }

            throw new IOException("Could not get video download url.");
        }
    }
}