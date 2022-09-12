
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Paweł Hermanowicz
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

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import javax.swing.*;

import atomicJ.gui.ColumnVisibilityDialog;
import atomicJ.gui.units.UnitSelectionDialog;


public class FlexibleNumericalTableDialog extends StandardNumericalTableDialog
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_LOCATION_X = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().width/3);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final int HEIGHT =  pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
    private final int WIDTH =  pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
    private final int LOCATION_X =  pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
    private final int LOCATION_Y =  pref.getInt(WINDOW_LOCATION_Y, DEFAULT_HEIGHT);

    private final Action visibilityAction = new ChangeVisibilityAction();
    private final Action selectUnitsAction = new SelectUnitsAction();
    private final ColumnVisibilityDialog hideColumnsDialog;

    private final UnitSelectionDialog unitSelectionDialog;

    public FlexibleNumericalTableDialog(Window parent, StandardNumericalTable table, String title)
    {
        super(parent,Dialog.ModalityType.MODELESS, table, title);	

        this.hideColumnsDialog = ColumnVisibilityDialog.getDialog(this, table, table.getColumnShortNames());
        this.unitSelectionDialog = new UnitSelectionDialog(this, table.getUnitSelectionPanel());

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
        getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JMenuItem itemSelectUnits = new JMenuItem(selectUnitsAction);
        JMenuItem itemColumnVsibility = new JMenuItem(visibilityAction);

        addMenuItem(itemSelectUnits);
        addMenuItem(itemColumnVsibility);	

        initComponentListener();

        setSize(WIDTH,HEIGHT);
        setLocation(LOCATION_X,LOCATION_Y);
    }

    private void initComponentListener()
    {
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {               
                pref.putInt(WINDOW_HEIGHT, getHeight());
                pref.putInt(WINDOW_WIDTH, getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) getLocation().getX());         
                pref.putInt(WINDOW_LOCATION_Y, (int) getLocation().getY());
            }
        });
    }

    private class ChangeVisibilityAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ChangeVisibilityAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Column visibility");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            hideColumnsDialog.setVisible(true);
        }
    }

    private class SelectUnitsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectUnitsAction() {
            putValue(NAME, "Select units");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            unitSelectionDialog.setVisible(true);
        }
    }
}
