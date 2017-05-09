package com.rarchives.ripme.ripper.rippers.ripperhelpers;

import java.util.List;

public class ChanSite {
    // The domains where the threads are hosted.
    public List<String> domains;
    // The domains where the images are hosted.
    public List<String> cdnDomains;

    public ChanSite(List<String> Domains, List<String> CdnDomains) {
        if (Domains.isEmpty()) {
            throw new IllegalArgumentException("Domains");
        }
        if (CdnDomains.isEmpty()) {
            throw new IllegalArgumentException("CdnDomains");
        }
        domains = Domains;
        cdnDomains = CdnDomains;
    }

    public ChanSite(List<String> Domains) {
        if (Domains.isEmpty()) {
            throw new IllegalArgumentException("Domains");
        }
        domains = Domains;
        cdnDomains = Domains;
    }
}
