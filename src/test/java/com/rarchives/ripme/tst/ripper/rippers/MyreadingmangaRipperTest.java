package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.MyreadingmangaRipper;


public class MyreadingmangaRipperTest extends RippersTest {
    public void testRip() throws IOException {
        MyreadingmangaRipper ripper = new MyreadingmangaRipper(new URL("https://myreadingmanga.info/zelo-lee-brave-lover-dj-slave-market-jp/"));
        testRipper(ripper);
    }
}
