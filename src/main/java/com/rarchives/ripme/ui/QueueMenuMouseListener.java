package com.rarchives.ripme.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import com.rarchives.ripme.utils.Utils;

public class QueueMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private Action removeSelected,
                   clearQueue;
    private JList queueList;
    private DefaultListModel queueListModel;

    @SuppressWarnings("serial")
    public QueueMenuMouseListener() {

        removeSelected = new AbstractAction("Remove Selected") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Object o = queueList.getSelectedValue();
                while (o != null) {
                    queueListModel.removeElement(o);
                    o = queueList.getSelectedValue();
                }
                updateUI();
            }
        };
        popup.add(removeSelected);

        clearQueue = new AbstractAction("Remove All") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                queueListModel.removeAllElements();
                updateUI();
            }
        };
        popup.add(clearQueue);

    }

    private void updateUI() {
        Utils.setConfigList("queue", (Enumeration<Object>) queueListModel.elements());

        if (queueListModel.size() == 0) {
            MainWindow.optionQueue.setText("Queue");
        }
        else {
            MainWindow.optionQueue.setText("Queue (" + queueListModel.size() + ")");
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JList)) {
                return;
            }

            queueList = (JList) e.getSource();
            queueListModel = (DefaultListModel) queueList.getModel();
            queueList.requestFocus();

            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }
            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}
