package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WordpressComicRipper;

public class WordpressComicRipperTest extends RippersTest {
    // Test links (see also WordpressComicRipper.java)
    // http://www.totempole666.com/comic/first-time-for-everything-00-cover/
    // http://buttsmithy.com/archives/comic/p1
    // http://themonsterunderthebed.net/?comic=test-post
    // http://prismblush.com/comic/hella-trap-pg-01/
    // http://www.konradokonski.com/sawdust/comic/get-up/
    // http://www.konradokonski.com/wiory/comic/08182008/
    // http://freeadultcomix.com/finders-feepaid-in-full-sparrow/
    // http://thisis.delvecomic.com/NewWP/comic/in-too-deep/
    // http://tnbtu.com/comic/01-00/
    // http://shipinbottle.pepsaga.com/?p=281

    /*
    // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_totempole666() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.totempole666.com/comic/first-time-for-everything-00-cover/"));
        testRipper(ripper);
    }
    */

    /*
    // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_buttsmithy() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://buttsmithy.com/archives/comic/p1"));
        testRipper(ripper);
    }
    */

    /*
    // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_themonsterunderthebed() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://themonsterunderthebed.net/?comic=test-post"));
        testRipper(ripper);
    }
    */

    public void test_prismblush() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://prismblush.com/comic/hella-trap-pg-01/"));
        testRipper(ripper);
    }

    public void test_konradokonski_1() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.konradokonski.com/sawdust/comic/get-up/"));
        testRipper(ripper);

    }

    public void test_konradokonski_2() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.konradokonski.com/wiory/comic/08182008/"));
        testRipper(ripper);
    }

    public void test_konradokonski_getAlbumTitle() throws IOException {
        URL url = new URL("http://www.konradokonski.com/sawdust/comic/get-up/");
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        assertEquals("konradokonski.com_sawdust", ripper.getAlbumTitle(url));

    }

    /*
    // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_freeadultcomix() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://freeadultcomix.com/finders-feepaid-in-full-sparrow/"));
        testRipper(ripper);
    }
    */

    public void test_delvecomic() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://thisis.delvecomic.com/NewWP/comic/in-too-deep/"));
        testRipper(ripper);
    }

    public void test_Eightmuses_download() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("https://8muses.download/lustomic-playkittens-josh-samuel-porn-comics-8-muses/"));
        testRipper(ripper);
    }

    public void test_Eightmuses_getAlbumTitle() throws IOException {
        URL url = new URL("https://8muses.download/lustomic-playkittens-josh-samuel-porn-comics-8-muses/");
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        assertEquals("8muses.download_lustomic-playkittens-josh-samuel-porn-comics-8-muses",
                ripper.getAlbumTitle(url));
    }

    public void test_spyingwithlana_download() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://spyingwithlana.com/comic/the-big-hookup/"));
        testRipper(ripper);
    }

    public void test_spyingwithlana_getAlbumTitle() throws IOException {
        URL url = new URL("http://spyingwithlana.com/comic/the-big-hookup/");
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        assertEquals("spyingwithlana_the-big-hookup", ripper.getAlbumTitle(url));
    }

    // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
//    public void test_pepsaga() throws IOException {
//        WordpressComicRipper ripper = new WordpressComicRipper(
//                new URL("http://shipinbottle.pepsaga.com/?p=281"));
//        testRipper(ripper);
//    }
}
