package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.video.XvideosRipper;

public class VideoRippersTest extends RippersTest {
    
    public void testXvideosRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.xvideos.com/video1428195/stephanie_first_time_anal"));
        contentURLs.add(new URL("http://www.xvideos.com/video7136868/vid-20140205-wa0011"));
        for (URL url : contentURLs) {
            try {
                XvideosRipper ripper = new XvideosRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }
    
    public void testPornhubRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.pornhub.com/view_video.php?viewkey=993166542"));
        for (URL url : contentURLs) {
            try {
                PornhubRipper ripper = new PornhubRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
