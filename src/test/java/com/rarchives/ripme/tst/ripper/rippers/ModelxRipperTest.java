package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ModelxRipper;

public class ModelxRipperTest extends RippersTest {
    public void testModelxAlbum() throws IOException {
        ModelxRipper ripper = new ModelxRipper(new URL("http://www.modelx.org/graphis-collection-2002-2016/ai-yuzuki-%e6%9f%9a%e6%9c%88%e3%81%82%e3%81%84-yuzuiro/"));
        testRipper(ripper);
    }
}