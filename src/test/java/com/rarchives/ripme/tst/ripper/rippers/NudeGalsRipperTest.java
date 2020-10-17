package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NudeGalsRipper;
<<<<<<< HEAD

public class NudeGalsRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NudeGalsRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testRip() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        assertEquals("5541", ripper.getGID( new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541")));
=======
    @Test
    public void testGetGID() throws IOException {
        NudeGalsRipper ripper = new NudeGalsRipper(new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541"));
        Assertions.assertEquals("5541", ripper.getGID( new URL("https://nude-gals.com/photoshoot.php?photoshoot_id=5541")));
>>>>>>> upstream/master
    }
}
