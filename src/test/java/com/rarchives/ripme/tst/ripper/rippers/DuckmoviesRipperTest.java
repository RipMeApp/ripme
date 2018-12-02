package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.DuckmoviesRipper;

import java.io.IOException;
import java.net.URL;

public class DuckmoviesRipperTest extends RippersTest{

    public void testRip() throws IOException {
        DuckmoviesRipper ripper = new DuckmoviesRipper(new URL("https://palapaja.com/spyfam-stepbro-gives-in-to-stepsis-asian-persuasion/"));
        testRipper(ripper);
    }

}