package com.rarchives.ripme.ripper;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractRipper implements RipperInterface {

    protected URL url;

    public AbstractRipper(URL url) throws MalformedURLException {
        // Ensure that the inheriting class can rip this URL.
        if (!canRip(url)) {
            throw new MalformedURLException("Unable to rip url: " + url);
        }
        this.url = url;
        sanitizeURL();
    }

    public URL getURL() {
        return url;
    }

}

