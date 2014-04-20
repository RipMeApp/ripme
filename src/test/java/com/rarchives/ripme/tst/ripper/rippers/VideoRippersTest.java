package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.video.BeegRipper;
import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.video.VineRipper;
import com.rarchives.ripme.ripper.rippers.video.XvideosRipper;
import com.rarchives.ripme.ripper.rippers.video.YoupornRipper;

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
    
    public void testVineRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("https://vine.co/v/hiqQrP0eUZx"));
        for (URL url : contentURLs) {
            try {
                VineRipper ripper = new VineRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

    public void testYoupornRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.youporn.com/watch/7669155/mrs-li-amateur-69-orgasm/?from=categ"));
        for (URL url : contentURLs) {
            try {
                YoupornRipper ripper = new YoupornRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

    public void testBeegRipper() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://beeg.com/4554321"));
        for (URL url : contentURLs) {
            try {
                BeegRipper ripper = new BeegRipper(url);
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
