package com.rarchives.ripme.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.rarchives.ripme.utils.SiteCookieStorage;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.utils.browser.BrowserCookieImporter;

public class CookieSettingsDialog extends JDialog {

    private final JTextField domainField;
    private final JTextArea cookieArea;
    private final JButton importButton;

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
        JPanel helpPanel = new JPanel(new BorderLayout(4, 4));
        helpPanel.add(new JLabel("<html>" + Utils.getLocalizedString("cookies.help") + "</html>"), BorderLayout.NORTH);
        JCheckBox autoImportCheckbox = new JCheckBox(Utils.getLocalizedString("cookies.import.auto"),
                Utils.getConfigBoolean(BrowserCookieImporter.CONFIG_AUTO_IMPORT, false));
        autoImportCheckbox.addActionListener(e -> Utils.setConfigBoolean(BrowserCookieImporter.CONFIG_AUTO_IMPORT,
                autoImportCheckbox.isSelected()));
        helpPanel.add(autoImportCheckbox, BorderLayout.SOUTH);
        bottom.add(helpPanel, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        importButton = new JButton(Utils.getLocalizedString("cookies.import"));
        JButton saveButton = new JButton(Utils.getLocalizedString("cookies.save"));
        JButton clearButton = new JButton(Utils.getLocalizedString("cookies.clear"));
        JButton closeButton = new JButton(Utils.getLocalizedString("cookies.close"));
        buttons.add(importButton);
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(closeButton);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        importButton.addActionListener(e -> importFromBrowser());
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

    private void importFromBrowser() {
        String domain = SiteCookieStorage.normalizeDomain(domainField.getText());
        String title = Utils.getLocalizedString("cookies.dialog.title");
        if (domain.isEmpty()) {
            JOptionPane.showMessageDialog(this, Utils.getLocalizedString("cookies.domain.required"), title,
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                String.format(Utils.getLocalizedString("cookies.import.confirm"), domain), title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        importButton.setEnabled(false);
        new SwingWorker<BrowserCookieImporter.ImportResult, Void>() {
            @Override
            protected BrowserCookieImporter.ImportResult doInBackground() {
                return BrowserCookieImporter.importFor(domain);
            }

            @Override
            protected void done() {
                importButton.setEnabled(true);
                try {
                    showImportResult(domain, get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(CookieSettingsDialog.this,
                            String.format(Utils.getLocalizedString("cookies.import.failed"), ex.getCause()),
                            title, JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showImportResult(String domain, BrowserCookieImporter.ImportResult result) {
        String title = Utils.getLocalizedString("cookies.dialog.title");
        if (!result.found()) {
            String message = result.undecryptable() > 0
                    ? String.format(Utils.getLocalizedString("cookies.import.undecryptable"), result.undecryptable())
                    : String.format(Utils.getLocalizedString("cookies.import.none"), domain);
            if (!result.notes().isEmpty()) {
                message += "\n\n" + String.join("\n", result.notes());
            }
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            SiteCookieStorage.save(domain, result.cookieHeader());
            cookieArea.setText(result.cookieHeader());
            JOptionPane.showMessageDialog(this,
                    String.format(Utils.getLocalizedString("cookies.import.success"), result.cookies().size(), domain,
                            result.browser().displayName()),
                    title, JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
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
