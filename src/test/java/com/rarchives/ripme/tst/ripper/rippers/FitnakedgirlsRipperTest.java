package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.FitnakedgirlsRipper;

public class FitnakedgirlsRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException, URISyntaxException {
        FitnakedgirlsRipper ripper = new FitnakedgirlsRipper(new URI("https://fitnakedgirls.com/photos/gallery/erin-ashford-nude/").toURL());
        testRipper(ripper);
    }
}
