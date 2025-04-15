package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ArtStationRipper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ArtStationRipperTest extends RippersTest {

    @Test
    @Tag("flaky")
    public void testArtStationProjects() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("https://www.artstation.com/artwork/the-dwarf-mortar").toURL());
        contentURLs.add(new URI("https://www.artstation.com/artwork/K36GR").toURL());
        for (URL url : contentURLs) {
            ArtStationRipper ripper = new ArtStationRipper(url);
            testRipper(ripper);
        }
    }

    @Test
    @Tag("flaky")
    public void testArtStationUserProfiles() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("https://www.artstation.com/heitoramatsu").toURL());
        contentURLs.add(new URI("https://artstation.com/kuvshinov_ilya").toURL());
        contentURLs.add(new URI("http://artstation.com/givemeapiggy").toURL());
        for (URL url : contentURLs) {
            ArtStationRipper ripper = new ArtStationRipper(url);
            testRipper(ripper);
        }
    }
}
