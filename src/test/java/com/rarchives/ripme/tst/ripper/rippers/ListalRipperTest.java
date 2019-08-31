package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.ListalRipper;

public class ListalRipperTest extends RippersTest {

    public void testRip() throws IOException {
        ListalRipper ripper =
                new ListalRipper(new URL("https://www.listal.com/list/evolution-emma-stone"));
        testRipper(ripper);
    }

}
