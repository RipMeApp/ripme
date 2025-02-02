package com.rarchives.ripme.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.rarchives.ripme.utils.Utils;

class HistoryMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private JTable tableComponent;

    @SuppressWarnings("serial")
    public HistoryMenuMouseListener() {
        Action checkAllAction = new AbstractAction(Utils.getLocalizedString("history.check.all")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row = 0; row < tableComponent.getRowCount(); row++) {
                    tableComponent.setValueAt(true, row, 4);
                }
            }
        };
        popup.add(checkAllAction);

        Action uncheckAllAction = new AbstractAction(Utils.getLocalizedString("history.check.none")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row = 0; row < tableComponent.getRowCount(); row++) {
                    tableComponent.setValueAt(false, row, 4);
                }
            }
        };
        popup.add(uncheckAllAction);

        popup.addSeparator();

        Action checkSelected = new AbstractAction(Utils.getLocalizedString("history.check.selected")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (int row : tableComponent.getSelectedRows()) {
                    tableComponent.setValueAt(true, row, 4);
                }
            }
        };
        popup.add(checkSelected);

        Action uncheckSelected = new AbstractAction(Utils.getLocalizedString("history.uncheck.selected")) {
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
    public void mousePressed(MouseEvent e) {
        checkPopupTrigger(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupTrigger(e);
    }

    private void checkPopupTrigger(MouseEvent e) {
        if (e.getModifiersEx() == InputEvent.BUTTON3_DOWN_MASK) {
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
