package com.rarchives.ripme.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class HistoryMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private Action checkAllAction,
                   uncheckAllAction,
                   checkSelected,
                   uncheckSelected;
    private JTable tableComponent;

    @SuppressWarnings("serial")
    public HistoryMenuMouseListener() {
        checkAllAction = new AbstractAction("Check All") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row = 0; row < tableComponent.getRowCount(); row++) {
                    tableComponent.setValueAt(true, row, 4);
                }
            }
        };
        popup.add(checkAllAction);

        uncheckAllAction = new AbstractAction("Check None") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row = 0; row < tableComponent.getRowCount(); row++) {
                    tableComponent.setValueAt(false, row, 4);
                }
            }
        };
        popup.add(uncheckAllAction);

        popup.addSeparator();

        checkSelected = new AbstractAction("Check Selected") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row : tableComponent.getSelectedRows()) {
                    tableComponent.setValueAt(true, row, 4);
                }
            }
        };
        popup.add(checkSelected);

        uncheckSelected = new AbstractAction("Uncheck Selected") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row : tableComponent.getSelectedRows()) {
                    tableComponent.setValueAt(false, row, 4);
                }
            }
        };
        popup.add(uncheckSelected);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JTable)) {
                return;
            }

            tableComponent = (JTable) e.getSource();
            tableComponent.requestFocus();

            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }
            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}
