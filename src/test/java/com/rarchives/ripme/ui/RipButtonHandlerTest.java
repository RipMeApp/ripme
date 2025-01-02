package com.rarchives.ripme.ui;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class RipButtonHandlerTest {


    @Test
    @Tag("flaky")
    public void duplicateUrlTestCase() throws IOException {
        // Simulating the MainWindow in our test
        MainWindow testMainWindow = new MainWindow();
        SwingUtilities.invokeLater(testMainWindow);

        MainWindow.RipButtonHandler rbHandler = new MainWindow.RipButtonHandler(testMainWindow);
        // Creating a RipButtonHandler instance - Changing fake text to cause github to rebuild 1.

        // Add some URL to the model (assuming there's a method for adding URLs)
        testMainWindow.getRipTextfield().setText("http://example.com");
        rbHandler.actionPerformed(null);
        testMainWindow.getRipTextfield().setText("http://example.com");
        rbHandler.actionPerformed(null);

        // Assuming your MainWindow or RipButtonHandler sets some flag or state
        // indicating that a duplicate URL was encountered
        assertEquals(testMainWindow.getRipTextfield().getText(), "");
    }

}