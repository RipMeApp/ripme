package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.InstagramRipper;

public class InstagramRipperTest extends RippersTest {
    
    public void testInstagramAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://instagram.com/feelgoodincc#"));
        for (URL url : contentURLs) {
            try {
                InstagramRipper ripper = new InstagramRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
