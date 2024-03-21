package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.FemjoyhunterRipper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class FemjoyhunterRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        FemjoyhunterRipper ripper = new FemjoyhunterRipper(new URI(
                "https://www.femjoyhunter.com/alisa-i-got-nice-big-breasts-and-fine-ass-so-she-seems-to-be-a-hottest-brunette-5936/").toURL());
        testRipper(ripper);
    }
}