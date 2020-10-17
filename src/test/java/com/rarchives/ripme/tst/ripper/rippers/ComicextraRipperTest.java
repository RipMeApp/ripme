package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.ComicextraRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class ComicextraRipperTest extends RippersTest {
    @Test
    public void testComicUrl() throws IOException {
        URL url = new URL("https://www.comicextra.com/comic/karma-police");
        ComicextraRipper ripper = new ComicextraRipper(url);
        testRipper(ripper);
    }
    @Test
<<<<<<< HEAD
=======
    @Disabled("no images found error, broken ripper?")
>>>>>>> upstream/master
    public void testChapterUrl() throws IOException {
        URL url = new URL("https://www.comicextra.com/v-for-vendetta/chapter-1");
        ComicextraRipper ripper = new ComicextraRipper(url);
        testRipper(ripper);
    }

}
