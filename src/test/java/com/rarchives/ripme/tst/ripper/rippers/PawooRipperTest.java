package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.PawooRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PawooRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        PawooRipper ripper = new PawooRipper(new URI("https://pawoo.net/@halki/media").toURL());
        testRipper(ripper);
    }
}
