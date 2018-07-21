package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ArtStationRipper;

public class ArtStationRipperTest extends RippersTest {

    public void testArtStationProjects() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("https://www.artstation.com/artwork/the-dwarf-mortar"));
        contentURLs.add(new URL("https://www.artstation.com/artwork/K36GR"));
        contentURLs.add(new URL("http://artstation.com/artwork/5JJQw"));
        for (URL url : contentURLs) {
            ArtStationRipper ripper = new ArtStationRipper(url);
            testRipper(ripper);
        }
    }

    public void testArtStationUserProfiles() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("https://www.artstation.com/heitoramatsu"));
        contentURLs.add(new URL("https://artstation.com/kuvshinov_ilya"));
        contentURLs.add(new URL("http://artstation.com/givemeapiggy"));
        for (URL url : contentURLs) {
            ArtStationRipper ripper = new ArtStationRipper(url);
            testRipper(ripper);
        }
    }
}
