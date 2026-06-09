package com.rarchives.ripme.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.rarchives.ripme.utils.SiteCookieStorage;
import com.rarchives.ripme.utils.Utils;

public class CookieSettingsDialog extends JDialog {

    private final JTextField domainField;
    private final JTextArea cookieArea;

    public CookieSettingsDialog(Frame owner) {
        super(owner, Utils.getLocalizedString("cookies.dialog.title"), true);
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(new JLabel(Utils.getLocalizedString("cookies.domain")), BorderLayout.WEST);
        domainField = new JTextField("reddit.com", 24);
        top.add(domainField, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        cookieArea = new JTextArea(8, 50);
        cookieArea.setLineWrap(true);
        cookieArea.setWrapStyleWord(true);
        add(new JScrollPane(cookieArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.add(new JLabel("<html>" + Utils.getLocalizedString("cookies.help") + "</html>"), BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton(Utils.getLocalizedString("cookies.save"));
        JButton clearButton = new JButton(Utils.getLocalizedString("cookies.clear"));
        JButton closeButton = new JButton(Utils.getLocalizedString("cookies.close"));
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(closeButton);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveCookies());
        clearButton.addActionListener(e -> clearCookies());
        closeButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    public void loadDomain(String domain) {
        domainField.setText(domain);
        try {
            cookieArea.setText(SiteCookieStorage.load(domain));
        } catch (IOException ex) {
            cookieArea.setText("");
        }
    }

    private void saveCookies() {
        String domain = domainField.getText();
        if (SiteCookieStorage.normalizeDomain(domain).isEmpty()) {
            JOptionPane.showMessageDialog(this, Utils.getLocalizedString("cookies.domain.required"),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            SiteCookieStorage.save(domain, cookieArea.getText());
            JOptionPane.showMessageDialog(this, Utils.getLocalizedString("cookies.saved"),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, Utils.getLocalizedString("cookies.invalid"),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCookies() {
        String domain = domainField.getText();
        if (SiteCookieStorage.normalizeDomain(domain).isEmpty()) {
            return;
        }
        try {
            SiteCookieStorage.clear(domain);
            cookieArea.setText("");
            JOptionPane.showMessageDialog(this, Utils.getLocalizedString("cookies.cleared"),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    Utils.getLocalizedString("cookies.dialog.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
