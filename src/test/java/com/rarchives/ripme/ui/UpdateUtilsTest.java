package com.rarchives.ripme.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpdateUtilsTest {

    @Test
    public void testIsNewerVersion() {
        UpdateUtils updateUtils = new UpdateUtils();
        Assertions.assertFalse(updateUtils.isNewerVersion("1.7.94"));
        Assertions.assertFalse(updateUtils.isNewerVersion("1.7.94-9-asdf"));
        Assertions.assertTrue(updateUtils.isNewerVersion("1.7.94-11-asdf"));
        Assertions.assertTrue(updateUtils.isNewerVersion("1.7.95"));
    }

}