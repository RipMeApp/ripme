package com.rarchives.ripme.uiUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ContextActionProtections {
    public static void pasteFromClipboard(JTextComponent textComponent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(new Object());

        try {
            String clipboardContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);

            // Limit the pasted content to 96 characters
            if (clipboardContent.length() > 96) {
                clipboardContent = clipboardContent.substring(0, 96);
            }
            // Set the text in the JTextField
            textComponent.setText(clipboardContent);
        } catch (UnsupportedFlavorException | IOException unable_to_modify_text_on_paste) {
            unable_to_modify_text_on_paste.printStackTrace();
        }
    }
}
