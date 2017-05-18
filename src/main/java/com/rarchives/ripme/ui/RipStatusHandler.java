package com.rarchives.ripme.ui;

import com.rarchives.ripme.ripper.AbstractRipper;

/**
 * @author Mads
 */
public interface RipStatusHandler {

    void update(AbstractRipper ripper, RipStatusMessage message);

}