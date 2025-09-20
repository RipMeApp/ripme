package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.XvideosRipper;

public class XvideosRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testXvideosVideo1() throws IOException, URISyntaxException {
        // This format is obsolete
        // XvideosRipper ripper = new XvideosRipper(new
        // URI("https://www.xvideos.com/video23515878/dee_s_pool_toys").toURL());
        // The website now redirects that video to this page
        XvideosRipper ripper = new XvideosRipper(
                new URI("https://www.xvideos.com/video.hppdiepcbfe/dee_s_pool_toys").toURL());
        testRipper(ripper);
    }

    @Test
    public void testXvideosVideo2() throws IOException, URISyntaxException {
        XvideosRipper ripper = new XvideosRipper(
                new URI("https://www.xvideos.com/video.ufkmptkc4ae/big_tit_step_sis_made_me_cum_inside_her").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testXvideosAmateursAlbum() throws IOException, URISyntaxException {
        XvideosRipper ripper = new XvideosRipper(
                new URI("https://www.xvideos.com/amateurs/nikibeee/photos/2476083/lanikki").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testXvideosProfilesAlbum() throws IOException, URISyntaxException {
        XvideosRipper ripper = new XvideosRipper(
                new URI("https://www.xvideos.com/profiles/dmthate/photos/8259625/sexy").toURL());
        testRipper(ripper);
    }
}
