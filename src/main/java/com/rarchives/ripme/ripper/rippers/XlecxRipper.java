package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XlecxRipper extends XcartxRipper {

    private Pattern p = Pattern.compile("^https?://xlecx.org/([a-zA-Z0-9_\\-]+).html");

    public XlecxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "xlecx";
    }

    @Override
    public String getDomain() {
        return "xlecx.org";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected URL format: http://xlecx.org/comic, got: " + url);

    }
}
