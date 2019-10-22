package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.URL;

public class BaraagRipper extends MastodonRipper {
    public BaraagRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "baraag";
    }

    @Override
    public String getDomain() {
        return "baraag.net";
    }
}
