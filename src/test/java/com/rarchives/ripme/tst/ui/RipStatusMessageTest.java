package com.rarchives.ripme.tst.ui;

import com.rarchives.ripme.ui.RipStatusMessage;
import junit.framework.Assert;
import junit.framework.TestCase;

public class RipStatusMessageTest extends TestCase {

    public void testConstructor() {
        RipStatusMessage.STATUS loadingResource = RipStatusMessage.STATUS.LOADING_RESOURCE;
        String path = "path/to/file";
        String toStringValue = "Loading Resource: " + path;

        RipStatusMessage ripStatusMessage = new RipStatusMessage(loadingResource, path);

        Assert.assertEquals(loadingResource, ripStatusMessage.getStatus());
        Assert.assertEquals(path, ripStatusMessage.getObject());
        Assert.assertEquals(toStringValue, ripStatusMessage.toString());
    }

}