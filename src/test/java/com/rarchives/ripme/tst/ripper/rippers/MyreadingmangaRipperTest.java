package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.MyreadingmangaRipper;


public class MyreadingmangaRipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        MyreadingmangaRipper ripper = new MyreadingmangaRipper(new URI("https://myreadingmanga.info/zelo-lee-brave-lover-dj-slave-market-jp/").toURL());
        testRipper(ripper);
    }
}
