package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NudeGalsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NudeGalsRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        Assertions.assertEquals("5541", ripper.getGID( new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541")));
    }
}
