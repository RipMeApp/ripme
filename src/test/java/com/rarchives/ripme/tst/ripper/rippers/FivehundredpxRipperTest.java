package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FivehundredpxRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FivehundredpxRipperTest extends RippersTest {
    @Test @Disabled("Ripper is broken. See https://github.com/RipMeApp/ripme/issues/438")
    public void test500pxAlbum() throws IOException, URISyntaxException {
        FivehundredpxRipper ripper = new FivehundredpxRipper(new URI("https://marketplace.500px.com/alexander_hurman").toURL());
        testRipper(ripper);
    }
}