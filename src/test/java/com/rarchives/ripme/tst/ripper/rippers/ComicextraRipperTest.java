package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.ComicextraRipper;

public class ComicextraRipperTest extends RippersTest {

    public void testComicUrl() throws IOException {
        URL url = new URL("https://www.comicextra.com/comic/karma-police");
        ComicextraRipper ripper = new ComicextraRipper(url);
        testRipper(ripper);
    }

    public void testChapterUrl() throws IOException {
        URL url = new URL("https://www.comicextra.com/v-for-vendetta/chapter-1");
        ComicextraRipper ripper = new ComicextraRipper(url);
        testRipper(ripper);
    }

}
