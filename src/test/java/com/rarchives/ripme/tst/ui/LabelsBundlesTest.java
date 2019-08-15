package com.rarchives.ripme.tst.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.rarchives.ripme.utils.Utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

public class LabelsBundlesTest {

    private Logger logger = Logger.getLogger(Utils.class);

    @Test
    void testKeyCount() {
        ((ConsoleAppender) Logger.getRootLogger().getAppender("stdout")).setThreshold(Level.DEBUG);
        File f = new File("E:\\Downloads\\_Isaaku\\dev\\ripme-1.7.86-jar-with-dependencies.jar");
        File[] files = f.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                logger.info("name: " + name);
                return name.startsWith("LabelsBundle_");
            }

        });

        for (String s : getResourcesNames("\\**")) {
            logger.info(s);
        }

    }

    public String[] getResourcesNames(String path) {
        Class loader = getClassLoader();
        /*URL u = loader.getResource("/rip.properties");
        path = u.getFile();
        path = new File(path).getParent();*/

        try {
            URL url = loader.getResource(path);
            if (url == null) {
                return null;
            }

            URI uri = url.toURI();
            if (uri.getScheme().equals("jar")) { // Run from jar
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    Path resourcePath = fileSystem.getPath(path);

                    // Get all contents of a resource (skip resource itself), if entry is a
                    // directory remove trailing /
                    List<String> resourcesNames = Files.walk(resourcePath, 1).skip(1).map(p -> {
                        String name = p.getFileName().toString();
                        if (name.endsWith("/")) {
                            name = name.substring(0, name.length() - 1);
                        }
                        return name;
                    }).sorted().collect(Collectors.toList());

                    return resourcesNames.toArray(new String[resourcesNames.size()]);
                }
            } else { // Run from IDE
                File resource = new File(uri);
                return resource.list();
            }
        } catch (IOException e) {
            return null;
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    private Class getClassLoader() {
        return Utils.class;
        //return Thread.currentThread().getContextClassLoader();
    }
}