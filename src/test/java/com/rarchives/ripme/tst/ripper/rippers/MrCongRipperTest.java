package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.MrCongRipper;

public class MrCongRipperTest extends RippersTest {
    @Test
    public void testMrCongAlbumRip1() throws IOException, URISyntaxException {
        MrCongRipper ripper = new MrCongRipper(new URI(
                "https://misskon.com/87161-xr-uncensored-lin-xing-lan-r18-xiu-ren-jue-mi-3wan-yuan-zi-liao-chao-shi-zhang-16k-qing-te-xie-1174-photos-1-video/")
                .toURL());
        testRipper(ripper);
    }

    @Test
    public void testMrCongAlbumRip2() throws IOException, URISyntaxException {
        MrCongRipper ripper = new MrCongRipper(
                new URI("https://misskon.com/xiaoyu-vol-799-lin-xing-lan-87-anh/").toURL());

        testRipper(ripper);
    }

    @Test
    public void testMrCongAlbumRip3() throws IOException, URISyntaxException {
        MrCongRipper ripper = new MrCongRipper(
                new URI("https://misskon.com/87163-le-ledb-201b-dayoung-50-photos/").toURL());
        testRipper(ripper);
    }

    // Ripping from tags is not yet implemented. Uncomment the @Test line when
    // implemented.
    // @Test
    public void testMrCongTagRip() throws IOException, URISyntaxException {
        MrCongRipper ripper = new MrCongRipper(new URI("https://misskon.com/tag/xr-uncensored/").toURL());
        testRipper(ripper);
    }
}
