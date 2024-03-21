package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.BaraagRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class BaraagRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        BaraagRipper ripper = new BaraagRipper(new URI("https://baraag.net/@darkshadow777/media").toURL());
        testRipper(ripper);
    }
}
