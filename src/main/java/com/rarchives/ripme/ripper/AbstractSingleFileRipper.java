package com.rarchives.ripme.ripper;

import com.rarchives.ripme.utils.Utils;

import java.io.IOException;
import java.net.URL;


/**
 * This is just an extension of AbstractHTMLRipper that auto overrides a few things
 * to help cut down on copy pasted code
 */
public abstract class AbstractSingleFileRipper extends AbstractHTMLRipper {

    protected AbstractSingleFileRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public int getCompletionPercentage() {
        return (int) (100 * (bytesCompleted / (float) bytesTotal));
    }

    @Override
    public boolean useByteProgessBar() {return true;}
}
