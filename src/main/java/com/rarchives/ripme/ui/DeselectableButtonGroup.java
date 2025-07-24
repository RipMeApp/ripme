package com.rarchives.ripme.ui;

import javax.swing.*;

public class DeselectableButtonGroup extends ButtonGroup {
    @Override
    public void setSelected(ButtonModel model, boolean selected) {
        if (selected) {
            super.setSelected(model, selected);
        } else {
            clearSelection();
        }
    }
}
