package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FemjoyhunterRipper;

public class FemjoyhunterRipperTest extends RippersTest {
    public void testRip() throws IOException {
        FemjoyhunterRipper ripper = new FemjoyhunterRipper(new URL("https://www.femjoyhunter.com/alisa-i-got-nice-big-breasts-and-fine-ass-so-she-seems-to-be-a-hottest-brunette-5936/"));
        testRipper(ripper);
    }
}