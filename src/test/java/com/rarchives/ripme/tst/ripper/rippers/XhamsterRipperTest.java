package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.XhamsterRipper;
import org.jsoup.nodes.Document;

public class XhamsterRipperTest extends RippersTest {

    public void testXhamsterAlbum1() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/sexy-preggo-girls-9026608"));
        testRipper(ripper);
    }

    public void testXhamsterAlbum2() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664"));
        testRipper(ripper);
    }

    public void testBrazilianXhamster() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://pt.xhamster.com/photos/gallery/silvana-7105696"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://xhamster.com/photos/gallery/japanese-dolls-4-asahi-mizuno-7254664");
        XhamsterRipper ripper = new XhamsterRipper(url);
        assertEquals("7254664", ripper.getGID(url));
    }

    public void testGetNextPage() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("https://pt.xhamster.com/photos/gallery/silvana-7105696"));
        Document doc = ripper.getFirstPage();
        try {
            ripper.getNextPage(doc);
        } catch (IOException e) {
            throw new Error("Was unable to get next page of album");
        }
    }
}
