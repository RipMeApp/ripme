package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HbrowseRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class HbrowseRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testPahealRipper() throws IOException, URISyntaxException {
        HbrowseRipper ripper = new HbrowseRipper(new URI("https://www.hbrowse.com/21013/c00001").toURL());
        testRipper(ripper);
    }
}
