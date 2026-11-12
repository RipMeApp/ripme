package com.rarchives.ripme.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RipStatusTest {

    @AfterEach
    public void resetFlag() {
        // RipStatus holds process-wide state; make sure one test's panic
        // request can't leak into the next test.
        RipStatus.consumePanicAll();
    }

    @Test
    public void consumePanicAllIsFalseByDefault() {
        Assertions.assertFalse(RipStatus.consumePanicAll());
    }

    @Test
    public void consumePanicAllReturnsTrueAfterRequest() {
        RipStatus.requestPanicAll();
        Assertions.assertTrue(RipStatus.consumePanicAll());
    }

    @Test
    public void consumePanicAllResetsAfterBeingRead() {
        RipStatus.requestPanicAll();

        Assertions.assertTrue(RipStatus.consumePanicAll());
        // A second read, with no new request in between, must not see the
        // same panic again - otherwise a disk-full panic from one rip
        // session would leak into the next, unrelated one.
        Assertions.assertFalse(RipStatus.consumePanicAll());
    }

}
