package com.rarchives.ripme.ui;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.AbstractRipper2;
import com.rarchives.ripme.ripper.AlbumRipper;

/**
 * @author Mads
 */
public interface RipStatusHandler {

    void update(AbstractRipper2 ripper, RipStatusMessage message);
    void update(AbstractRipper ripper, RipStatusMessage message);

}