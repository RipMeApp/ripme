package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.GfycatRipper;
import com.rarchives.ripme.ripper.rippers.RedgifsRipper;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;

public class RedgifsRipperTest extends RippersTest {

    /**
     * Rips correctly formatted URL directly from Redgifs
     * @throws IOException
     */
    @Test
    public void testRedgifsGoodURL() throws IOException{
        RedgifsRipper ripper = new RedgifsRipper(new URL("https://redgifs.com/watch/blaringbonyfulmar-panty-peel"));
        testRipper(ripper);
    }


    /**
     * Rips a Redifs profile
     * @throws IOException
     */
    @Test
    public void testRedgifsProfile() throws IOException {
        RedgifsRipper ripper  = new RedgifsRipper(new URL("https://redgifs.com/users/margo_monty"));
        testRipper(ripper);
    }

    /**
     * Rips a Redifs category/search
     * @throws IOException
     */
    @Test
    public void testRedgifsSearch() throws IOException {
        RedgifsRipper ripper  = new RedgifsRipper(new URL("https://redgifs.com/gifs/browse/little-caprice"));
        Document doc = ripper.getFirstPage();

        doc = ripper.getNextPage(doc);
        assertTrue("https://api.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=150".equalsIgnoreCase(doc.location()));
        doc = ripper.getNextPage(doc);
        assertTrue("https://api.redgifs.com/v1/gfycats/search?search_text=little%20caprice&count=150&start=300".equalsIgnoreCase(doc.location()));
    }
}
