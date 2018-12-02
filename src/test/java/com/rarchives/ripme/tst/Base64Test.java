package com.rarchives.ripme.tst;

import junit.framework.TestCase;
import com.rarchives.ripme.utils.Base64;

public class Base64Test extends TestCase {

    public void testDecode() {
        assertEquals("test", new String(Base64.decode("dGVzdA==")));
    }
}
