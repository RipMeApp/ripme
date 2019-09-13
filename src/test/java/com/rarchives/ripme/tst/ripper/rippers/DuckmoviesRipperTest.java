package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.DuckmoviesRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class DuckmoviesRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testRip() throws IOException {
        DuckmoviesRipper ripper = new DuckmoviesRipper(
                new URL("https://palapaja.com/spyfam-stepbro-gives-in-to-stepsis-asian-persuasion/"));
        testRipper(ripper);
    }

}