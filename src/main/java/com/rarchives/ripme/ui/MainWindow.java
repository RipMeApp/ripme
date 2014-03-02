package com.rarchives.ripme.ui;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;

public class MainWindow implements Runnable {

    private static final Logger logger = Logger.getLogger(MainWindow.class);

    private static final String WINDOW_TITLE = "RipMe";

    private static JFrame mainFrame;
    private static JTextField ripTextfield;
    private static JButton ripButton;

    private static JLabel statusLabel;
    private static JButton openButton;
    private static JProgressBar statusProgress;

    private static JTextPane textLog;
    private static JScrollPane textLogScroll;

    public MainWindow() {
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false);
        mainFrame.setLayout(new GridBagLayout());

        createUI(mainFrame.getContentPane());
        setupHandlers();
    }
    
    public void run() {
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    public static void status(String text) {
        statusLabel.setText(text);
        mainFrame.pack();
    }

    private void createUI(Container pane) {
        EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 9; gbc.ipadx = 5; gbc.gridx = 0;
        gbc.weighty = 9; gbc.ipady = 5; gbc.gridy = 0;

        ripTextfield = new JTextField("", 20);
        ripButton    = new JButton("rip");
        JPanel ripPanel = new JPanel(new GridBagLayout());
        ripPanel.setBorder(emptyBorder);

        ripPanel.add(new JLabel("URL:"), gbc);
        gbc.gridx = 1;
        ripPanel.add(ripTextfield, gbc);
        gbc.gridx = 2;
        ripPanel.add(ripButton, gbc);

        statusLabel  = new JLabel("Inactive");
        openButton = new JButton();
        openButton.setVisible(false);
        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(emptyBorder);
        
        gbc.gridx = 0; gbc.gridy = 1;
        statusPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        statusPanel.add(openButton, gbc);

        JPanel progressPanel = new JPanel(new GridBagLayout());
        progressPanel.setBorder(emptyBorder);
        statusProgress = new JProgressBar(0,  100);
        progressPanel.add(statusProgress, gbc);
        
        JPanel logPanel = new JPanel(new GridBagLayout());
        logPanel.setBorder(emptyBorder);
        textLog = new JTextPane();
        textLogScroll = new JScrollPane(textLog);
        logPanel.add(textLogScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        pane.add(ripPanel, gbc);
        gbc.gridy = 1;
        pane.add(statusPanel, gbc);
        gbc.gridy = 2;
        pane.add(progressPanel, gbc);
        gbc.gridy = 3;
        pane.add(logPanel, gbc);
    }
    
    private void setupHandlers() {
        ripButton.addActionListener(new RipButtonHandler());
    }

    class RipButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            openButton.setVisible(false);
            statusLabel.setVisible(true);
            mainFrame.pack();
            try {
                URL url = new URL(ripTextfield.getText());
                AbstractRipper ripper = AbstractRipper.getRipper(url);
                ripper.setObserver(new RipStatusHandler());
                Thread t = new Thread(ripper);
                t.start();
            } catch (Exception e) {
                status("Error: " + e.getMessage());
                return;
            }
        }
    }

    class RipStatusHandler implements Observer {
        public void update(Observable observable, Object object) {
            RipStatusMessage msg = (RipStatusMessage) object;
            System.err.println("Observer update, object: " + object.toString());
            switch(msg.getStatus()) {
            case LOADING_RESOURCE:
            case DOWNLOAD_STARTED:
            case DOWNLOAD_COMPLETE:
            case DOWNLOAD_ERRORED:
                //status((String) msg.getObject());
                int completedPercent = ((AbstractRipper) observable).getCompletionPercentage();
                statusProgress.setValue(completedPercent);
                status( ((AbstractRipper)observable).getStatusText() );
                break;

            case RIP_COMPLETE:
                statusProgress.setValue(100);
                statusLabel.setVisible(false);
                openButton.setVisible(true);
                File f = (File) msg.getObject();
                openButton.setText("Open " + Utils.removeCWD(f));
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
    }
}