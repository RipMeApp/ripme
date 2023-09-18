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

public class ImxtoRipper extends AbstractHTMLRipper {

    public ImxtoRipper(URL url) throws IOException {
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
        // Try and match both single image and gallery links
        Pattern p = Pattern.compile("^https?://imx\\.to/[i,g]/(.+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected porncoven.com URL format: " + "imx.to/i/### or imx.to/g/### - got" + url + " instead.");
    }

    @Override
    protected Document getFirstPage() throws IOException {
        // Fetch the initial 'landing' page containing a 'Continue to image' button.
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0";
        Document page = Http.url(url).userAgent(userAgent).get();

        // Fetch the 'real' page containing the image
        // Check if the button container exists
        Element btnContainer = page.body().getElementById("continuetoimage");
        Element menuContainer = page.body().select(".menuimxto .submenuimxto").first();

        if(btnContainer != null || menuContainer != null) {
            Map<String, String> formData = new HashMap<>();
            formData.put("imgContinue", "Continue+to+your+image...");
            return Http.url(url).userAgent(userAgent).data(formData).post();
        } else {
            // This is gallery.
            return page;
        }
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> imageUrls = new ArrayList<>();
        // Page with a single image
        Element singleImage = page.body().select("img.centred").first();
        if (singleImage != null) {
            imageUrls.add(singleImage.attr("src"));
        } else {
            // Page with a gallery
            for (Element element : page.body().select("div#content div.tooltip a img")) {
                String aLink = element.attr("src").replace("/t/", "/i/");
                imageUrls.add(aLink);
            }
        }
        return imageUrls;
    }

    @Override
    protected void downloadURL(URL url, int index) { addURLToDownload(url, getPrefix(index)); }
}
