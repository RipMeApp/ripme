package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.YoupornRipper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YoupornRipperTest  extends RippersTest {
    public void testYoupornRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("http://www.youporn.com/watch/7669155/mrs-li-amateur-69-orgasm/?from=categ"));
        for (URL url : contentURLs) {
            YoupornRipper ripper = new YoupornRipper(url);
            testRipper(ripper);
        }
    }
}
