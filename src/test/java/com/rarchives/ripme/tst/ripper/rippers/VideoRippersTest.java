package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.video.YuvutuRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VideoRippersTest extends RippersTest {

    /**
     * Helper method for testing a video ripper
     * 
     * @param ripper The video ripper
     */
    private void videoTestHelper(VideoRipper ripper) {
        URL oldURL = ripper.getURL();
        try {
            ripper.setup();
            ripper.markAsTest();
            ripper.rip();
            // Video ripper testing is... weird.
            // If the ripper finds the URL to download the video, and it's a test,
            // then the ripper sets the download URL as the ripper's URL.
            Assertions.assertFalse(oldURL.equals(ripper.getURL()), "Failed to find download url for " + oldURL);
        } catch (Exception e) {
            Assertions.fail("Error while ripping " + ripper.getURL() + " : " + e);
            e.printStackTrace();
        } finally {
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    @Disabled("Test disbaled. See https://github.com/RipMeApp/ripme/issues/574")
    public void testTwitchVideoRipper() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("https://clips.twitch.tv/FaithfulIncredulousPotTBCheesePull").toURL());
        for (URL url : contentURLs) {
            // TwitchVideoRipper ripper = new TwitchVideoRipper(url);
            // videoTestHelper(ripper);
        }
    }

    @Test
    @Disabled("Test disabled see https://github.com/RipMeApp/ripme/issues/1095")
    public void testPornhubRipper() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("https://www.pornhub.com/view_video.php?viewkey=ph5a329fa707269").toURL());
        for (URL url : contentURLs) {
            PornhubRipper ripper = new PornhubRipper(url);
            videoTestHelper(ripper);
        }
    }

    public void testYuvutuRipper() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("http://www.yuvutu.com/video/828499/female-reader-armpit-job/").toURL());
        for (URL url : contentURLs) {
            YuvutuRipper ripper = new YuvutuRipper(url);
            videoTestHelper(ripper);
        }
    }

}
