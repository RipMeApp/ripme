package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PornpicsRipper;

public class PornpicsRipperTest extends RippersTest {
    public void testRip() throws IOException {
        PornpicsRipper ripper = new PornpicsRipper(new URL("https://www.pornpics.com/galleries/pornstar-dahlia-sky-takes-a-fat-cock-in-her-butthole-wearing-fishnet-stockings/"));
        testRipper(ripper);
    }
}