package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.HqpornerRipper;

import java.io.IOException;
import java.net.URL;

public class HqpornerRipperTest extends RippersTest{

    public void testRip() throws IOException {
        HqpornerRipper ripper = new HqpornerRipper(new URL("https://hqporner.com/hdporn/84636-pool_lesson_with_a_cheating_husband.html"));
        testRipper(ripper);
    }

}