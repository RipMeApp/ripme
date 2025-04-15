package com.rarchives.ripme.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpdateUtilsTest {

    @Test
    public void testIsNewerVersion() {
        Assertions.assertFalse(UpdateUtils.isNewerVersion("1.7.94"));
        Assertions.assertFalse(UpdateUtils.isNewerVersion("1.7.94-9-asdf"));
        Assertions.assertTrue(UpdateUtils.isNewerVersion("1.7.94-11-asdf"));
        Assertions.assertTrue(UpdateUtils.isNewerVersion("1.7.95"));
    }

}