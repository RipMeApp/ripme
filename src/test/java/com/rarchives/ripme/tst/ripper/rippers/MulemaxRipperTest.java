package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MulemaxRipper;
import com.rarchives.ripme.utils.Utils;

public class MulemaxRipperTest extends RippersTest {
    
    public void testMulemaxVideo() throws IOException {
        MulemaxRipper ripper = new MulemaxRipper(new URL("https://mulemax.com/video/1720/emma-and-her-older-sissy-are-home-for-a-holiday-break"));  //pick any video from the front page
        testRipper(ripper);
    }

}