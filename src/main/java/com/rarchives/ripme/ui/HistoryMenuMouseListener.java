package com.rarchives.ripme.ui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import com.rarchives.ripme.utils.Utils;

class HistoryMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private JTable tableComponent;
    private Point lastPoint;

    @SuppressWarnings("serial")
    public HistoryMenuMouseListener(History history) {
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
        popup.addSeparator();
        popup.add(new AbstractAction(Utils.getLocalizedString("history.open.folder")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String url = tableComponent.getValueAt(tableComponent.rowAtPoint(lastPoint), 0).toString();
                    File dir = new File(history.getEntryByURL(url).dir);
                    if (dir.exists())
                        java.awt.Desktop.getDesktop().open(dir);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JTable)) {
                return;
            }

            tableComponent = (JTable) e.getSource();
            tableComponent.requestFocus();

            lastPoint = e.getPoint();

            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }
            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}
