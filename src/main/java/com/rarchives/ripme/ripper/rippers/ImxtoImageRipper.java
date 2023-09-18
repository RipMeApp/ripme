package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImxtoImageRipper extends AbstractHTMLRipper {

    public ImxtoImageRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "imx.to";
    }

    @Override
    public String getHost() {
        return "imx";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://imx\\.to/i/(.+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected porncoven.com URL format: " + "imx.to/i/id - got" + url + " instead.");
    }

    @Override
    protected Document getFirstPage() throws IOException {
        // Fetch the initial 'landing' page containing a 'Continue to image' button.
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";
        Document initialPage = Http.url(url).userAgent(userAgent).get();

        // Fetch the 'real' page containing the image
        // Check if the button container exists
        Element btnContainer = initialPage.body().getElementById("continuetoimage");
        Element menuContainer = initialPage.body().select(".menuimxto .submenuimxto").first();

        if(btnContainer != null || menuContainer != null) {
            Map<String, String> formData = new HashMap<>();
            formData.put("imgContinue", "Continue+to+your+image...");
            return Http.url(url).userAgent(userAgent).data(formData).post();
        }

        throw new RuntimeException("The initial page appears to be missing!");
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> imageUrls = new ArrayList<>();
        for (Element img : page.body().select("img")) {
            if (img.hasClass("centred")) {
                imageUrls.add(img.attr("src"));
            }
        }
        return imageUrls;
    }

    @Override
    protected void downloadURL(URL url, int index) { addURLToDownload(url, getPrefix(index)); }
}
