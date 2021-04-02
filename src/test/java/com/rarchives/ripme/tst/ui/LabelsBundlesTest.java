package com.rarchives.ripme.tst.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

import com.rarchives.ripme.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class LabelsBundlesTest {
    private Logger logger = LogManager.getLogger(Utils.class);
    private static final String DEFAULT_LANG = "en_US";

    @Test
    void testKeyCount() {
        ResourceBundle defaultBundle = Utils.getResourceBundle(null);
        HashMap<String, ArrayList<String>> dictionary = new HashMap<>();
        for (String lang : Utils.getSupportedLanguages()) {
            ResourceBundle.clearCache();
            if (lang.equals(DEFAULT_LANG))
                continue;
            ResourceBundle selectedLang = Utils.getResourceBundle(lang);
            for (final Enumeration<String> keys = defaultBundle.getKeys(); keys.hasMoreElements();) {
                String element = keys.nextElement();
                if (selectedLang.containsKey(element)
                        && !selectedLang.getString(element).equals(defaultBundle.getString(element))) {
                    if (dictionary.get(lang) == null)
                        dictionary.put(lang, new ArrayList<>());
                    dictionary.get(lang).add(element);
                }
            }
        }

        dictionary.keySet().forEach(d -> {
            logger.warn(String.format("Keys missing in %s", d));
            dictionary.get(d).forEach(v -> logger.warn(v));
            logger.warn("\n");
        });
    }

    @Test
    void testKeyName() {
        ResourceBundle defaultBundle = Utils.getResourceBundle(null);
        Set<String> defaultSet = defaultBundle.keySet();
        for (String lang : Utils.getSupportedLanguages()) {
            if (lang.equals(DEFAULT_LANG))
                continue;
            for (String key : Utils.getResourceBundle(lang).keySet()) {
                assertTrue(defaultSet.contains(key),
                        String.format("The key %s of %s is not in the default bundle", key, lang));
            }
        }
    }
}