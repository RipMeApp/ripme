package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DribbbleRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class DribbbleRipperTest extends RippersTest {
    @Test
<<<<<<< HEAD
=======
    @Disabled("test or ripper broken")
>>>>>>> upstream/master
    public void testDribbbleRip() throws IOException {
        DribbbleRipper ripper = new DribbbleRipper(new URL("https://dribbble.com/typogriff"));
        testRipper(ripper);
    }
}
