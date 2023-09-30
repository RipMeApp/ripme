package com.rarchives.ripme.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * Listens for mouse events & displays a contextual pop-up menu.
 * Copied verbatim from http://stackoverflow.com/a/2793959
 * @author Bozhidar Batsov
 */
public class ContextMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action undoAction;
    private Action selectAllAction;

    private JTextComponent textComponent;
    private String savedString = "";
    private Actions lastActionSelected;

    private enum Actions { UNDO, CUT, COPY, PASTE, SELECT_ALL }

    @SuppressWarnings("serial")
    public ContextMenuMouseListener() {
        undoAction = new AbstractAction("Undo") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                textComponent.setText("");
                textComponent.replaceSelection(savedString);

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
                textComponent.cut();
            }
        };

        popup.add(cutAction);

        copyAction = new AbstractAction("Copy") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.COPY;
                textComponent.copy();
            }
        };

        popup.add(copyAction);

        pasteAction = new AbstractAction("Paste") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.PASTE;
                savedString = textComponent.getText();
                textComponent.paste();
            }
        };

        popup.add(pasteAction);
        popup.addSeparator();

        selectAllAction = new AbstractAction("Select All") {

            @Override
            public void actionPerformed(ActionEvent ae) {
                lastActionSelected = Actions.SELECT_ALL;
                textComponent.selectAll();
            }
        };

        popup.add(selectAllAction);
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