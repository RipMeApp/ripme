package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public interface RipperInterface {
    public void rip() throws IOException;
    public boolean canRip(URL url);
    public void sanitizeURL() throws MalformedURLException;
}
