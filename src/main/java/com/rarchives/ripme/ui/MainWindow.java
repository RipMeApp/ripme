package com.rarchives.ripme.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.rarchives.ripme.ripper.AbstractRipper;


public class MainWindow implements Runnable {

    private static final String WINDOW_TITLE = "RipMe";

    private static JFrame mainFrame;
    private static JTextField ripTextfield;
    private static JButton ripButton;

    private static JLabel ripStatus;

    public MainWindow() {
        createUI();
        setupHandlers();
    }
    
    private void createUI() {
        mainFrame = new JFrame(WINDOW_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ripTextfield = new JTextField("", 20);
        ripButton    = new JButton("rip");
        ripStatus    = new JLabel("inactive");
        mainFrame.getContentPane().add(ripTextfield, BorderLayout.WEST);
        mainFrame.getContentPane().add(ripButton, BorderLayout.EAST);
        mainFrame.getContentPane().add(ripStatus, BorderLayout.SOUTH);
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
        ripStatus.setText(text);
    }

    class RipButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                URL url = new URL(ripTextfield.getText());
                AbstractRipper ripper = AbstractRipper.getRipper(url);
                ripper.setObserver(new RipStatusHandler());
                ripper.rip();
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
                status("RIP COMPLETE: " + f);
            }
        }
    }
}