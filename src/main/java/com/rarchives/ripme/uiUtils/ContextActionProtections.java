package com.rarchives.ripme.uiUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ContextActionProtections {
    private static final Logger logger = LogManager.getLogger(ContextActionProtections.class);

    public static void pasteFromClipboard(JTextComponent textComponent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(new Object());

        try {
            String clipboardContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);

            // TODO check if commenting this causes regression
            // Limit the pasted content to 96 characters
            // if (clipboardContent.length() > 96) {
            //     clipboardContent = clipboardContent.substring(0, 96);
            // }
            // Set the text in the JTextField
            textComponent.setText(clipboardContent);
        } catch (UnsupportedFlavorException | IOException e) {
            logger.error("Unable to paste from clipboard", e);
        }
    }
}
