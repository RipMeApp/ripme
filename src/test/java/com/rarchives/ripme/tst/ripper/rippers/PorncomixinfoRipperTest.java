package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PorncomixinfoRipper;
import org.junit.Test;

public class PorncomixinfoRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        PorncomixinfoRipper ripper = new PorncomixinfoRipper(new URL("https://porncomixinfo.net/chapter/alx-come-to-naught-down-in-flames-up-in-smoke-tracy-scops/alx-come-to-naught-down-in-flames-up-in-smoke-tracy-scops/"));
        testRipper(ripper);
    }
}
