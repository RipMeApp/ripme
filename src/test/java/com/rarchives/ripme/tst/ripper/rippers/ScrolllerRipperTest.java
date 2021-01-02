package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ScrolllerRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ScrolllerRipperTest extends RippersTest {
    @Test
    public void testScrolllerGID() throws IOException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp"), "CatsStandingUp");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=pictures"), "CatsStandingUp");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?sort=top&filter=pictures"), "CatsStandingUp");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=pictures&sort=top"), "CatsStandingUp");
        for (URL url : testURLs.keySet()) {
            ScrolllerRipper ripper = new ScrolllerRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    public void testScrolllerFilterRegex() throws IOException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp"), "NOFILTER");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=pictures"), "PICTURE");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=videos"), "VIDEO");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=albums"), "ALBUM");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?sort=top&filter=pictures"), "PICTURE");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?sort=top&filter=videos"), "VIDEO");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?sort=top&filter=albums"), "ALBUM");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=pictures&sort=top"), "PICTURE");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=videos&sort=top"), "VIDEO");
        testURLs.put(new URL("https://scrolller.com/r/CatsStandingUp?filter=albums&sort=top"), "ALBUM");
        for (URL url : testURLs.keySet()) {
            ScrolllerRipper ripper = new ScrolllerRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.convertFilterString(ripper.getParameter(ripper.getURL(),"filter")));
            deleteDir(ripper.getWorkingDir());
        }
    }



}
