package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.CyberdropRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyberdropRipperTest extends RippersTest {
    @Test
    public void testScrolllerGID() throws IOException {
        Map<URL, String> testURLs = new HashMap<>();

        testURLs.put(new URL("https://cyberdrop.me/a/n4umdBjw"), "n4umdBjw");
        testURLs.put(new URL("https://cyberdrop.me/a/iLtp4BjW"), "iLtp4BjW");
        for (URL url : testURLs.keySet()) {
            CyberdropRipper ripper = new CyberdropRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    public void testCyberdropNumberOfFiles() throws IOException {
        List<URL> testURLs = new ArrayList<URL>();

        testURLs.add(new URL("https://cyberdrop.me/a/n4umdBjw"));
        testURLs.add(new URL("https://cyberdrop.me/a/iLtp4BjW"));
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