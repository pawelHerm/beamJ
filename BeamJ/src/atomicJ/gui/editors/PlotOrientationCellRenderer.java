
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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

package atomicJ.gui.editors;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.jfree.chart.plot.PlotOrientation;

public class PlotOrientationCellRenderer extends JLabel implements ListCellRenderer<PlotOrientation> {

    private static final long serialVersionUID = 1L;

    PlotOrientationCellRenderer() 
    {
        setOpaque(true);
        setPreferredSize(new Dimension(20,20));
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends PlotOrientation> list, PlotOrientation value, int index, boolean isSelected, boolean cellHasFocus)
    {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());};  
            if(value.equals(PlotOrientation.VERTICAL))
            {
                setText("Vertical");
            }
            else 
            {
                setText("Horizontal");
            }
            return this;
    }
}
