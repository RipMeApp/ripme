package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.URL;

public class PawooRipper extends MastodonRipper {
    public PawooRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "pawoo";
    }

    @Override
    public String getDomain() {
        return "pawoo.net";
    }

}
