package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class GirlsreleasedRipper extends AbstractHTMLRipper {

    public GirlsreleasedRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "girlsreleased.";
    }

    @Override
    public String getDomain() {
        return "girlsreleased.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://girlsreleased.com/#set/([0-9]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected girlsreleased URL format: " +
                "https://girlsreleased.com/#set/ID - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            String f = Jsoup.connect(url.toExternalForm()).ignoreContentType(true).userAgent(USER_AGENT).requestBody("{\"tasks\":[\"getset\"],\"set\":{\"id\":\"" + getGID(url) + "\"},\"w\":\"b53477c2821c1bf0da5d40e57b870d35\"}").post().text();
            JSONObject jsonObject = new JSONObject(f);
            for (Object h : jsonObject.getJSONObject("set").getJSONArray("images")) {
//                Example data: [9582439,1001,0,"https://imagetwist.com/sic98h1p8vya","https://img202.imagetwist.com/th/32480/sic98h1p8vya.jpg","V097_01001.jpg.JPG",null]
                result.add(h.toString().split(",")[4].replaceAll("\"", "").replaceAll("/th/", "/i/"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
