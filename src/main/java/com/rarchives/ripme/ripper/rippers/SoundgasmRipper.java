package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundgasmRipper extends AbstractHTMLRipper {

    private static final String HOST = "soundgasm.net";

    public SoundgasmRipper(URL url) throws IOException {
        super(new URL(url.toExternalForm()));
    }

    @Override
    protected String getDomain() { return "soundgasm.net"; }

    @Override
    public String getHost() { return "soundgasm"; }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^/u/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+).*$");
        Matcher m = p.matcher(url.getFile());
        if (m.find()) {
            return m.group(m.groupCount());
        }
        throw new MalformedURLException(
                "Expected soundgasm.net format: "
                        + "soundgasm.net/u/username/id or "
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        return super.getFirstPage();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> res = new ArrayList<>();

        Elements script = page.select("script");
        Pattern p = Pattern.compile("m4a\\:\\s\"(https?:.*)\\\"");

        for (Element e: script) {
            Matcher m = p.matcher(e.data());
            if (m.find()) { res.add(m.group(1)); }
        }
        return res;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}
