package com.rarchives.ripme.ui;

import javax.swing.JLabel;
import java.awt.Dimension;
import java.util.Objects;

/**
 * GridBagLayout does not respect minimum size, only preferred size.
 * In order to set a minimum width, we need to override getPreferredSize.
 */
public class MinimumWidthLabel extends JLabel {
    private String minimumWidthText;
    private String currentText;

    public MinimumWidthLabel(String minimumWidthText, String defaultText) {
        this.minimumWidthText = minimumWidthText;
        setText(defaultText);
    }

    @Override
    public Dimension getPreferredSize() {
        String text = getText();
        Dimension preferredSize = super.getPreferredSize();
        setText(minimumWidthText);
        int minimumWidth = super.getPreferredSize().width;
        // Hopefully GridBagLayout correctly handles maximum size and we don't need to clamp
        preferredSize.width = Math.max(minimumWidth, preferredSize.width);
        setText(text);
        return preferredSize;
    }

    public void setMinimumWidthText(String minimumWidthText) {
        this.minimumWidthText = minimumWidthText;
    }

    @Override
    public void setText(String text) {
        // Cache the last text to save cycles,
        // because this may be called with the same value very often
        if (!Objects.equals(currentText, text)) {
            currentText = text;
            super.setText(text);
        }
    }
}
