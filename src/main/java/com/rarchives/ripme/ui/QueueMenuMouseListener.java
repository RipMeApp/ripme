package com.rarchives.ripme.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.rarchives.ripme.utils.Utils;

class QueueMenuMouseListener extends MouseAdapter {
    private JPopupMenu popup = new JPopupMenu();
    private JList<Object> queueList;
    private DefaultListModel<Object> queueListModel;
    private Consumer<DefaultListModel<Object>> updateQueue;

    public QueueMenuMouseListener(Consumer<DefaultListModel<Object>> updateQueue) {
        this.updateQueue = updateQueue;
        updateUI();
    }

	@SuppressWarnings("serial")
    public void updateUI() {
        popup.removeAll();

        Action removeSelected = new AbstractAction(Utils.getLocalizedString("queue.remove.selected")) {
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

        Action clearQueue = new AbstractAction(Utils.getLocalizedString("queue.remove.all")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (JOptionPane.showConfirmDialog(null, Utils.getLocalizedString("queue.validation"), "RipMe",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    queueListModel.removeAllElements();
                    updateUI();
                }
            }
        };
        popup.add(clearQueue);

        updateQueue.accept(queueListModel);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkPopupTrigger(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupTrigger(e);
    }

    @SuppressWarnings("unchecked")
    private void checkPopupTrigger(MouseEvent e) {
        if (e.getModifiersEx() == InputEvent.BUTTON3_DOWN_MASK) {
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
