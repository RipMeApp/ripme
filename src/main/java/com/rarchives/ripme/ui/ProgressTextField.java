package com.rarchives.ripme.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class ProgressTextField extends JProgressBar {
    private static final Logger logger = LogManager.getLogger(ProgressTextField.class);
    private final JTextField textField = new JTextField();
    private final JLabel valueLabel = new JLabel();

    public ProgressTextField() {
        super(0, 100);
        setLayout(new BorderLayout());

        // Paint an empty string to reserve height for the text field
        super.setStringPainted(true);
        progressString = ""; // Directly set here so setString can be overridden

        textField.setOpaque(false);
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        add(textField, BorderLayout.CENTER);

        valueLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, valueLabel.getFont().getSize()));
        valueLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        setValue(0);

        add(valueLabel, BorderLayout.EAST);
    }

    @Override
    public void setString(String s) {
        logger.warn("bug: progress bar should use setText, not setString");
    }

    @Override
    public void setValue(int n) {
        super.setValue(n);
        int minimum = getMinimum();
        // note: integer division
        valueLabel.setText(100 * (n - minimum) / (getMaximum() - minimum) + "%");
    }

    public void setText(String t) {
        textField.setText(t);
    }
}
