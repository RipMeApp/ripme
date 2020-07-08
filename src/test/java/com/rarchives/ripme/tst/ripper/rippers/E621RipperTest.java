package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.E621Ripper;
import org.junit.jupiter.api.Test;

public class E621RipperTest extends RippersTest {
    public void testRip() throws IOException {
        E621Ripper ripper = new E621Ripper(new URL("https://e621.net/posts?tags=beach"));
        testRipper(ripper);
    }
    @Test
    public void testFlashOrWebm() throws IOException {
        E621Ripper ripper = new E621Ripper(new URL("https://e621.net/posts?page=4&tags=gif+rating%3As+3d"));
        testRipper(ripper);
    }
    @Test
    public void testGetNextPage() throws IOException {
        E621Ripper nextPageRipper = new E621Ripper(new URL("https://e621.net/posts?tags=cosmicminerals"));
        try {
            nextPageRipper.getNextPage(nextPageRipper.getFirstPage());
            assert (true);
        } catch (IOException e) {
            throw e;
        }

        E621Ripper noNextPageRipper = new E621Ripper(new URL("https://e621.net/post/index/1/cosmicminerals"));
        try {
            noNextPageRipper.getNextPage(noNextPageRipper.getFirstPage());
        } catch (IOException e) {
            assertEquals(e.getMessage(), "No more pages.");
        }
    }
    @Test
    public void testOldRip() throws IOException {
        E621Ripper ripper = new E621Ripper(new URL("https://e621.net/post/index/1/beach"));
        testRipper(ripper);
    }
    @Test
    public void testOldFlashOrWebm() throws IOException {
        E621Ripper ripper = new E621Ripper(new URL("https://e621.net/post/index/1/gif"));
        testRipper(ripper);
    }
    @Test
    public void testOldGetNextPage() throws IOException {
        E621Ripper nextPageRipper = new E621Ripper(new URL("https://e621.net/post/index/1/cosmicminerals"));
        try {
            nextPageRipper.getNextPage(nextPageRipper.getFirstPage());
            assert (true);
        } catch (IOException e) {
            throw e;
        }

        E621Ripper noNextPageRipper = new E621Ripper(new URL("https://e621.net/post/index/1/cosmicminerals"));
        try {
            noNextPageRipper.getNextPage(noNextPageRipper.getFirstPage());
        } catch (IOException e) {
            assertEquals(e.getMessage(), "No more pages.");
        }
    }
}
