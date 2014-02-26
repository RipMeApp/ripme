package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.URL;

public interface RipperInterface {
    public void rip() throws IOException;
    public void processURL(String url);
    public boolean canRip(URL url);
    public void setWorkingDir() throws IOException;
}
