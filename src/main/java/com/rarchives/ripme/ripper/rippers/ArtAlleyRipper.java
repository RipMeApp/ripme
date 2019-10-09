package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.URL;

public class ArtAlleyRipper extends MastodonRipper {
    public ArtAlleyRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "artalley";
    }

    @Override
    public String getDomain() {
        return "artalley.social";
    }
}
