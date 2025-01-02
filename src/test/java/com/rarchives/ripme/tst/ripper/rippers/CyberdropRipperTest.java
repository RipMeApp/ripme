package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.CyberdropRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyberdropRipperTest extends RippersTest {
    @Test
    public void testScrolllerGID() throws IOException, URISyntaxException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URI("https://cyberdrop.me/a/n4umdBjw").toURL(), "n4umdBjw");
        testURLs.put(new URI("https://cyberdrop.me/a/iLtp4BjW").toURL(), "iLtp4BjW");
        for (URL url : testURLs.keySet()) {
            CyberdropRipper ripper = new CyberdropRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    @Tag("flaky")
    public void testCyberdropNumberOfFiles() throws IOException, URISyntaxException {
        List<URL> testURLs = new ArrayList<URL>();

        testURLs.add(new URI("https://cyberdrop.me/a/n4umdBjw").toURL());
        testURLs.add(new URI("https://cyberdrop.me/a/iLtp4BjW").toURL());
        for (URL url : testURLs) {
            Assertions.assertTrue(willDownloadAllFiles(url));
        }
    }

    public boolean willDownloadAllFiles(URL url) throws IOException {
        Document doc = Http.url(url).get();
        long numberOfLinks = doc.getElementsByClass("image").stream().count();
        int numberOfFiles = Integer.parseInt(doc.getElementById("totalFilesAmount").text());
        return numberOfLinks == numberOfFiles;
    }



}