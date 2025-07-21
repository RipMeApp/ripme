package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public interface TokenedUrlGetter {
    /**
     * @return The URL of the file to fetch
     * @throws IOException May be thrown if a tokened URI can't be fetched
     * @throws URISyntaxException May be thrown if a URI can't be constructed
     */
    URL getTokenedUrl() throws IOException, URISyntaxException;
}
