package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.URL;

public class MastodonXyzRipper extends MastodonRipper {
    public MastodonXyzRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "mastodonxyz";
    }

    @Override
    public String getDomain() {
        return "mastodon.xyz";
    }
}
