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
    // http://www.konradokonski.com/sawdust/
    // http://www.konradokonski.com/wiory/
    // http://freeadultcomix.com/finders-feepaid-in-full-sparrow/
    // http://comics-xxx.com/republic-rendezvous-palcomix-star-wars-xxx/
    // http://tnbtu.com/comic/01-00/
    // http://shipinbottle.pepsaga.com/?p=281

    public void test_totempole666() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.totempole666.com/comic/first-time-for-everything-00-cover/"));
        testRipper(ripper);
    }

    public void test_buttsmithy() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://buttsmithy.com/archives/comic/p1"));
        testRipper(ripper);
    }

    public void test_themonsterunderthebed() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://themonsterunderthebed.net/?comic=test-post"));
        testRipper(ripper);
    }

    public void test_prismblush() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://prismblush.com/comic/hella-trap-pg-01/"));
        testRipper(ripper);
    }

    /*
    // https://github.com/RipMeApp/ripme/issues/266 - WordpressRipper: konradokonski.com previously supported but now cannot rip

    public void test_konradokonski_1() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.konradokonski.com/sawdust/"));
        testRipper(ripper);
    }

    public void test_konradokonski_2() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://www.konradokonski.com/wiory/"));
        testRipper(ripper);
    }
    */

    public void test_freeadultcomix() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://freeadultcomix.com/finders-feepaid-in-full-sparrow/"));
        testRipper(ripper);
    }

    public void test_comicsxxx() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://comics-xxx.com/republic-rendezvous-palcomix-star-wars-xxx/"));
        testRipper(ripper);
    }

    public void test_tnbtu() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://tnbtu.com/comic/01-00/"));
        testRipper(ripper);
    }

    public void test_pepsaga() throws IOException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URL("http://shipinbottle.pepsaga.com/?p=281"));
        testRipper(ripper);
    }
}
