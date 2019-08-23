package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XhamsterRipper;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;


public class XhamsterRipperTest extends RippersTest {
    @Test
    public void testXhamsterAlbum1() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/sexy-preggo-girls-9026608"));
        testRipper(ripper);
    }
    @Test
    public void testXhamster2Album() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster2.com/photos/gallery/sexy-preggo-girls-9026608"));
        testRipper(ripper);
    }
    @Test
    public void testXhamsterAlbum2() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664"));
        testRipper(ripper);
    }
    @Test
    public void testXhamsterAlbumOneDomain() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.one/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664"));
        testRipper(ripper);
    }
    @Test
    public void testXhamsterAlbumDesiDomain() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.desi/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664"));
        testRipper(ripper);
    }
    @Test
    public void testXhamsterVideo() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/videos/brazzers-busty-big-booty-milf-lisa-ann-fucks-her-masseur-1492828"));
        testRipper(ripper);
    }
    @Test
    public void testBrazilianXhamster() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://pt.xhamster.com/photos/gallery/silvana-7105696"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664");
        XhamsterRipper ripper = new XhamsterRipper(url);
        assertEquals("7254664", ripper.getGID(url));
    }
    @Test
    public void testGetNextPage() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://pt.xhamster.com/photos/gallery/mega-compil-6-10728626"));
        Document doc = ripper.getFirstPage();
        try {
            ripper.getNextPage(doc);
        } catch (IOException e) {
            throw new Error("Was unable to get next page of album");
        }
    }
}
