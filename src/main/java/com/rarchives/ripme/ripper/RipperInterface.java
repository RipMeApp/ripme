package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * I have no idea why I made this interface. Everything is captured within the AbstractRipper.
 * Oh well, here's to encapsulation and abstraction! (raises glass)
 * 
 * (cheers!)
 */
interface RipperInterface {
    void rip() throws IOException, URISyntaxException;
    boolean canRip(URL url);
    URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException;
    void setWorkingDir(URL url) throws IOException, URISyntaxException;
    String getHost();
    String getGID(URL url) throws MalformedURLException, URISyntaxException;
}