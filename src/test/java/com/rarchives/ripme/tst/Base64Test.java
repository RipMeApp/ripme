package com.rarchives.ripme.tst;

<<<<<<< HEAD
import junit.framework.TestCase;
import com.rarchives.ripme.utils.Base64;

public class Base64Test extends TestCase {

=======
import com.rarchives.ripme.utils.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64Test {

    @Test
>>>>>>> upstream/master
    public void testDecode() {
        assertEquals("test", new String(Base64.decode("dGVzdA==")));
    }
}
