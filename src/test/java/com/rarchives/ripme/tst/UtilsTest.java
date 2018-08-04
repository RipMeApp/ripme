package com.rarchives.ripme.tst;

import junit.framework.TestCase;
import com.rarchives.ripme.utils.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class UtilsTest extends TestCase {

    public void testGetEXTFromMagic() {
        assertEquals("jpeg", Utils.getEXTFromMagic(new byte[]{-1, -40, -1, -37, 0, 0, 0, 0}));
        assertEquals("png", Utils.getEXTFromMagic(new byte[]{-119, 80, 78, 71, 13, 0, 0, 0}));
    }

    public void testStripURLParameter() {
        assertEquals("http://example.tld/image.ext",
                Utils.stripURLParameter("http://example.tld/image.ext?param", "param"));
    }

    public void testShortenPath() {
        String path = "/test/test/test/test/test/test/test/test/";
        assertEquals("/test/test1", Utils.shortenPath("/test/test1"));
        assertEquals("/test/test/t...st/test/test", Utils.shortenPath(path));
    }

    public void testBytesToHumanReadable() {
        assertEquals("10.00iB", Utils.bytesToHumanReadable(10));
        assertEquals("1.00KiB", Utils.bytesToHumanReadable(1024));
        assertEquals("1.00MiB", Utils.bytesToHumanReadable(1024 * 1024));
        assertEquals("1.00GiB", Utils.bytesToHumanReadable(1024 * 1024 * 1024));
    }

    public void testGetListOfAlbumRippers() throws Exception{
        assert(!Utils.getListOfAlbumRippers().isEmpty());
    }

    public void testGetByteStatusText() {
        assertEquals("5%  - 500.00iB / 97.66KiB", Utils.getByteStatusText(5, 500, 100000));
    }

    public void testBetween() {
        assertEquals(Arrays.asList(" is a "), Utils.between("This is a test", "This", "test"));
    }

}
