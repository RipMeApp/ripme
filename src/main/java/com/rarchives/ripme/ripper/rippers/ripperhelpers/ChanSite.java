/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rarchives.ripme.ripper.rippers.ripperhelpers;

import java.util.List;

/**
 *
 * @author Erwin
 */
public class ChanSite {
    //The domains where the threads are hosted.
    public List<String> domains;
    //The domains where the images are hosted.
    public List<String> cdnDomains;

    public ChanSite(List<String> Domains, List<String> CdnDomains){
        if(Domains.isEmpty())
            throw new IllegalArgumentException("Domains");
        if(CdnDomains.isEmpty())
            throw new IllegalArgumentException("CdnDomains");
        domains = Domains;
        cdnDomains = CdnDomains;            
    }
    public ChanSite(List<String> Domains){
        if(Domains.isEmpty())
            throw new IllegalArgumentException("Domains");            
        domains = Domains;
        cdnDomains = Domains;    
    }
}
