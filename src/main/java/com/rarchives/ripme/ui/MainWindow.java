package com.rarchives.ripme.ui;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

import javax.swing.UnsupportedLookAndFeelException;

/**
 * Everything UI-related starts and ends here.
 */
public final class MainWindow implements Runnable, RipStatusHandler {

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    private boolean isRipping = false; // Flag to indicate if we're ripping something

    private static JFrame mainFrame;
    private static JTextField ripTextfield;
    private static JButton ripButton,
                           stopButton;

    private static JLabel statusLabel;
    private static JButton openButton;
    private static JProgressBar statusProgress;

    // Put an empty JPanel on the bottom of the window to keep components
    // anchored to the top when there is no open lower panel
    private static JPanel emptyPanel;

    // Log
    private static JButton optionLog;
    private static JPanel logPanel;
    private static JTextPane logText;

    // History
    private static JButton optionHistory;
    private static final History HISTORY = new History();
    private static JPanel historyPanel;
    private static JTable historyTable;
    private static AbstractTableModel historyTableModel;
    private static JButton historyButtonRemove,
                           historyButtonClear,
                           historyButtonRerip;

    // Queue
    public static JButton optionQueue;
    private static JPanel queuePanel;
    private static DefaultListModel queueListModel;

    // Configuration
    private static JButton optionConfiguration;
    private static JPanel configurationPanel;
    private static JButton configUpdateButton;
    private static JLabel configUpdateLabel;
    private static JTextField configTimeoutText;
    private static JTextField configThreadsText;
    private static JCheckBox configOverwriteCheckbox;
    private static JLabel configSaveDirLabel;
    private static JButton configSaveDirButton;
    private static JTextField configRetriesText;
    private static JCheckBox configAutoupdateCheckbox;
    private static JComboBox<String> configLogLevelCombobox;
    private static JCheckBox configURLHistoryCheckbox;
    private static JCheckBox configPlaySound;
    private static JCheckBox configSaveOrderCheckbox;
    private static JCheckBox configShowPopup;
    private static JCheckBox configSaveLogs;
    private static JCheckBox configSaveURLsOnly;
    private static JCheckBox configSaveAlbumTitles;
    private static JCheckBox configClipboardAutorip;
    private static JCheckBox configSaveDescriptions;
    private static JCheckBox configPreferMp4;
    private static JCheckBox configWindowPosition;
    private static JComboBox<String> configSelectLangComboBox;
    private static JLabel configThreadsLabel;
    private static JLabel configTimeoutLabel;
    private static JLabel configRetriesLabel;
    // This doesn't really belong here but I have no idea where else to put it
    private static JButton configUrlFileChooserButton;

    private static TrayIcon trayIcon;
    private static MenuItem trayMenuMain;
    private static CheckboxMenuItem trayMenuAutorip;

    private static Image mainIcon;

    private static AbstractRipper ripper;

    public static ResourceBundle rb = Utils.getResourceBundle(null);

    // All the langs ripme has been translated into
    private static String[] supportedLanges = new String[] {
            "de_DE",
            "en_US",
            "es_ES",
            "fi_FI",
            "fr_CH",
            "in_ID",
            "kr_KR",
            "nl_NL",
            "pl_PL",
            "porrisavvo_FI",
            "pt_BR",
            "pt_PT",
            "ru_RU"};

    private void updateQueueLabel() {
        if (queueListModel.size() > 0) {
            optionQueue.setText(rb.getString("Queue") + " (" + queueListModel.size() + ")");
        } else {
            optionQueue.setText(rb.getString("Queue"));
        }
    }


    private static void addCheckboxListener(JCheckBox checkBox, String configString) {
        checkBox.addActionListener(arg0 -> {
            Utils.setConfigBoolean(configString, checkBox.isSelected());
            Utils.configureLogger();
        });

    }

    private static JCheckBox addNewCheckbox(String text, String configString, Boolean configBool) {
        JCheckBox checkbox = new JCheckBox(text, Utils.getConfigBoolean(configString, configBool));
        checkbox.setHorizontalAlignment(JCheckBox.RIGHT);
        checkbox.setHorizontalTextPosition(JCheckBox.LEFT);
        return checkbox;
    }


    public static void addUrlToQueue(String url) {
        queueListModel.addElement(url);
    }

    public MainWindow() {
        mainFrame = new JFrame("RipMe v" + UpdateUtils.getThisJarVersion());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new GridBagLayout());

        createUI(mainFrame.getContentPane());
        pack();

        loadHistory();
        setupHandlers();

        Thread shutdownThread = new Thread(this::shutdownCleanup);
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        if (Utils.getConfigBoolean("auto.update", true)) {
            upgradeProgram();
        }

        boolean autoripEnabled = Utils.getConfigBoolean("clipboard.autorip", false);
        ClipboardUtils.setClipboardAutoRip(autoripEnabled);
        trayMenuAutorip.setState(autoripEnabled);
    }

    private void upgradeProgram() {
        if (!configurationPanel.isVisible()) {
            optionConfiguration.doClick();
        }
        Runnable r = () -> UpdateUtils.updateProgramGUI(configUpdateLabel);
        new Thread(r).start();
    }

    public void run() {
        pack();
        restoreWindowPosition(mainFrame);
        mainFrame.setVisible(true);
    }

    private void shutdownCleanup() {
        Utils.setConfigBoolean("file.overwrite", configOverwriteCheckbox.isSelected());
        Utils.setConfigInteger("threads.size", Integer.parseInt(configThreadsText.getText()));
        Utils.setConfigInteger("download.retries", Integer.parseInt(configRetriesText.getText()));
        Utils.setConfigInteger("download.timeout", Integer.parseInt(configTimeoutText.getText()));
        Utils.setConfigBoolean("clipboard.autorip", ClipboardUtils.getClipboardAutoRip());
        Utils.setConfigBoolean("auto.update", configAutoupdateCheckbox.isSelected());
        Utils.setConfigString("log.level", configLogLevelCombobox.getSelectedItem().toString());
        Utils.setConfigBoolean("play.sound", configPlaySound.isSelected());
        Utils.setConfigBoolean("download.save_order", configSaveOrderCheckbox.isSelected());
        Utils.setConfigBoolean("download.show_popup", configShowPopup.isSelected());
        Utils.setConfigBoolean("log.save", configSaveLogs.isSelected());
        Utils.setConfigBoolean("urls_only.save", configSaveURLsOnly.isSelected());
        Utils.setConfigBoolean("album_titles.save", configSaveAlbumTitles.isSelected());
        Utils.setConfigBoolean("clipboard.autorip", configClipboardAutorip.isSelected());
        Utils.setConfigBoolean("descriptions.save", configSaveDescriptions.isSelected());
        Utils.setConfigBoolean("prefer.mp4", configPreferMp4.isSelected());
        Utils.setConfigBoolean("remember.url_history", configURLHistoryCheckbox.isSelected());
        saveWindowPosition(mainFrame);
        saveHistory();
        Utils.saveConfig();
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
        pack();
    }

    private void pack() {
        SwingUtilities.invokeLater(() -> {
            Dimension preferredSize = mainFrame.getPreferredSize();
            mainFrame.setMinimumSize(preferredSize);
            if (isCollapsed()) {
                mainFrame.setSize(preferredSize);
            }
        });
    }

    private boolean isCollapsed() {
        return (!logPanel.isVisible() &&
                !historyPanel.isVisible() &&
                !queuePanel.isVisible() &&
                !configurationPanel.isVisible()
        );
    }

    private void createUI(Container pane) {
        //If creating the tray icon fails, ignore it.
        try {
            setupTrayIcon();
        } catch (Exception e) { }

        EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1; gbc.ipadx = 2; gbc.gridx = 0;
        gbc.weighty = 0; gbc.ipady = 2; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            LOGGER.error("[!] Exception setting system theme:", e);
        }

        ripTextfield = new JTextField("", 20);
        ripTextfield.addMouseListener(new ContextMenuMouseListener());
        ImageIcon ripIcon = new ImageIcon(mainIcon);
        ripButton = new JButton("<html><font size=\"5\"><b>Rip</b></font></html>", ripIcon);
        stopButton = new JButton("<html><font size=\"5\"><b>Stop</b></font></html>");
        stopButton.setEnabled(false);
        try {
            Image stopIcon = ImageIO.read(getClass().getClassLoader().getResource("stop.png"));
            stopButton.setIcon(new ImageIcon(stopIcon));
        } catch (Exception ignored) { }
        JPanel ripPanel = new JPanel(new GridBagLayout());
        ripPanel.setBorder(emptyBorder);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.gridx = 0; ripPanel.add(new JLabel("URL:", JLabel.RIGHT), gbc);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 1; ripPanel.add(ripTextfield, gbc);
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.gridx = 2; ripPanel.add(ripButton, gbc);
        gbc.gridx = 3; ripPanel.add(stopButton, gbc);
        gbc.weightx = 1;

        statusLabel = new JLabel(rb.getString("inactive"));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        openButton = new JButton();
        openButton.setVisible(false);
        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(emptyBorder);

        gbc.gridx = 0; statusPanel.add(statusLabel, gbc);
        gbc.gridy = 1; statusPanel.add(openButton, gbc);
        gbc.gridy = 0;

        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBorder(emptyBorder);
        statusProgress = new JProgressBar(0, 100);
        progressPanel.add(statusProgress, gbc);

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(emptyBorder);
        optionLog = new JButton(rb.getString("Log"));
        optionHistory = new JButton(rb.getString("History"));
        optionQueue = new JButton(rb.getString("Queue"));
        optionConfiguration = new JButton(rb.getString("Configuration"));
        optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
        optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
        optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
        optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
        try {
            Image icon;
            icon = ImageIO.read(getClass().getClassLoader().getResource("comment.png"));
            optionLog.setIcon(new ImageIcon(icon));
            icon = ImageIO.read(getClass().getClassLoader().getResource("time.png"));
            optionHistory.setIcon(new ImageIcon(icon));
            icon = ImageIO.read(getClass().getClassLoader().getResource("list.png"));
            optionQueue.setIcon(new ImageIcon(icon));
            icon = ImageIO.read(getClass().getClassLoader().getResource("gear.png"));
            optionConfiguration.setIcon(new ImageIcon(icon));
        } catch (Exception e) { }
        gbc.gridx = 0; optionsPanel.add(optionLog, gbc);
        gbc.gridx = 1; optionsPanel.add(optionHistory, gbc);
        gbc.gridx = 2; optionsPanel.add(optionQueue, gbc);
        gbc.gridx = 3; optionsPanel.add(optionConfiguration, gbc);

        logPanel = new JPanel(new GridBagLayout());
        logPanel.setBorder(emptyBorder);
        logText = new JTextPane();
        logText.setEditable(false);
        JScrollPane logTextScroll = new JScrollPane(logText);
        logTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logPanel.setVisible(false);
        logPanel.setPreferredSize(new Dimension(300, 250));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        logPanel.add(logTextScroll, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        historyPanel = new JPanel(new GridBagLayout());
        historyPanel.setBorder(emptyBorder);
        historyPanel.setVisible(false);
        historyPanel.setPreferredSize(new Dimension(300, 250));
        historyTableModel = new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getColumnName(int col) {
                return HISTORY.getColumnName(col);
            }
            @Override
            public Class<?> getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            @Override
            public Object getValueAt(int row, int col) {
                return HISTORY.getValueAt(row, col);
            }
            @Override
            public int getRowCount() {
                return HISTORY.toList().size();
            }
            @Override
            public int getColumnCount() {
                return HISTORY.getColumnCount();
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return (col == 0 || col == 4);
            }
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == 4) {
                    HISTORY.get(row).selected = (Boolean) value;
                    historyTableModel.fireTableDataChanged();
                }
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.addMouseListener(new HistoryMenuMouseListener());
        historyTable.setAutoCreateRowSorter(true);
        for (int i = 0; i < historyTable.getColumnModel().getColumnCount(); i++) {
            int width = 130; // Default
            switch (i) {
            case 0: // URL
                width = 270;
                break;
            case 3:
                width = 40;
                break;
            case 4:
                width = 15;
                break;
            }
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        JScrollPane historyTableScrollPane = new JScrollPane(historyTable);
        historyButtonRemove = new JButton(rb.getString("remove"));
        historyButtonClear  = new JButton(rb.getString("clear"));
        historyButtonRerip  = new JButton(rb.getString("re-rip.checked"));
        gbc.gridx = 0;
        // History List Panel
        JPanel historyTablePanel = new JPanel(new GridBagLayout());
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        historyTablePanel.add(historyTableScrollPane, gbc);
        gbc.ipady = 180;
        gbc.gridy = 0;
        historyPanel.add(historyTablePanel, gbc);
        gbc.ipady = 0;
        JPanel historyButtonPanel = new JPanel(new GridBagLayout());
        historyButtonPanel.setPreferredSize(new Dimension(300, 10));
        historyButtonPanel.setBorder(emptyBorder);
        gbc.gridx = 0; historyButtonPanel.add(historyButtonRemove, gbc);
        gbc.gridx = 1; historyButtonPanel.add(historyButtonClear, gbc);
        gbc.gridx = 2; historyButtonPanel.add(historyButtonRerip, gbc);
        gbc.gridy = 1; gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        historyPanel.add(historyButtonPanel, gbc);

        queuePanel = new JPanel(new GridBagLayout());
        queuePanel.setBorder(emptyBorder);
        queuePanel.setVisible(false);
        queuePanel.setPreferredSize(new Dimension(300, 250));
        queueListModel  = new DefaultListModel();
        JList queueList = new JList(queueListModel);
        queueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        queueList.addMouseListener(new QueueMenuMouseListener());
        JScrollPane queueListScroll = new JScrollPane(queueList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        for (String item : Utils.getConfigList("queue")) {
            queueListModel.addElement(item);
        }
        updateQueueLabel();
        gbc.gridx = 0;
        JPanel queueListPanel = new JPanel(new GridBagLayout());
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        queueListPanel.add(queueListScroll, gbc);
        queuePanel.add(queueListPanel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.ipady = 0;

        configurationPanel = new JPanel(new GridBagLayout());
        configurationPanel.setBorder(emptyBorder);
        configurationPanel.setVisible(false);
        // TODO Configuration components
        configUpdateButton = new JButton(rb.getString("check.for.updates"));
        configUpdateLabel = new JLabel( rb.getString("current.version") + ": " + UpdateUtils.getThisJarVersion(), JLabel.RIGHT);
        configThreadsLabel = new JLabel(rb.getString("max.download.threads") + ":", JLabel.RIGHT);
        configTimeoutLabel = new JLabel(rb.getString("timeout.mill"), JLabel.RIGHT);
        configRetriesLabel = new JLabel(rb.getString("retry.download.count"), JLabel.RIGHT);
        configThreadsText = new JTextField(Integer.toString(Utils.getConfigInteger("threads.size", 3)));
        configTimeoutText = new JTextField(Integer.toString(Utils.getConfigInteger("download.timeout", 60000)));
        configRetriesText = new JTextField(Integer.toString(Utils.getConfigInteger("download.retries", 3)));
        configOverwriteCheckbox = addNewCheckbox(rb.getString("overwrite.existing.files"), "file.overwrite", false);
        configAutoupdateCheckbox = addNewCheckbox(rb.getString("auto.update"), "auto.update", true);
        configPlaySound = addNewCheckbox(rb.getString("sound.when.rip.completes"), "play.sound", false);
        configShowPopup = addNewCheckbox(rb.getString("notification.when.rip.starts"), "download.show_popup", false);
        configSaveOrderCheckbox = addNewCheckbox(rb.getString("preserve.order"), "download.save_order", true);
        configSaveLogs = addNewCheckbox(rb.getString("save.logs"), "log.save", false);
        configSaveURLsOnly = addNewCheckbox(rb.getString("save.urls.only"), "urls_only.save", false);
        configSaveAlbumTitles = addNewCheckbox(rb.getString("save.album.titles"), "album_titles.save", true);
        configClipboardAutorip = addNewCheckbox(rb.getString("autorip.from.clipboard"), "clipboard.autorip", false);
        configSaveDescriptions = addNewCheckbox(rb.getString("save.descriptions"), "descriptions.save", true);
        configPreferMp4 = addNewCheckbox(rb.getString("prefer.mp4.over.gif"),"prefer.mp4", false);
        configWindowPosition = addNewCheckbox(rb.getString("restore.window.position"), "window.position", true);
        configURLHistoryCheckbox = addNewCheckbox(rb.getString("remember.url.history"), "remember.url_history", true);
        configUrlFileChooserButton = new JButton(rb.getString("download.url.list"));

        configLogLevelCombobox = new JComboBox<>(new String[] {"Log level: Error", "Log level: Warn", "Log level: Info", "Log level: Debug"});
        configSelectLangComboBox = new JComboBox<>(supportedLanges);
        configLogLevelCombobox.setSelectedItem(Utils.getConfigString("log.level", "Log level: Debug"));
        setLogLevel(configLogLevelCombobox.getSelectedItem().toString());
        configSaveDirLabel = new JLabel();
        try {
            String workingDir = (Utils.shortenPath(Utils.getWorkingDirectory()));
            configSaveDirLabel.setText(workingDir);
            configSaveDirLabel.setForeground(Color.BLUE);
            configSaveDirLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } catch (Exception e) { }
        configSaveDirLabel.setToolTipText(configSaveDirLabel.getText());
        configSaveDirLabel.setHorizontalAlignment(JLabel.RIGHT);
        configSaveDirButton = new JButton(rb.getString("select.save.dir") + "...");

        addItemToConfigGridBagConstraints(gbc, 0, configUpdateLabel, configUpdateButton);
        addItemToConfigGridBagConstraints(gbc, 1, configAutoupdateCheckbox, configLogLevelCombobox);
        addItemToConfigGridBagConstraints(gbc, 2, configThreadsLabel, configThreadsText);
        addItemToConfigGridBagConstraints(gbc, 3, configTimeoutLabel, configTimeoutText);
        addItemToConfigGridBagConstraints(gbc, 4, configRetriesLabel, configRetriesText);
        addItemToConfigGridBagConstraints(gbc, 5, configOverwriteCheckbox, configSaveOrderCheckbox);
        addItemToConfigGridBagConstraints(gbc, 6, configPlaySound, configSaveLogs);
        addItemToConfigGridBagConstraints(gbc, 7, configShowPopup, configSaveURLsOnly);
        addItemToConfigGridBagConstraints(gbc, 8, configClipboardAutorip, configSaveAlbumTitles);
        addItemToConfigGridBagConstraints(gbc, 9, configSaveDescriptions, configPreferMp4);
        addItemToConfigGridBagConstraints(gbc, 10, configWindowPosition, configURLHistoryCheckbox);
        addItemToConfigGridBagConstraints(gbc, 11, configSelectLangComboBox, configUrlFileChooserButton);
        addItemToConfigGridBagConstraints(gbc, 12, configSaveDirLabel, configSaveDirButton);




        emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(0, 0));
        emptyPanel.setSize(0, 0);

        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridy = 0; pane.add(ripPanel, gbc);
        gbc.gridy = 1; pane.add(statusPanel, gbc);
        gbc.gridy = 2; pane.add(progressPanel, gbc);
        gbc.gridy = 3; pane.add(optionsPanel, gbc);
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 4; pane.add(logPanel, gbc);
        gbc.gridy = 5; pane.add(historyPanel, gbc);
        gbc.gridy = 5; pane.add(queuePanel, gbc);
        gbc.gridy = 5; pane.add(configurationPanel, gbc);
        gbc.gridy = 5; pane.add(emptyPanel, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JLabel thing1ToAdd, JButton thing2ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
                                gbc.gridx = 1; configurationPanel.add(thing2ToAdd, gbc);
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JLabel thing1ToAdd, JTextField thing2ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
        gbc.gridx = 1; configurationPanel.add(thing2ToAdd, gbc);
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JCheckBox thing1ToAdd, JCheckBox thing2ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
        gbc.gridx = 1; configurationPanel.add(thing2ToAdd, gbc);
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JCheckBox thing1ToAdd, JComboBox thing2ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
        gbc.gridx = 1; configurationPanel.add(thing2ToAdd, gbc);
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JComboBox thing1ToAdd, JButton thing2ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
        gbc.gridx = 1; configurationPanel.add(thing2ToAdd, gbc);
    }

    private void addItemToConfigGridBagConstraints(GridBagConstraints gbc, int gbcYValue, JComboBox thing1ToAdd ) {
        gbc.gridy = gbcYValue;  gbc.gridx = 0; configurationPanel.add(thing1ToAdd, gbc);
    }

    private void changeLocale() {
        statusLabel.setText(rb.getString("inactive"));
        configUpdateButton.setText(rb.getString("check.for.updates"));
        configUpdateLabel.setText(rb.getString("current.version") + ": " + UpdateUtils.getThisJarVersion());
        configThreadsLabel.setText(rb.getString("max.download.threads"));
        configTimeoutLabel.setText(rb.getString("timeout.mill"));
        configRetriesLabel.setText(rb.getString("retry.download.count"));
        configOverwriteCheckbox.setText(rb.getString("overwrite.existing.files"));
        configAutoupdateCheckbox.setText(rb.getString("auto.update"));
        configPlaySound.setText(rb.getString("sound.when.rip.completes"));
        configShowPopup.setText(rb.getString("notification.when.rip.starts"));
        configSaveOrderCheckbox.setText(rb.getString("preserve.order"));
        configSaveLogs.setText(rb.getString("save.logs"));
        configSaveURLsOnly.setText(rb.getString("save.urls.only"));
        configSaveAlbumTitles.setText(rb.getString("save.album.titles"));
        configClipboardAutorip.setText(rb.getString("autorip.from.clipboard"));
        configSaveDescriptions.setText(rb.getString("save.descriptions"));
        configUrlFileChooserButton.setText(rb.getString("download.url.list"));
        configSaveDirButton.setText(rb.getString("select.save.dir") + "...");
        configPreferMp4.setText(rb.getString("prefer.mp4.over.gif"));
        configWindowPosition.setText(rb.getString("restore.window.position"));
        configURLHistoryCheckbox.setText(rb.getString("remember.url.history"));
        optionLog.setText(rb.getString("Log"));
        optionHistory.setText(rb.getString("History"));
        optionQueue.setText(rb.getString("Queue"));
        optionConfiguration.setText(rb.getString("Configuration"));
    }

    private void setupHandlers() {
        ripButton.addActionListener(new RipButtonHandler());
        ripTextfield.addActionListener(new RipButtonHandler());
        ripTextfield.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            private void update() {
                try {
                    String urlText = ripTextfield.getText().trim();
                    if (urlText.equals("")) {
                        return;
                    }
                    if (!urlText.startsWith("http")) {
                        urlText = "http://" + urlText;
                    }
                    URL url = new URL(urlText);
                    AbstractRipper ripper = AbstractRipper.getRipper(url);
                    statusWithColor(ripper.getHost() + " album detected", Color.GREEN);
                } catch (Exception e) {
                    statusWithColor("Can't rip this URL: " + e.getMessage(), Color.RED);
                }
            }
        });
        stopButton.addActionListener(event -> {
            if (ripper != null) {
                ripper.stop();
                isRipping = false;
                stopButton.setEnabled(false);
                statusProgress.setValue(0);
                statusProgress.setVisible(false);
                pack();
                statusProgress.setValue(0);
                status("Ripping interrupted");
                appendLog("Ripper interrupted", Color.RED);
            }
        });
        optionLog.addActionListener(event -> {
            logPanel.setVisible(!logPanel.isVisible());
            emptyPanel.setVisible(!logPanel.isVisible());
            historyPanel.setVisible(false);
            queuePanel.setVisible(false);
            configurationPanel.setVisible(false);
            if (logPanel.isVisible()) {
                optionLog.setFont(optionLog.getFont().deriveFont(Font.BOLD));
            } else {
                optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            }
            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            pack();
        });
        optionHistory.addActionListener(event -> {
            logPanel.setVisible(false);
            historyPanel.setVisible(!historyPanel.isVisible());
            emptyPanel.setVisible(!historyPanel.isVisible());
            queuePanel.setVisible(false);
            configurationPanel.setVisible(false);
            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            if (historyPanel.isVisible()) {
                optionHistory.setFont(optionLog.getFont().deriveFont(Font.BOLD));
            } else {
                optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            }
            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            pack();
        });
        optionQueue.addActionListener(event -> {
            logPanel.setVisible(false);
            historyPanel.setVisible(false);
            queuePanel.setVisible(!queuePanel.isVisible());
            emptyPanel.setVisible(!queuePanel.isVisible());
            configurationPanel.setVisible(false);
            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            if (queuePanel.isVisible()) {
                optionQueue.setFont(optionLog.getFont().deriveFont(Font.BOLD));
            } else {
                optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            }
            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            pack();
        });
        optionConfiguration.addActionListener(event -> {
            logPanel.setVisible(false);
            historyPanel.setVisible(false);
            queuePanel.setVisible(false);
            configurationPanel.setVisible(!configurationPanel.isVisible());
            emptyPanel.setVisible(!configurationPanel.isVisible());
            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            if (configurationPanel.isVisible()) {
                optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.BOLD));
            } else {
                optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
            }
            pack();
        });
        historyButtonRemove.addActionListener(event -> {
            int[] indices = historyTable.getSelectedRows();
            for (int i = indices.length - 1; i >= 0; i--) {
                int modelIndex = historyTable.convertRowIndexToModel(indices[i]);
                HISTORY.remove(modelIndex);
            }
            try {
                historyTableModel.fireTableDataChanged();
            } catch (Exception e) { }
            saveHistory();
        });
        historyButtonClear.addActionListener(event -> {
            if (Utils.getConfigBoolean("history.warn_before_delete", true)) {

                JPanel checkChoise = new JPanel();
                checkChoise.setLayout(new FlowLayout());
                JButton yesButton = new JButton("YES");
                JButton noButton = new JButton("NO");
                yesButton.setPreferredSize(new Dimension(70, 30));
                noButton.setPreferredSize(new Dimension(70, 30));
                checkChoise.add(yesButton);
                checkChoise.add(noButton);
                JFrame.setDefaultLookAndFeelDecorated(true);
                JFrame frame = new JFrame("Are you sure?");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(checkChoise);
                frame.setSize(405, 70);
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                noButton.addActionListener(e -> {
                    frame.setVisible(false);
                });
                yesButton.addActionListener(ed -> {
                    frame.setVisible(false);
                    Utils.clearURLHistory();
                    HISTORY.clear();
                    try {
                        historyTableModel.fireTableDataChanged();
                    } catch (Exception e) {
                    }
                    saveHistory();
                });
            }
            else {
                Utils.clearURLHistory();
                HISTORY.clear();
                try {
                    historyTableModel.fireTableDataChanged();
                } catch (Exception e) {
                }
                saveHistory();
            }
        });

        // Re-rip all history
        historyButtonRerip.addActionListener(event -> {
            if (HISTORY.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "There are no history entries to re-rip. Rip some albums first",
                        "RipMe Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int added = 0;
            for (HistoryEntry entry : HISTORY.toList()) {
                if (entry.selected) {
                    added++;
                    queueListModel.addElement(entry.url);
                }
            }
            if (added == 0) {
                JOptionPane.showMessageDialog(null,
                        "No history entries have been 'Checked'\n" +
                                "Check an entry by clicking the checkbox to the right of the URL or Right-click a URL to check/uncheck all items",
                        "RipMe Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        configUpdateButton.addActionListener(arg0 -> {
            Thread t = new Thread(() -> UpdateUtils.updateProgramGUI(configUpdateLabel));
            t.start();
        });
        configLogLevelCombobox.addActionListener(arg0 -> {
            String level = ((JComboBox) arg0.getSource()).getSelectedItem().toString();
            setLogLevel(level);
        });
        configSelectLangComboBox.addActionListener(arg0 -> {
            String level = ((JComboBox) arg0.getSource()).getSelectedItem().toString();
            rb = Utils.getResourceBundle(level);
            changeLocale();
        });
        configSaveDirLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File file = new File(Utils.getWorkingDirectory().toString());
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(file);
                } catch (Exception e1) { }
            }
        });
        configSaveDirButton.addActionListener(arg0 -> {
            UIManager.put("FileChooser.useSystemExtensionHiding", false);
            JFileChooser jfc = new JFileChooser(Utils.getWorkingDirectory());
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = jfc.showDialog(null, "select directory");
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File chosenFile = jfc.getSelectedFile();
            String chosenPath = null;
            try {
                chosenPath = chosenFile.getCanonicalPath();
            } catch (Exception e) {
                LOGGER.error("Error while getting selected path: ", e);
                return;
            }
            configSaveDirLabel.setText(Utils.shortenPath(chosenPath));
            Utils.setConfigString("rips.directory", chosenPath);
        });
        configUrlFileChooserButton.addActionListener(arg0 -> {
            UIManager.put("FileChooser.useSystemExtensionHiding", false);
            JFileChooser jfc = new JFileChooser(Utils.getWorkingDirectory());
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = jfc.showDialog(null, "Open");
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File chosenFile = jfc.getSelectedFile();
            String chosenPath = null;
            try {
                chosenPath = chosenFile.getCanonicalPath();
            } catch (Exception e) {
                LOGGER.error("Error while getting selected path: ", e);
                return;
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(chosenPath));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    if (line.startsWith("http")) {
                        MainWindow.addUrlToQueue(line);
                    } else {
                        LOGGER.error("Skipping url " + line + " because it looks malformed (doesn't start with http)");
                    }
                }


                } catch(IOException e) {
                    LOGGER.error("Error reading file " + e.getMessage());
                }
            });
        addCheckboxListener(configSaveOrderCheckbox, "download.save_order");
        addCheckboxListener(configOverwriteCheckbox, "file.overwrite");
        addCheckboxListener(configSaveLogs, "log.save");
        addCheckboxListener(configSaveURLsOnly, "urls_only.save");
        addCheckboxListener(configURLHistoryCheckbox, "remember.url_history");
        addCheckboxListener(configSaveAlbumTitles, "album_titles.save");
        addCheckboxListener(configSaveDescriptions, "descriptions.save");
        addCheckboxListener(configPreferMp4, "prefer.mp4");
        addCheckboxListener(configWindowPosition, "window.position");
        
        configClipboardAutorip.addActionListener(arg0 -> {
            Utils.setConfigBoolean("clipboard.autorip", configClipboardAutorip.isSelected());
            ClipboardUtils.setClipboardAutoRip(configClipboardAutorip.isSelected());
            trayMenuAutorip.setState(configClipboardAutorip.isSelected());
            Utils.configureLogger();
        });

        queueListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent arg0) {
                updateQueueLabel();
                if (!isRipping) {
                    ripNextAlbum();
                }
            }
            @Override
            public void contentsChanged(ListDataEvent arg0) { }
            @Override
            public void intervalRemoved(ListDataEvent arg0) { }
        });
    }

    private void setLogLevel(String level) {
        Level newLevel = Level.ERROR;
        level = level.substring(level.lastIndexOf(' ') + 1);
        switch (level) {
            case "Debug":
                newLevel = Level.DEBUG;
                break;
            case "Info":
                newLevel = Level.INFO;
                break;
            case "Warn":
                newLevel = Level.WARN;
                break;
            case "Error":
                newLevel = Level.ERROR;
                break;
        }
        Logger.getRootLogger().setLevel(newLevel);
        LOGGER.setLevel(newLevel);
        ConsoleAppender ca = (ConsoleAppender)Logger.getRootLogger().getAppender("stdout");
        if (ca != null) {
            ca.setThreshold(newLevel);
        }
        FileAppender fa = (FileAppender)Logger.getRootLogger().getAppender("FILE");
        if (fa != null) {
            fa.setThreshold(newLevel);
        }
    }

    private void setupTrayIcon() {
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e)   { trayMenuMain.setLabel("Hide"); }
            @Override
            public void windowDeactivated(WindowEvent e) { trayMenuMain.setLabel("Show"); }
            @Override
            public void windowDeiconified(WindowEvent e) { trayMenuMain.setLabel("Hide"); }
            @Override
            public void windowIconified(WindowEvent e)   { trayMenuMain.setLabel("Show"); }
        });
        PopupMenu trayMenu = new PopupMenu();
        trayMenuMain = new MenuItem("Hide");
        trayMenuMain.addActionListener(arg0 -> toggleTrayClick());
        MenuItem trayMenuAbout = new MenuItem("About " + mainFrame.getTitle());
        trayMenuAbout.addActionListener(arg0 -> {
            StringBuilder about = new StringBuilder();
            about.append("<html><h1>")
                 .append(mainFrame.getTitle())
                 .append("</h1>");
            about.append("Download albums from various websites:");
            try {
                List<String> rippers = Utils.getListOfAlbumRippers();
                about.append("<ul>");
                for (String ripper : rippers) {
                    about.append("<li>");
                    ripper = ripper.substring(ripper.lastIndexOf('.') + 1);
                    if (ripper.contains("Ripper")) {
                        ripper = ripper.substring(0, ripper.indexOf("Ripper"));
                    }
                    about.append(ripper);
                    about.append("</li>");
                }
                about.append("</ul>");
            } catch (Exception e) { }
           about.append("<br>And download videos from video sites:");
            try {
                List<String> rippers = Utils.getListOfVideoRippers();
                about.append("<ul>");
                for (String ripper : rippers) {
                    about.append("<li>");
                    ripper = ripper.substring(ripper.lastIndexOf('.') + 1);
                    if (ripper.contains("Ripper")) {
                        ripper = ripper.substring(0, ripper.indexOf("Ripper"));
                    }
                    about.append(ripper);
                    about.append("</li>");
                }
                about.append("</ul>");
            } catch (Exception e) { }

            about.append("Do you want to visit the project homepage on Github?");
            about.append("</html>");
            int response = JOptionPane.showConfirmDialog(null,
                    about.toString(),
                    mainFrame.getTitle(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    new ImageIcon(mainIcon));
            if (response == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().browse(URI.create("http://github.com/ripmeapp/ripme"));
                } catch (IOException e) {
                    LOGGER.error("Exception while opening project home page", e);
                }
            }
        });
        MenuItem trayMenuExit = new MenuItem("Exit");
        trayMenuExit.addActionListener(arg0 -> System.exit(0));
        trayMenuAutorip = new CheckboxMenuItem("Clipboard Autorip");
        trayMenuAutorip.addItemListener(arg0 -> {
            ClipboardUtils.setClipboardAutoRip(trayMenuAutorip.getState());
            configClipboardAutorip.setSelected(trayMenuAutorip.getState());
        });
        trayMenu.add(trayMenuMain);
        trayMenu.add(trayMenuAbout);
        trayMenu.addSeparator();
        trayMenu.add(trayMenuAutorip);
        trayMenu.addSeparator();
        trayMenu.add(trayMenuExit);
        try {
            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
            trayIcon = new TrayIcon(mainIcon);
            trayIcon.setToolTip(mainFrame.getTitle());
            trayIcon.setImageAutoSize(true);
            trayIcon.setPopupMenu(trayMenu);
            SystemTray.getSystemTray().add(trayIcon);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleTrayClick();
                    if (mainFrame.getExtendedState() != JFrame.NORMAL) {
                        mainFrame.setExtendedState(JFrame.NORMAL);
                    }
                    mainFrame.setAlwaysOnTop(true);
                    mainFrame.setAlwaysOnTop(false);
                }
            });
        } catch (IOException | AWTException e) {
            //TODO implement proper stack trace handling this is really just intented as a placeholder until you implement proper error handling
            e.printStackTrace();
        }
    }

    private void toggleTrayClick() {
        if (mainFrame.getExtendedState() == JFrame.ICONIFIED
                || !mainFrame.isActive()
                || !mainFrame.isVisible()) {
            mainFrame.setVisible(true);
            mainFrame.setAlwaysOnTop(true);
            mainFrame.setAlwaysOnTop(false);
            trayMenuMain.setLabel("Hide");
        } else {
            mainFrame.setVisible(false);
            trayMenuMain.setLabel("Show");
        }
    }

    /**
     * Write a line to the Log section of the GUI
     *
     * @param text the string to log
     * @param color the color of the line
     */
    private void appendLog(final String text, final Color color) {
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, color);
        StyledDocument sd = logText.getStyledDocument();
        try {
            synchronized (this) {
                sd.insertString(sd.getLength(), text + "\n", sas);
            }
        } catch (BadLocationException e) { }

        logText.setCaretPosition(sd.getLength());
    }

    /**
     * Write a line to the GUI log and the CLI log
     *
     * @param line the string to log
     * @param color the color of the line for the GUI log
     */
    public void displayAndLogError(String line, Color color) {
        appendLog(line, color);
        LOGGER.error(line);
    }

    private void loadHistory() {
        File historyFile = new File(Utils.getConfigDir() + File.separator + "history.json");
        HISTORY.clear();
        if (historyFile.exists()) {
            try {
                LOGGER.info(rb.getString("loading.history.from") + " " + historyFile.getCanonicalPath());
                HISTORY.fromFile(historyFile.getCanonicalPath());
            } catch (IOException e) {
                LOGGER.error("Failed to load history from file " + historyFile, e);
                JOptionPane.showMessageDialog(null,
                        "RipMe failed to load the history file at " + historyFile.getAbsolutePath() + "\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Closing RipMe will automatically overwrite the contents of this file,\n" +
                        "so you may want to back the file up before closing RipMe!",
                        "RipMe - history load failure",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            LOGGER.info(rb.getString("loading.history.from.configuration"));
            HISTORY.fromList(Utils.getConfigList("download.history"));
            if (HISTORY.toList().isEmpty()) {
                // Loaded from config, still no entries.
                // Guess rip history based on rip folder
                String[] dirs = Utils.getWorkingDirectory().list((dir, file) -> new File(dir.getAbsolutePath() + File.separator + file).isDirectory());
                for (String dir : dirs) {
                    String url = RipUtils.urlFromDirectoryName(dir);
                    if (url != null) {
                        // We found one, add it to history
                        HistoryEntry entry = new HistoryEntry();
                        entry.url = url;
                        HISTORY.add(entry);
                    }
                }
            }
        }
    }

    private void saveHistory() {
        Path historyFile = Paths.get(Utils.getConfigDir() + File.separator + "history.json");
        try {
            if (!Files.exists(historyFile)) {
                Files.createDirectories(historyFile.getParent());
                Files.createFile(historyFile);
            }

            HISTORY.toFile(historyFile.toString());
            Utils.setConfigList("download.history", Collections.emptyList());
        } catch (IOException e) {
            LOGGER.error("Failed to save history to file " + historyFile, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void ripNextAlbum() {
        isRipping = true;
        // Save current state of queue to configuration.
        Utils.setConfigList("queue", (Enumeration<Object>) queueListModel.elements());

        if (queueListModel.isEmpty()) {
            // End of queue
            isRipping = false;
            return;
        }
        String nextAlbum = (String) queueListModel.remove(0);
        updateQueueLabel();
        Thread t = ripAlbum(nextAlbum);
        if (t == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                LOGGER.error(rb.getString("interrupted.while.waiting.to.rip.next.album"), ie);
            }
            ripNextAlbum();
        } else {
            t.start();
        }
    }

    private Thread ripAlbum(String urlString) {
        //shutdownCleanup();
        if (!logPanel.isVisible()) {
            optionLog.doClick();
        }
        urlString = urlString.trim();
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
            LOGGER.error("[!] Could not generate URL for '" + urlString + "'", e);
            error("Given URL is not valid, expecting http://website.com/page/...");
            return null;
        }
        stopButton.setEnabled(true);
        statusProgress.setValue(100);
        openButton.setVisible(false);
        statusLabel.setVisible(true);
        pack();
        boolean failed = false;
        try {
            ripper = AbstractRipper.getRipper(url);
            ripper.setup();
        } catch (Exception e) {
            failed = true;
            LOGGER.error("Could not find ripper for URL " + url, e);
            error(e.getMessage());
        }
        if (!failed) {
            try {
                mainFrame.setTitle("Ripping - RipMe v" + UpdateUtils.getThisJarVersion());
                status("Starting rip...");
                ripper.setObserver(this);
                Thread t = new Thread(ripper);
                if (configShowPopup.isSelected() &&
                        (!mainFrame.isVisible() || !mainFrame.isActive())) {
                    mainFrame.toFront();
                    mainFrame.setAlwaysOnTop(true);
                    trayIcon.displayMessage(mainFrame.getTitle(), "Started ripping " + ripper.getURL().toExternalForm(), MessageType.INFO);
                    mainFrame.setAlwaysOnTop(false);
                }
                return t;
            } catch (Exception e) {
                LOGGER.error("[!] Error while ripping: " + e.getMessage(), e);
                error("Unable to rip this URL: " + e.getMessage());
            }
        }
        stopButton.setEnabled(false);
        statusProgress.setValue(0);
        pack();
        return null;
    }

    private boolean canRip(String urlString) {
        try {
            String urlText = urlString.trim();
            if (urlText.equals("")) {
                return false;
            }
            if (!urlText.startsWith("http")) {
                urlText = "http://" + urlText;
            }
            URL url = new URL(urlText);
            // Ripper is needed here to throw/not throw an Exception
            AbstractRipper ripper = AbstractRipper.getRipper(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    class RipButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String url = ripTextfield.getText();
            if (!queueListModel.contains(url) && !url.equals("")) {
                // Check if we're ripping a range of urls
                if (url.contains("{")) {
                    // Make sure the user hasn't forgotten the closing }
                    if (url.contains("}")) {
                        String rangeToParse = url.substring(url.indexOf("{") + 1, url.indexOf("}"));
                        int rangeStart = Integer.parseInt(rangeToParse.split("-")[0]);
                        int rangeEnd = Integer.parseInt(rangeToParse.split("-")[1]);
                        for (int i = rangeStart; i < rangeEnd +1; i++) {
                            String realURL = url.replaceAll("\\{\\S*\\}", Integer.toString(i));
                            if (canRip(realURL)) {
                                queueListModel.add(queueListModel.size(), realURL);
                                ripTextfield.setText("");
                            } else {
                                displayAndLogError("Can't find ripper for " + realURL, Color.RED);
                            }
                        }
                    }
                } else {
                    queueListModel.add(queueListModel.size(), ripTextfield.getText());
                    ripTextfield.setText("");
                }
            } else {
                if (!isRipping) {
                    ripNextAlbum();
                }
            }
        }
    }

    private class StatusEvent implements Runnable {
        private final AbstractRipper ripper;
        private final RipStatusMessage msg;

        StatusEvent(AbstractRipper ripper, RipStatusMessage msg) {
            this.ripper = ripper;
            this.msg = msg;
        }

        public void run() {
            handleEvent(this);
        }
    }

    private synchronized void handleEvent(StatusEvent evt) {
        if (ripper.isStopped()) {
            return;
        }
        RipStatusMessage msg = evt.msg;

        int completedPercent = evt.ripper.getCompletionPercentage();
        statusProgress.setValue(completedPercent);
        statusProgress.setVisible(true);
        status( evt.ripper.getStatusText() );

        switch(msg.getStatus()) {
        case LOADING_RESOURCE:
        case DOWNLOAD_STARTED:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("Downloading " + msg.getObject(), Color.BLACK);
            }
            break;
        case DOWNLOAD_COMPLETE:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("Downloaded " + msg.getObject(), Color.GREEN);
            }
            break;
        case DOWNLOAD_COMPLETE_HISTORY:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("" + msg.getObject(), Color.GREEN);
            }
            break;

        case DOWNLOAD_ERRORED:
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                appendLog((String) msg.getObject(), Color.RED);
            }
            break;
        case DOWNLOAD_WARN:
            if (LOGGER.isEnabledFor(Level.WARN)) {
                appendLog((String) msg.getObject(), Color.ORANGE);
            }
            break;

        case RIP_ERRORED:
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                appendLog((String) msg.getObject(), Color.RED);
            }
            stopButton.setEnabled(false);
            statusProgress.setValue(0);
            statusProgress.setVisible(false);
            openButton.setVisible(false);
            pack();
            statusWithColor("Error: " + msg.getObject(), Color.RED);
            break;

        case RIP_COMPLETE:
            RipStatusComplete rsc = (RipStatusComplete) msg.getObject();
            String url = ripper.getURL().toExternalForm();
            if (HISTORY.containsURL(url)) {
                // TODO update "modifiedDate" of entry in HISTORY
                HistoryEntry entry = HISTORY.getEntryByURL(url);
                entry.count = rsc.count;
                entry.modifiedDate = new Date();
            } else {
                HistoryEntry entry = new HistoryEntry();
                entry.url = url;
                entry.dir = rsc.getDir();
                entry.count = rsc.count;
                try {
                    entry.title = ripper.getAlbumTitle(ripper.getURL());
                } catch (MalformedURLException e) { }
                HISTORY.add(entry);
                historyTableModel.fireTableDataChanged();
            }
            if (configPlaySound.isSelected()) {
                Utils.playSound("camera.wav");
            }
            saveHistory();
            stopButton.setEnabled(false);
            statusProgress.setValue(0);
            statusProgress.setVisible(false);
            openButton.setVisible(true);
            File f = rsc.dir;
            String prettyFile = Utils.shortenPath(f);
            openButton.setText("Open " + prettyFile);
            mainFrame.setTitle("RipMe v" + UpdateUtils.getThisJarVersion());
            try {
                Image folderIcon = ImageIO.read(getClass().getClassLoader().getResource("folder.png"));
                openButton.setIcon(new ImageIcon(folderIcon));
            } catch (Exception e) { }
            appendLog("Rip complete, saved to " + f.getAbsolutePath(), Color.GREEN);
            openButton.setActionCommand(f.toString());
            openButton.addActionListener(event -> {
                try {
                    Desktop.getDesktop().open(new File(event.getActionCommand()));
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
            pack();
            ripNextAlbum();
            break;
        case COMPLETED_BYTES:
            // Update completed bytes
            break;
        case TOTAL_BYTES:
            // Update total bytes
            break;
        }
    }

    public void update(AbstractRipper ripper, RipStatusMessage message) {
        StatusEvent event = new StatusEvent(ripper, message);
        SwingUtilities.invokeLater(event);
    }

    public static void ripAlbumStatic(String url) {
        ripTextfield.setText(url.trim());
        ripButton.doClick();
    }

    public static void enableWindowPositioning() {
        Utils.setConfigBoolean("window.position", true);
    }

    public static void disableWindowPositioning() {
        Utils.setConfigBoolean("window.position", false);
    }

    private static boolean hasWindowPositionBug() {
        String osName = System.getProperty("os.name");
        // Java on Windows has a bug where if we try to manually set the position of the Window,
        // javaw.exe will not close itself down when the application is closed.
        // Therefore, even if isWindowPositioningEnabled, if we are on Windows, we ignore it.
        return osName == null || osName.startsWith("Windows");
    }

    private static boolean isWindowPositioningEnabled() {
        boolean isEnabled = Utils.getConfigBoolean("window.position", true);
        return isEnabled && !hasWindowPositionBug();
    }

    private static void saveWindowPosition(Frame frame) {
        if (!isWindowPositioningEnabled()) {
            return;
        }

        Point point;
        try {
            point = frame.getLocationOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                point = frame.getLocation();
            } catch (Exception e2) {
                e2.printStackTrace();
                return;
            }
        }
        int x = (int)point.getX();
        int y = (int)point.getY();
        int w = frame.getWidth();
        int h = frame.getHeight();
        Utils.setConfigInteger("window.x", x);
        Utils.setConfigInteger("window.y", y);
        Utils.setConfigInteger("window.w", w);
        Utils.setConfigInteger("window.h", h);
        LOGGER.debug("Saved window position (x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ")");
    }

    private static void restoreWindowPosition(Frame frame) {
        if (!isWindowPositioningEnabled()) {
            mainFrame.setLocationRelativeTo(null); // default to middle of screen
            return;
        }

        try {
            int x = Utils.getConfigInteger("window.x", -1);
            int y = Utils.getConfigInteger("window.y", -1);
            int w = Utils.getConfigInteger("window.w", -1);
            int h = Utils.getConfigInteger("window.h", -1);
            if (x < 0 || y < 0 || w <= 0 || h <= 0) {
                LOGGER.debug("UNUSUAL: One or more of: x, y, w, or h was still less than 0 after reading config");
                mainFrame.setLocationRelativeTo(null); // default to middle of screen
                return;
            }
            frame.setBounds(x, y, w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
