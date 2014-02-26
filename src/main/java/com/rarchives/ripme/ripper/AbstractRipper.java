package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.rarchives.ripme.utils.Utils;

public abstract class AbstractRipper implements RipperInterface {

    protected URL url;
    protected File workingDir = null;

    public abstract void rip() throws IOException;
    public abstract void setWorkingDir() throws IOException;

    /**
     * Ensures inheriting ripper can rip this URL.
     * @param url
     *      URL to rip.
     * @throws IOException
     *      If anything goes wrong.
     */
    public AbstractRipper(URL url) throws IOException {
        if (!canRip(url)) {
            throw new MalformedURLException("Unable to rip url: " + url);
        }
        this.url = url;
        setWorkingDir();
        workingDir = Utils.getWorkingDirectory();
    }

    public URL getURL() {
        return url;
    }

}