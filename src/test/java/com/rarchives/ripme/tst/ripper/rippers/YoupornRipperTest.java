package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.YoupornRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YoupornRipperTest  extends RippersTest {
    @Test
    @Tag("flaky")
    public void testYoupornRipper() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        // Video cannot be loaded: "Video has been flagged for verification"
        //contentURLs.add(new URI("http://www.youporn.com/watch/7669155/mrs-li-amateur-69-orgasm/?from=categ").toURL());
        contentURLs.add(new URI("https://www.youporn.com/watch/13158849/smashing-star-slut-part-2/").toURL());
        for (URL url : contentURLs) {
            YoupornRipper ripper = new YoupornRipper(url);
            testRipper(ripper);
        }
    }
}
