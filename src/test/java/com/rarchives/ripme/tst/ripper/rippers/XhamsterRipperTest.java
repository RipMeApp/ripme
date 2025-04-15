package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XhamsterRipper;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class XhamsterRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testXhamsterAlbum1() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://xhamster.com/photos/gallery/sexy-preggo-girls-9026608").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testXhamster2Album() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://xhamster2.com/photos/gallery/sexy-preggo-girls-9026608").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testXhamsterAlbum2() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testXhamsterAlbumDesiDomain() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://xhamster5.desi/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testXhamsterVideo() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://xhamster.com/videos/brazzers-busty-big-booty-milf-lisa-ann-fucks-her-masseur-1492828").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testBrazilianXhamster() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://pt.xhamster.com/photos/gallery/cartoon-babe-15786301").toURL());
        testRipper(ripper);
    }
    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://xhamster5.desi/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664").toURL();
        XhamsterRipper ripper = new XhamsterRipper(url);
        Assertions.assertEquals("7254664", ripper.getGID(url));
    }
    @Test
    @Tag("flaky")
    public void testGetNextPage() throws IOException, URISyntaxException {
        XhamsterRipper ripper = new XhamsterRipper(new URI("https://pt.xhamster.com/photos/gallery/mega-compil-6-10728626").toURL());
        Document doc = ripper.getFirstPage();
        try {
            ripper.getNextPage(doc);
        } catch (IOException e) {
            throw new Error("Was unable to get next page of album");
        }
    }
}
