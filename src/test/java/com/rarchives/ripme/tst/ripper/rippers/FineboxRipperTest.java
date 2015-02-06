package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.rippers.FineboxRipper;

public class FineboxRipperTest extends RippersTest {
    
    public void testVineboxAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        Logger.getRootLogger().setLevel(Level.ALL);
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://vinebox.co/u/wi57hMjc2Ka"));
        contentURLs.add(new URL("http://finebox.co/u/wi57hMjc2Ka"));
        for (URL url : contentURLs) {
            FineboxRipper ripper = new FineboxRipper(url);
            testRipper(ripper);
        }
    }

}
