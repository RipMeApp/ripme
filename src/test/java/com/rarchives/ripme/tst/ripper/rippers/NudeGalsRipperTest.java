package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NudeGalsRipper;

public class NudeGalsRipperTest extends RippersTest {
    public void testRip() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        assertEquals("5541", ripper.getGID( new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541")));
    }
}
