package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import com.rarchives.ripme.ripper.AbstractRipper;

public class ExamplegalleryRipper extends AbstractRipper {

    public ExamplegalleryRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "examplegallery.com";
    }

    @Override
    public void rip() throws IOException {
        System.out.println("Examplegallery ripper running...");
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().contains("examplegallery.com");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException, URISyntaxException {
        return url.getPath().replace("/", "_");
    }

    @Override
    public boolean addURLToDownload(URL url, Path saveAs) {
        return true;
    }

    @Override
    protected boolean addURLToDownload(URL url, Path saveAs, String referrer, Map<String, String> cookies, Boolean getFileExtFromMIME) {
        return true;
    }

    @Override
    public void downloadCompleted(URL url, Path saveAs) {
    }

    @Override
    public void downloadErrored(URL url, String reason) {
    }

    @Override
    public void downloadExists(URL url, Path file) {
    }

    @Override
    public void setWorkingDir(URL url) throws IOException, URISyntaxException {
    }

    @Override
    public int getCompletionPercentage() {
        return 0;
    }

    @Override
    public String getStatusText() {
        return "Examplegallery ripper running";
    }
}