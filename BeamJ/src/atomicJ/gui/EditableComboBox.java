
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.DefaultFormatter;

import com.google.common.base.Objects;


public class EditableComboBox extends JComboBox<String>
{
    private static final long serialVersionUID = 1L;

    public EditableComboBox(String[] items)
    {
        super(items);
        setEditable(true);
        setEditor(new NameComboEditor());	

    }  

    //https://stackoverflow.com/questions/15928314/obtain-currently-highlighted-item-from-jcombobox-popup-not-selected-item
    private JList<?> getPopupList() {
        ComboPopup popup = (ComboPopup) getUI().getAccessibleChild(this, 0);
        return popup.getList();
    }

    public DefaultFormatter getEditorFormatter()
    {
        NameComboEditor editor = (NameComboEditor)getEditor();
        DefaultFormatter formatter = editor.getFormatter();

        return formatter;
    }

    private class NameComboEditor implements ComboBoxEditor 
    {
        private static final String REMOVE_ITEM_ACTION_MAP_KEY = "RemoveItem";

        private final List<ActionListener> actionListeners = new ArrayList<>();

        private final JFormattedTextField field = new JFormattedTextField(new DefaultFormatter());
        private static final String VALUE_CHANGED = "ValueChanged";

        public NameComboEditor() 
        {
            field.setBorder(null);

            KeyStroke deleteStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
            InputMap fieldInputMap = field.getInputMap();
            ActionMap fieldActionMap = field.getActionMap();

            Object deleteActionMapKeyForSelected = fieldInputMap.get(deleteStroke);
            Action deleteActionForSelected = fieldActionMap.get(deleteActionMapKeyForSelected);
            fieldInputMap.put(deleteStroke, REMOVE_ITEM_ACTION_MAP_KEY);

            fieldActionMap.put(REMOVE_ITEM_ACTION_MAP_KEY, new AbstractAction()
            {            
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    JList<?> popupList = getPopupList();
                    Object highlightedItem = popupList.getSelectedValue();
                    Object selectedItem = getSelectedItem();

                    if(highlightedItem != null && !Objects.equal(highlightedItem, selectedItem))
                    {
                        removeItem(highlightedItem);
                    }
                    else
                    {
                        deleteActionForSelected.actionPerformed(e);
                    }

                }
            });

            field.addPropertyChangeListener("value", new PropertyChangeListener() {               
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    Object valNew = evt.getNewValue();
                    if(valNew != null)
                    {
                        ActionEvent event = new ActionEvent(NameComboEditor.this, ActionEvent.ACTION_PERFORMED, VALUE_CHANGED);
                        firePropertyChangeEvent(event);
                    }
                }
            });

        }

        private void firePropertyChangeEvent(ActionEvent evt)
        {
            for(ActionListener l : actionListeners)
            {
                l.actionPerformed(evt);
            }
        }

        public DefaultFormatter getFormatter()
        {
            DefaultFormatter formatter = (DefaultFormatter)field.getFormatter();

            return formatter;
        }

        @Override
        public void addActionListener(ActionListener l) 
        {
            if(!actionListeners.contains(l))
            {
                actionListeners.add(l);
            }
            field.addActionListener(l);
        }

        @Override
        public Component getEditorComponent() 
        {
            return field;
        }

        @Override
        public String getItem() 
        {
            String text = field.getText();
            return text;
        }

        @Override
        public void removeActionListener(ActionListener l) 
        {
            actionListeners.remove(l);
            field.removeActionListener(l);
        }

        @Override
        public void selectAll() 
        {
            field.selectAll();
        }

        @Override
        public void setItem(Object newValue) 
        {
            String text = newValue.toString();
            field.setText(text);			
        }
    }
}

