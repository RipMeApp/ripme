package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.PornpicsRipper;

public class PornpicsRipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        PornpicsRipper ripper = new PornpicsRipper(new URI("https://www.pornpics.com/galleries/pornstar-dahlia-sky-takes-a-fat-cock-in-her-butthole-wearing-fishnet-stockings/").toURL());
        testRipper(ripper);
    }
}