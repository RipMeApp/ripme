package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DrawcrowdRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DrawcrowdRipperTest extends RippersTest {
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/304 -- Drawcrowd broken (site changed)")
    public void testRip() throws IOException {
        DrawcrowdRipper ripper = new DrawcrowdRipper(new URL("https://drawcrowd.com/rabbiteyes"));
        testRipper(ripper);
    }

}
