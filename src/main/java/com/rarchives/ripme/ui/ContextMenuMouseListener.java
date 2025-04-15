package com.rarchives.ripme.ui;

import com.rarchives.ripme.uiUtils.ContextActionProtections;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Listens for mouse events & displays a contextual pop-up menu.
 * Copied verbatim from http://stackoverflow.com/a/2793959
 * @author Bozhidar Batsov
 */
public class ContextMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();

    public String getDebugSavedString() {
        return debugSavedString;
    }

    private String debugSavedString;

    public Action getCutAction() {
        return cutAction;
    }

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;

    public Action getCopyAction() {
        return copyAction;
    }

    public Action getPasteAction() {
        return pasteAction;
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getSelectAllAction() {
        return selectAllAction;
    }

    private Action undoAction;
    private Action selectAllAction;

    public JTextComponent getTextComponent() {
        return textComponent;
    }

    private JTextComponent textComponent;

    public String getSavedString() {
        return savedString;
    }

    private String savedString = "";
    private Actions lastActionSelected;

    private enum Actions { UNDO, CUT, COPY, PASTE, SELECT_ALL }


    @SuppressWarnings("serial")
    public ContextMenuMouseListener(JTextField ripTextfield) {
        this.textComponent = ripTextfield;

        //Add protection for cntl+v

        generate_popup();
    }

    private void generate_popup() {
        undoAction = new AbstractAction("Undo") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                textComponent.setText("");
                textComponent.replaceSelection(savedString);
                debugSavedString = textComponent.getText();
                lastActionSelected = Actions.UNDO;
            }
        };

        popup.add(undoAction);
        popup.addSeparator();

        cutAction = new AbstractAction("Cut") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.CUT;
                savedString = textComponent.getText();
                debugSavedString = savedString;
                textComponent.cut();
            }
        };

        popup.add(cutAction);

        copyAction = new AbstractAction("Copy") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.COPY;
                debugSavedString = textComponent.getText();
                textComponent.copy();
            }
        };

        popup.add(copyAction);

        pasteAction = new AbstractAction("Paste") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.PASTE;
                savedString = textComponent.getText();
                debugSavedString = savedString;
                ContextActionProtections.pasteFromClipboard(textComponent);
            }
        };

        popup.add(pasteAction);
        popup.addSeparator();

        selectAllAction = new AbstractAction("Select All") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.SELECT_ALL;
                debugSavedString = textComponent.getText();
                textComponent.selectAll();
            }
        };

        popup.add(selectAllAction);
    }


    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            if(this.popup == null) {
                popup = new JPopupMenu();
                generate_popup();
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiersEx() == InputEvent.BUTTON3_DOWN_MASK) {
            if (!(e.getSource() instanceof JTextComponent)) {
                return;
            }

            textComponent = (JTextComponent) e.getSource();
            textComponent.requestFocus();

            boolean enabled = textComponent.isEnabled();
            boolean editable = textComponent.isEditable();
            boolean nonempty = !(textComponent.getText() == null || textComponent.getText().equals(""));
            boolean marked = textComponent.getSelectedText() != null;

            boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);

            undoAction.setEnabled(enabled && editable && (lastActionSelected == Actions.CUT || lastActionSelected == Actions.PASTE));
            cutAction.setEnabled(enabled && editable && marked);
            copyAction.setEnabled(enabled && marked);
            pasteAction.setEnabled(enabled && editable && pasteAvailable);
            selectAllAction.setEnabled(enabled && nonempty);

            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }

            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}