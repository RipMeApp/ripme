package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WebtoonsRipper;

public class WebtoonsRipperTest extends RippersTest {
    public void testWebtoonsAlbum() throws IOException {
        WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://www.webtoons.com/en/drama/my-boo/ep-33/viewer?title_no=1185&episode_no=33"));
        testRipper(ripper);
    }

    public void testWebtoonsType() throws IOException {
    	WebtoonsRipper ripper = new WebtoonsRipper(new URL("http://webtoon.phinf.naver.net/20180103_72/1514974518278wi4tU_JPEG/151497451824910491454.jpg?type=q90"));
    	testRipper(ripper);
    }
}
