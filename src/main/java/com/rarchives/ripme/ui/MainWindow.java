package com.rarchives.ripme.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;

/**
 * Everything UI-related starts and ends here.
 */
public class MainWindow implements Runnable, RipStatusHandler {

    private static final Logger logger = Logger.getLogger(MainWindow.class);
    
    private static final String WINDOW_TITLE = "RipMe";
    private static final String HISTORY_FILE = ".history";

    private static JFrame mainFrame;
    private static JTextField ripTextfield;
    private static JButton ripButton;

    private static JLabel statusLabel;
    private static JButton openButton;
    private static JProgressBar statusProgress;

    // Log
    private static JButton optionLog;
    private static JPanel logPanel;
    private static JTextPane logText;
    private static JScrollPane logTextScroll;

    // History
    private static JButton optionHistory;
    private static JPanel historyPanel;
    private static JList historyList;
    private static DefaultListModel historyListModel;
    private static JScrollPane historyListScroll;
    private static JPanel historyButtonPanel;
    private static JButton historyButtonRemove,
                           historyButtonClear,
                           historyButtonRerip;

    // Configuration
    private static JButton optionConfiguration;
    private static JPanel configurationPanel;
    // TODO Configuration components
    
    public MainWindow() {
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //mainFrame.setPreferredSize(new Dimension(400, 180));
        //mainFrame.setResizable(false);
        mainFrame.setLayout(new GridBagLayout());

        createUI(mainFrame.getContentPane());
        loadHistory();
        setupHandlers();
    }
    
    public void run() {
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void status(String text) {
        statusWithColor(text, Color.BLACK);
    }
    
    private void error(String text) {
        statusWithColor(text, Color.RED);
    }
    
    private void statusWithColor(String text, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
        mainFrame.pack();
    }

    private void createUI(Container pane) {
        EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 2; gbc.ipadx = 2; gbc.gridx = 0;
        gbc.weighty = 2; gbc.ipady = 2; gbc.gridy = 0;

        ripTextfield = new JTextField("", 20);
        ripButton    = new JButton("Rip");
        JPanel ripPanel = new JPanel(new GridBagLayout());
        ripPanel.setBorder(emptyBorder);

        gbc.gridx = 0; ripPanel.add(new JLabel("URL:", JLabel.RIGHT), gbc);
        gbc.gridx = 1; ripPanel.add(ripTextfield, gbc);
        gbc.gridx = 2; ripPanel.add(ripButton, gbc);

        statusLabel  = new JLabel("Inactive");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        openButton = new JButton();
        openButton.setVisible(false);
        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(emptyBorder);

        gbc.gridx = 0;
        statusPanel.add(statusLabel, gbc);
        statusPanel.add(openButton, gbc);

        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBorder(emptyBorder);
        statusProgress = new JProgressBar(0,  100);
        progressPanel.add(statusProgress, gbc);

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(emptyBorder);
        optionLog = new JButton("Log");
        optionHistory = new JButton("History");
        optionConfiguration = new JButton("Configuration");
        gbc.gridx = 0; optionsPanel.add(optionLog, gbc);
        gbc.gridx = 1; optionsPanel.add(optionHistory, gbc);
        gbc.gridx = 2; optionsPanel.add(optionConfiguration, gbc);

        logPanel = new JPanel(new GridBagLayout());
        logPanel.setBorder(emptyBorder);
        logText = new JTextPaneNoWrap();
        logTextScroll = new JScrollPane(logText);
        logPanel.setVisible(false);
        logPanel.setPreferredSize(new Dimension(300, 250));
        logPanel.add(logTextScroll, gbc);

        historyPanel = new JPanel(new GridBagLayout());
        historyPanel.setBorder(emptyBorder);
        historyPanel.setVisible(false);
        historyPanel.setPreferredSize(new Dimension(300, 250));
        historyListModel  = new DefaultListModel();
        historyList       = new JList(historyListModel);
        historyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        historyListScroll = new JScrollPane(historyList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        historyButtonRemove = new JButton("Remove");
        historyButtonClear  = new JButton("Clear");
        historyButtonRerip  = new JButton("Re-rip All");
        gbc.gridx = 0;
        JPanel historyListPanel = new JPanel(new GridBagLayout());
        historyListPanel.add(historyListScroll, gbc);
        gbc.ipady = 150;
        historyPanel.add(historyListPanel, gbc);
        gbc.ipady = 0;
        historyButtonPanel = new JPanel(new GridBagLayout());
        historyButtonPanel.setPreferredSize(new Dimension(300, 10));
        historyButtonPanel.setBorder(emptyBorder);
        gbc.gridx = 0; historyButtonPanel.add(historyButtonRemove, gbc);
        gbc.gridx = 1; historyButtonPanel.add(historyButtonClear, gbc);
        gbc.gridx = 2; historyButtonPanel.add(historyButtonRerip, gbc);
        gbc.gridy = 1; gbc.gridx = 0;
        historyPanel.add(historyButtonPanel, gbc);
        
        configurationPanel = new JPanel(new GridBagLayout());
        configurationPanel.setBorder(emptyBorder);
        configurationPanel.setVisible(false);
        configurationPanel.setPreferredSize(new Dimension(300, 250));
        // TODO Configuration components

        gbc.gridy = 0; pane.add(ripPanel, gbc);
        gbc.gridy = 1; pane.add(statusPanel, gbc);
        gbc.gridy = 2; pane.add(progressPanel, gbc);
        gbc.gridy = 3; pane.add(optionsPanel, gbc);
        gbc.gridy = 4; pane.add(logPanel, gbc);
        gbc.gridy = 5; pane.add(historyPanel, gbc);
        gbc.gridy = 5; pane.add(configurationPanel, gbc);
    }
    
    private void setupHandlers() {
        ripButton.addActionListener(new RipButtonHandler());
        ripTextfield.addActionListener(new RipButtonHandler());
        optionLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                logPanel.setVisible(!logPanel.isVisible());
                historyPanel.setVisible(false);
                configurationPanel.setVisible(false);
                mainFrame.pack();
            }
        });
        optionHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                logPanel.setVisible(false);
                historyPanel.setVisible(!historyPanel.isVisible());
                configurationPanel.setVisible(false);
                mainFrame.pack();
            }
        });
        optionConfiguration.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                logPanel.setVisible(false);
                historyPanel.setVisible(false);
                configurationPanel.setVisible(!configurationPanel.isVisible());
                mainFrame.pack();
            }
        });
        historyButtonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int[] indices = historyList.getSelectedIndices();
                for (int i = indices.length - 1; i >= 0; i--) {
                    historyListModel.remove(indices[i]);
                }
                saveHistory();
            }
        });
        historyButtonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                historyListModel.clear();
                saveHistory();
            }
        });
        
        // Re-rip all history
        historyButtonRerip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Runnable ripAllThread = new Runnable() {
                    @Override
                    public void run() {
                        historyButtonPanel.setEnabled(false);
                        historyList.setEnabled(false);
                        for (int i = 0; i < historyListModel.size(); i++) {
                            historyList.clearSelection();
                            historyList.setSelectedIndex(i);
                            Thread t = ripAlbum( (String) historyListModel.get(i) );
                            try {
                                synchronized (t) {
                                    t.wait();
                                }
                                t.join();
                            } catch (InterruptedException e) {
                                logger.error("[!] Exception while waiting for ripper to finish:", e);
                            }
                        }
                        historyList.setEnabled(true);
                        historyButtonPanel.setEnabled(true);
                    }

                };
                new Thread(ripAllThread).start();
            }
        });
    }
    
    private void appendLog(final String text, final Color color) {
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, color);
        StyledDocument sd = logText.getStyledDocument();
        try {
            sd.insertString(sd.getLength(), text + "\n", sas);
        } catch (BadLocationException e) { }

        logText.setCaretPosition(sd.getLength());
    }
    
    private void loadHistory() {
        File f; FileReader fr = null; BufferedReader br;
        try {
            f = new File(HISTORY_FILE);
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String line;
            while ( (line = br.readLine()) != null ) {
                if (!line.trim().equals("")) {
                    historyListModel.addElement(line.trim());
                }
            }
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            logger.error("[!] Error while loading history file " + HISTORY_FILE, e);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) { }
        }
    }

    private void saveHistory() {
        FileWriter fw = null;
        try {
            fw = new FileWriter(HISTORY_FILE, false);
            for (int i = 0; i < historyListModel.size(); i++) {
                fw.write( (String) historyListModel.get(i) );
                fw.write("\n");
                fw.flush();
            }
        } catch (IOException e) {
            logger.error("[!] Error while saving history file " + HISTORY_FILE, e);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) { }
        }
    }

    private Thread ripAlbum(String urlString) {
        if (urlString.toLowerCase().startsWith("gonewild:")) {
            urlString = "http://gonewild.com/user/" + urlString.substring(urlString.indexOf(':') + 1);
        }
        if (!urlString.startsWith("http")) {
            urlString = "http://" + urlString;
        }
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            logger.error("[!] Could not generate URL for '" + urlString + "'", e);
            error("Given URL is not valid, expecting http://website.com/page/...");
            return null;
        }
        ripButton.setEnabled(false);
        ripTextfield.setEnabled(false);
        statusProgress.setValue(100);
        openButton.setVisible(false);
        statusLabel.setVisible(true);
        mainFrame.pack();
        try {
            AbstractRipper ripper = AbstractRipper.getRipper(url);
            ripTextfield.setText(ripper.getURL().toExternalForm());
            status("Starting rip...");
            ripper.setObserver((RipStatusHandler) this);
            Thread t = new Thread(ripper);
            t.start();
            return t;
        } catch (Exception e) {
            logger.error("[!] Error while ripping: " + e.getMessage(), e);
            error("Unable to rip this URL: " + e.getMessage());
            ripButton.setEnabled(true);
            ripTextfield.setEnabled(true);
            statusProgress.setValue(0);
            mainFrame.pack();
            return null;
        }
    }

    class RipButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            ripAlbum(ripTextfield.getText());
        }
    }
    
    private class StatusEvent implements Runnable {
        private final AbstractRipper ripper;
        private final RipStatusMessage msg;

        public StatusEvent(AbstractRipper ripper, RipStatusMessage msg) {
            this.ripper = ripper;
            this.msg = msg;
        }

        public void run() {
            handleEvent(this);
        }
    }
    
    private void handleEvent(StatusEvent evt) {
        RipStatusMessage msg = evt.msg;

        int completedPercent = evt.ripper.getCompletionPercentage();
        statusProgress.setValue(completedPercent);
        status( evt.ripper.getStatusText() );

        switch(msg.getStatus()) {
        case LOADING_RESOURCE:
        case DOWNLOAD_STARTED:
            appendLog( "Downloading: " + (String) msg.getObject(), Color.BLACK);
            break;
        case DOWNLOAD_COMPLETE:
            appendLog( "Completed: " + (String) msg.getObject(), Color.GREEN);
            break;
        case DOWNLOAD_ERRORED:
            appendLog( "Error: " + (String) msg.getObject(), Color.RED);
            break;

        case DOWNLOAD_WARN:
            appendLog( "Warn: " + (String) msg.getObject(), Color.ORANGE);
            break;

        case RIP_COMPLETE:
            if (!historyListModel.contains(ripTextfield.getText())) {
                historyListModel.addElement(ripTextfield.getText());
            }
            saveHistory();
            ripButton.setEnabled(true);
            ripTextfield.setEnabled(true);
            statusProgress.setValue(100);
            statusLabel.setVisible(false);
            openButton.setVisible(true);
            File f = (File) msg.getObject();
            String prettyFile = Utils.removeCWD(f);
            openButton.setText("Open " + prettyFile);
            appendLog( "Rip complete, saved to " + prettyFile, Color.GREEN);
            openButton.setActionCommand(f.toString());
            openButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
                        Desktop.getDesktop().open(new File(event.getActionCommand()));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
            mainFrame.pack();
        }
    }

    public void update(AbstractRipper ripper, RipStatusMessage message) {
        StatusEvent event = new StatusEvent(ripper, message);
        SwingUtilities.invokeLater(event);
    }
    
    /** Simple TextPane that allows horizontal scrolling. */
    class JTextPaneNoWrap extends JTextPane {
        private static final long serialVersionUID = 1L;
        
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }
    }
}