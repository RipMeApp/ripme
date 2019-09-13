package com.rarchives.ripme.tst;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import com.rarchives.ripme.utils.Utils;

import org.junit.jupiter.api.Assertions;

public class UtilsTest {

    public void testGetEXTFromMagic() {
        Assertions.assertEquals("jpeg", Utils.getEXTFromMagic(new byte[] { -1, -40, -1, -37, 0, 0, 0, 0 }));
        Assertions.assertEquals("png", Utils.getEXTFromMagic(new byte[] { -119, 80, 78, 71, 13, 0, 0, 0 }));
    }

    public void testStripURLParameter() {
        Assertions.assertEquals("http://example.tld/image.ext",
                Utils.stripURLParameter("http://example.tld/image.ext?param", "param"));
    }

    public void testShortenPath() {
        String path = "/test/test/test/test/test/test/test/test/";
        Assertions.assertEquals("/test/test1", Utils.shortenPath("/test/test1"));
        Assertions.assertEquals("/test/test/t...st/test/test", Utils.shortenPath(path));
    }

    public void testBytesToHumanReadable() {
        Assertions.assertEquals("10.00iB", Utils.bytesToHumanReadable(10));
        Assertions.assertEquals("1.00KiB", Utils.bytesToHumanReadable(1024));
        Assertions.assertEquals("1.00MiB", Utils.bytesToHumanReadable(1024 * 1024));
        Assertions.assertEquals("1.00GiB", Utils.bytesToHumanReadable(1024 * 1024 * 1024));
    }

    public void testGetListOfAlbumRippers() throws Exception {
        assert (!Utils.getListOfAlbumRippers().isEmpty());
    }

    public void testGetByteStatusText() {
        Assertions.assertEquals("5%  - 500.00iB / 97.66KiB", Utils.getByteStatusText(5, 500, 100000));
    }

    public void testBetween() {
        Assertions.assertEquals(Arrays.asList(" is a "), Utils.between("This is a test", "This", "test"));
    }

    public void testShortenFileNameWindows() throws FileNotFoundException {
        String filename = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff.png";
        // Test filename shortening for windows
        File f = Utils.shortenSaveAsWindows("D:/rips/test/reddit/deep", filename);
        Assertions.assertEquals(new File(
                "D:/rips/test/reddit/deep/fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff.png"),
                f);
    }

    public void testSanitizeSaveAs() {
        Assertions.assertEquals("This is a _ !__ test", Utils.sanitizeSaveAs("This is a \" !<? test"));
    }

}
