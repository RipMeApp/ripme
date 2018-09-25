package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SpankbangRipper;

public class SpankBangRipperTest extends RippersTest {

    public void testSpankBangVideo() throws IOException {
        SpankbangRipper ripper = new SpankbangRipper(new URL("https://spankbang.com/2a7fh/video/mdb901"));  //most popular video of all time on site; should stay up
        testRipper(ripper);
    }

}
