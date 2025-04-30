package raven.combobox;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;


/**
 * 
 * 
 * Slight alteration of ComboboxMultiSelection
 * <p>
 * This class is a custom implementation of the ComboBoxMultiSelection class. I only changed the renderer to use my a custom color for the checkbox
 * <p>
 * 
 * @see {@link raven.combobox.ComboBoxMultiSelection}
 * @author Daniel
 * 
*/
public class CustomComboBoxMultiSelection<E> extends ComboBoxMultiSelection<E> {

    // Override the renderer to use the custom checkbox icon
    @Override
    public void setRenderer(ListCellRenderer<? super E> renderer) {
        super.setRenderer(new CustomComboBoxMultiCellRenderer());
    }
    
    private class CustomComboBoxMultiCellRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setIcon(new CustomCheckBoxIcon(getSelectedItems().contains(value)));
            return this;
        }
    }
    
    private class CustomCheckBoxIcon extends FlatCheckBoxIcon {
        private final boolean selected;
        private final Color FINLANDIA = new Color(0x5E6C5E);
        
        public CustomCheckBoxIcon(boolean selected) {
            this.selected = selected;
        }
        
        @Override
        protected boolean isSelected(Component c) {
            return selected;
        }
        
        protected Color getCheckmarkColor() {
            return FINLANDIA;
        }
    }
}