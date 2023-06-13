package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.PorncomixinfoRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PorncomixinfoRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        PorncomixinfoRipper ripper = new PorncomixinfoRipper(new URI("https://porncomixinfo.net/chapter/alx-come-to-naught-down-in-flames-up-in-smoke-tracy-scops/alx-come-to-naught-down-in-flames-up-in-smoke-tracy-scops/").toURL());
        testRipper(ripper);
    }
}
