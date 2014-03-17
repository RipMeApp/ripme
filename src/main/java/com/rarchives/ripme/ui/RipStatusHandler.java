
package com.rarchives.ripme.ui;

import com.rarchives.ripme.ripper.AbstractRipper;

/**
 *
 * @author Mads
 */
public interface RipStatusHandler {

    public void update(AbstractRipper ripper, RipStatusMessage message);

}
