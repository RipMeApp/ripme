package com.rarchives.ripme.tst;

import com.rarchives.ripme.utils.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64Test {

    @Test
    public void testDecode() {
        assertEquals("test", new String(Base64.decode("dGVzdA==")));
    }
}
