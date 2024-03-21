package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.NudeGalsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NudeGalsRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI("https://nude-gals.com/photoshoot.php?photoshoot_id=5541").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URI("https://nude-gals.com/photoshoot.php?photoshoot_id=5541").toURL());
        Assertions.assertEquals("5541", ripper.getGID( new URI("https://nude-gals.com/photoshoot.php?photoshoot_id=5541").toURL()));
    }
}
