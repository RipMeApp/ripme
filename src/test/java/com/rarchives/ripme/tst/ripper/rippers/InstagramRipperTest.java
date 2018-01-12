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
        Map<URL, String> testURLs = new HashMap<>();
        testURLs.put(new URL("http://instagram.com/Test_User"), "Test_User");
        testURLs.put(new URL("http://instagram.com/_test_user_"), "_test_user_");
        testURLs.put(new URL("https://www.instagram.com/p/BZ4egP7njW5/?hl=en"), "BZ4egP7njW5");
        testURLs.put(new URL("https://www.instagram.com/p/BZ4egP7njW5"), "BZ4egP7njW5");
        testURLs.put(new URL("https://www.instagram.com/p/BaNPpaHn2zU/?taken-by=hilaryduff"), "hilaryduff_BaNPpaHn2zU");
        testURLs.put(new URL("https://www.instagram.com/p/BaNPpaHn2zU/"), "BaNPpaHn2zU");
        for (URL url : testURLs.keySet()) {
            InstagramRipper ripper = new InstagramRipper(url);
            ripper.setup();
            assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    public void testInstagramAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        // This unit test is a bit flaky 
        //contentURLs.add(new URL("https://www.instagram.com/Test_User/"));
        contentURLs.add(new URL("https://www.instagram.com/p/BZ4egP7njW5/?hl=en"));
        contentURLs.add(new URL("https://www.instagram.com/p/BaNPpaHn2zU/"));
        for (URL url : contentURLs) {
            InstagramRipper ripper = new InstagramRipper(url);
            testRipper(ripper);
        }
    }
}
