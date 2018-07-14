package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.LoveromRipper;

import java.io.IOException;
import java.net.URL;

public class LoveromRipperTest extends RippersTest{
    public void testRip() throws IOException {
        LoveromRipper ripper = new LoveromRipper(new URL("https://www.loveroms.com/download/nintendo/adventures-of-tom-sawyer-u/107165"));
        testRipper(ripper);
    }
}
