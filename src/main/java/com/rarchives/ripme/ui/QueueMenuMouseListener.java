package com.rarchives.ripme.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.rarchives.ripme.utils.Utils;

class QueueMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private JList<Object> queueList;
    private DefaultListModel<Object> queueListModel;
    private boolean mouseDragging = false;
    private int dragSourceIndex;

    public QueueMenuMouseListener() {
        updateUI();
    }

    public void updateUI() {
        popup.removeAll();

        Action removeSelected = new AbstractAction(Utils.getLocalizedString("queue.remove.selected")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (JOptionPane.showConfirmDialog(null, Utils.getLocalizedString("queue.remove.selected.validation"), "RipMe",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Object o = queueList.getSelectedValue();
                    while (o != null) {
                        queueListModel.removeElement(o);
                        o = queueList.getSelectedValue();
                    }
                    updateUI();
                }
            }
        };
        popup.add(removeSelected);

        Action clearQueue = new AbstractAction(Utils.getLocalizedString("queue.remove.all")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (JOptionPane.showConfirmDialog(null, Utils.getLocalizedString("queue.remove.all.validation"), "RipMe",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    queueListModel.removeAllElements();
                    updateUI();
                }
            }
        };
        popup.add(clearQueue);

    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkPopupTrigger(e);
        handleDragStart(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupTrigger(e);
        handleDragEnd(e);
    }

    @SuppressWarnings("unchecked")
    public void handleDragStart(MouseEvent e) {
        if (!(e.getSource() instanceof JList<?>)) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(e)) {
            queueList = (JList<Object>) e.getSource();
            queueListModel = (DefaultListModel<Object>) queueList.getModel();

            dragSourceIndex = queueList.getSelectedIndex();
            mouseDragging = true;
        }
    }

    public void handleDragEnd(MouseEvent e) {
        mouseDragging = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        if (!(e.getSource() instanceof JList<?>)) {
            return;
        }
        if (mouseDragging) {
            queueList = (JList<Object>) e.getSource();
            queueListModel = (DefaultListModel<Object>) queueList.getModel();
            int currentIndex = queueList.locationToIndex(e.getPoint());
            if (currentIndex != dragSourceIndex) {
                int dragTargetIndex = queueList.getSelectedIndex();
                dragTargetIndex = Math.max(0, dragTargetIndex);
                dragTargetIndex = Math.min(queueListModel.size() - 1, dragTargetIndex);
                Object dragElement = queueListModel.get(dragSourceIndex);
                queueListModel.remove(dragSourceIndex);
                queueListModel.add(dragTargetIndex, dragElement);
                dragSourceIndex = currentIndex;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkPopupTrigger(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (!(e.getSource() instanceof JList)) {
                return;
            }

            queueList = (JList<Object>) e.getSource();
            queueListModel = (DefaultListModel<Object>) queueList.getModel();
            queueList.requestFocus();

            int nx = e.getX();

            if (nx > 500) {
                nx = nx - popup.getSize().width;
            }
            popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
        }
    }
}
