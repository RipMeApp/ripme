package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rarchives.ripme.ripper.rippers.InstagramRipper;

public class InstagramRipperTest extends RippersTest {
    
    public void testInstagramGID() throws IOException {
        Map<URL, String> testURLs = new HashMap<URL, String>();
        testURLs.put(new URL("http://instagram.com/Test_User"), "Test_User");
        testURLs.put(new URL("http://instagram.com/_test_user_"), "_test_user_");
        testURLs.put(new URL("http://instagram.com/-test-user-"), "-test-user-");
        testURLs.put(new URL("http://statigr.am/username"), "username");
        for (URL url : testURLs.keySet()) {
            InstagramRipper ripper = new InstagramRipper(url);
            ripper.setup();
            assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    public void testInstagramAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://instagram.com/anacheri"));
        for (URL url : contentURLs) {
            InstagramRipper ripper = new InstagramRipper(url);
            testRipper(ripper);
        }
    }

}
