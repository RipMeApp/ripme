package com.rarchives.ripme.tst.ui;

import com.rarchives.ripme.ui.RipStatusMessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RipStatusMessageTest {

    @Test
    public void testConstructor() {
        RipStatusMessage.STATUS loadingResource = RipStatusMessage.STATUS.LOADING_RESOURCE;
        String path = "path/to/file";
        String toStringValue = "Loading Resource: " + path;

        RipStatusMessage ripStatusMessage = new RipStatusMessage(loadingResource, path);

        Assertions.assertEquals(loadingResource, ripStatusMessage.getStatus());
        Assertions.assertEquals(path, ripStatusMessage.getObject());
        Assertions.assertEquals(toStringValue, ripStatusMessage.toString());
    }

}