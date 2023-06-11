package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.ArtAlleyRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ArtAlleyRipperTest extends RippersTest {
    @Test
    @Disabled("website switched off")
    public void testRip() throws IOException, URISyntaxException {
        ArtAlleyRipper ripper = new ArtAlleyRipper(new URI("https://artalley.social/@curator/media").toURL());
        testRipper(ripper);
    }
}
