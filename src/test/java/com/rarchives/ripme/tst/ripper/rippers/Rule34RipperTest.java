package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.Rule34Ripper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class Rule34RipperTest extends RippersTest {
    @Test
    public void testShesFreakyRip() throws IOException {
        Rule34Ripper ripper = new Rule34Ripper(new URL("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL url = new URL("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo");
        Rule34Ripper ripper = new Rule34Ripper(url);
        assertEquals("bimbo", ripper.getGID(url));
=======
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo");
        Rule34Ripper ripper = new Rule34Ripper(url);
        Assertions.assertEquals("bimbo", ripper.getGID(url));
>>>>>>> upstream/master
    }

}
