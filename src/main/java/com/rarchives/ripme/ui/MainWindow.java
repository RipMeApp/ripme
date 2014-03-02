package com.rarchives.ripme.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;


public class MainWindow implements Runnable {

    private static final Logger logger = Logger.getLogger(MainWindow.class);

    private static final String WINDOW_TITLE = "RipMe";

    private static JFrame mainFrame;
    private static JPanel ripPanel;
    private static JTextField ripTextfield;
    private static JButton ripButton;

    private static JPanel statusPanel;
    private static JLabel statusLabel;
    private static JButton statusButton;

    public MainWindow() {
        createUI();
        setupHandlers();
    }
    
    private void createUI() {
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ripPanel     = new JPanel();
        ripTextfield = new JTextField("", 20);
        ripButton    = new JButton("rip");
        ripPanel.add(ripTextfield, BorderLayout.WEST);
        ripPanel.add(ripButton, BorderLayout.EAST);
        mainFrame.getContentPane().add(ripPanel, BorderLayout.NORTH);

        statusPanel  = new JPanel();
        statusLabel  = new JLabel("inactive", SwingConstants.LEADING);
        statusButton = new JButton("open dir");
        statusButton.setVisible(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(statusButton, BorderLayout.EAST);
        mainFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupHandlers() {
        ripButton.addActionListener(new RipButtonHandler());
    }

    public void run() {
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    public static void status(String text) {
        statusLabel.setText(text);
    }

    class RipButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            statusButton.setVisible(false);
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
                status((String) msg.getObject());
                break;
            case RIP_COMPLETE:
                File f = (File) msg.getObject();
                statusButton.setActionCommand(f.toString());
                statusButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        try {
                            Desktop.getDesktop().open(new File(event.getActionCommand()));
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                });
                statusButton.setVisible(true);
                status("Finished: " + Utils.removeCWD(f));
            }
        }
    }
}