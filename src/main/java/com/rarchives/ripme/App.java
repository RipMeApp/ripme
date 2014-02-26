package com.rarchives.ripme;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rarchives.ripme.ripper.rippers.ImagefapRipper;

/**
 *
 */
public class App {
    public static void main( String[] args ) throws IOException {
        Logger logger = Logger.getLogger(App.class);
        PropertyConfigurator.configure("config/log4j.properties");
        logger.debug("Testing");
        URL url = new URL("http://www.imagefap.com/pictures/4117023/Mirror-flat-stomach-small-firm-tits");
        System.out.println("URL: " + url.toExternalForm());
        ImagefapRipper ir = new ImagefapRipper(url);
        System.out.println("Ripping");
        ir.rip();
    }
    
    public static void initialize() {
        
    }
}
