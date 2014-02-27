package com.rarchives.ripme;

import java.net.URL;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.rippers.ImgurRipper;

/**
 *
 */
public class App {
    public static void main( String[] args ) throws Exception {
        Logger logger = Logger.getLogger(App.class);
        logger.debug("Initialized");
        //URL url = new URL("http://www.imagefap.com/pictures/4117023/Mirror-flat-stomach-small-firm-tits");
        URL url = new URL("http://imgur.com/a/Ox6jN");
        try {
                ImgurRipper ir = new ImgurRipper(url);
                ir.rip();
        } catch (Exception e) {
            logger.error("Caught exception:", e);
            throw e;
        }
    }
    
    public static void initialize() {
        
    }
}
