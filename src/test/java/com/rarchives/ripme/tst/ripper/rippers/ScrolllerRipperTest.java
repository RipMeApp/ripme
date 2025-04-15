package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ScrolllerRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ScrolllerRipperTest extends RippersTest {
    @Test
    public void testScrolllerGID() throws IOException, URISyntaxException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp").toURL(), "CatsStandingUp");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=pictures").toURL(), "CatsStandingUp");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?sort=top&filter=pictures").toURL(), "CatsStandingUp");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=pictures&sort=top").toURL(), "CatsStandingUp");
        for (URL url : testURLs.keySet()) {
            ScrolllerRipper ripper = new ScrolllerRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    public void testScrolllerFilterRegex() throws IOException, URISyntaxException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp").toURL(), "NOFILTER");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=pictures").toURL(), "PICTURE");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=videos").toURL(), "VIDEO");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=albums").toURL(), "ALBUM");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?sort=top&filter=pictures").toURL(), "PICTURE");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?sort=top&filter=videos").toURL(), "VIDEO");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?sort=top&filter=albums").toURL(), "ALBUM");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=pictures&sort=top").toURL(), "PICTURE");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=videos&sort=top").toURL(), "VIDEO");
        testURLs.put(new URI("https://scrolller.com/r/CatsStandingUp?filter=albums&sort=top").toURL(), "ALBUM");
        for (URL url : testURLs.keySet()) {
            ScrolllerRipper ripper = new ScrolllerRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.convertFilterString(ripper.getParameter(ripper.getURL(),"filter")));
            deleteDir(ripper.getWorkingDir());
        }
    }



}
