
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

package atomicJ.gui.editors;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;

import atomicJ.gui.*;
import atomicJ.utilities.SerializationUtilities;



import static atomicJ.gui.PreferenceKeys.*;

public class DiscreteSeriesSubeditor extends SubPanel implements ActionListener, SeriesSubeditor, MarkerStyleReceiver
{		
    private static final long serialVersionUID = 1L;

    private static final Shape[] SHAPES = PlotStyleUtilities.getNonZeroAreaShapes();	

    private boolean visible;
    private boolean visibleInLegend;
    private int markerIndex;		
    private float markerSize;		
    private Paint markerFillPaint;

    private final Channel1DRendererDataImmutable initRendererData;

    private final Preferences pref;       
    private final String seriesName;
    private final StyleTag styleTag;

    private final DiscreteSeriesRenderer renderer;
    private final List<DiscreteSeriesRenderer> boundedRenderers;

    private final JCheckBox showCheckBox = new JCheckBox();
    private final JCheckBox boxVisibleInLegend = new JCheckBox();
    private ShapeAndSizeChooser shapeChooser;
    private final PaintSampleFlexible paintSample = new PaintSampleFlexible();		
    private final JLabel shapeLabel;		
    private final JButton selectPaintButton = new JButton("Select");		
    private final JButton selectShapeButton = new JButton("Select");

    public DiscreteSeriesSubeditor(DiscreteSeriesRenderer renderer, List<DiscreteSeriesRenderer> boundedRenderes) 
    {
        this.renderer = renderer;
        this.boundedRenderers = boundedRenderes;
        this.seriesName = renderer.getName();
        this.styleTag = renderer.getStyleKey();
        this.pref = renderer.getPreferences();

        this.initRendererData = renderer.getImmutableData();
        boolean initVisible = initRendererData.getBaseSeriesVisible();
        boolean initVisibleInLegend = renderer.getBaseSeriesVisibleInLegend();

        Paint initPaint = initRendererData.getBasePaint();


        setParametersToInitial();

        paintSample.setPaint(initPaint);

        showCheckBox.setSelected(initVisible);
        showCheckBox.setActionCommand("SHOW_COMMAND");
        showCheckBox.addActionListener(this);

        boxVisibleInLegend.setSelected(initVisibleInLegend);
        boxVisibleInLegend.setActionCommand("VISIBLE_IN_LEGEND");
        boxVisibleInLegend.addActionListener(this);

        shapeLabel = buildShapeLabel();

        selectPaintButton.setActionCommand("SELECT_PAINT_COMMAND");
        selectPaintButton.addActionListener(this);

        selectShapeButton.setActionCommand("SELECT_MARKER_COMMAND");
        selectShapeButton.addActionListener(this);

        addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(showCheckBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(new JLabel("Legend"), 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(boxVisibleInLegend, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(new JLabel("Point style"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(shapeLabel, 1, 1, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(selectShapeButton, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(new JLabel("Color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(paintSample, 1, 2, 3, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(selectPaintButton, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),seriesName)));		
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName) : BorderFactory.createEtchedBorder();
        setBorder(border);
    }

    private void setParametersToInitial()
    {
        this.visible = initRendererData.getBaseSeriesVisible();
        this.visibleInLegend = initRendererData.getBaseSeriesVisibleInLegend();
        this.markerIndex =  initRendererData.getBaseMarkerIndex();
        this.markerSize = initRendererData.getBaseMarkerSize();
        this.markerFillPaint = initRendererData.getBasePaint();       
    }

    @Override
    public void saveAsDefaults()
    {
        pref.putBoolean(SHOWN, visible);
        pref.putBoolean(VISIBLE_IN_LEGEND, visibleInLegend);

        pref.putInt(SHAPE_INDEX, markerIndex);
        pref.putFloat(SHAPE_SIZE, getMarkerSize());

        try 
        {
            SerializationUtilities.putSerializableObject(pref, PAINT, markerFillPaint);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }

    @Override
    public void resetToDefaults()
    {
        SeriesStyleSupplier defaultStyle = DefaultSeriesStyleSupplier.getSupplier();

        int defSize = defaultStyle.getDefaultMarkerSize(styleTag);
        int defIndex = defaultStyle.getDefaultMarkerIndex(styleTag);
        Paint defPaint = defaultStyle.getDefaultMarkerPaint(styleTag);

        this.visible = pref.getBoolean(SHOWN, true);
        this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true);
        this.markerIndex = pref.getInt(SHAPE_INDEX, defIndex);
        this.markerSize = pref.getFloat(SHAPE_SIZE, defSize);
        this.markerFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, defPaint);

        Channel1DRendererDataImmutable dataEdited = buildImmutableDataBasedOnEditor();
        renderer.setData(dataEdited);

        resetEditor();
    }

    @Override
    public void applyChangesToAll() 
    {
        Channel1DRendererDataImmutable dataEdited = buildImmutableDataBasedOnEditor();
        for(DiscreteSeriesRenderer r: boundedRenderers)
        {
            r.setData(dataEdited);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        Channel1DRendererDataImmutable dataEdited = buildImmutableDataBasedOnEditor();
        renderer.setData(dataEdited);
        resetEditor();	
    }

    @Override
    public Component getEditionComponent()
    {
        return this;
    }

    private void resetEditor()
    {
        showCheckBox.setSelected(visible);
        boxVisibleInLegend.setSelected(visibleInLegend);
        paintSample.setPaint(markerFillPaint);
        updateShapeLabel();
    }

    private Channel1DRendererDataImmutable buildImmutableDataBasedOnEditor()
    {
        Channel1DRendererDataMutable dataMutable = initRendererData.getMutableCopy();

        dataMutable.setBaseSeriesVisible(visible);
        dataMutable.setBaseSeriesVisibleInLegend(visibleInLegend);
        dataMutable.setBaseMarkerIndex(markerIndex);
        dataMutable.setBaseMarkerSize(markerSize);
        dataMutable.setBasePaint(markerFillPaint);

        Channel1DRendererDataImmutable dataImmutable = dataMutable.getImmutableVersion();

        return dataImmutable; 
    }

    @Override
    public int getMarkerIndex()
    {
        return markerIndex;
    }

    @Override
    public void setMarkerIndex(int markerIndex) 
    {
        this.markerIndex = markerIndex;	        	
        renderer.setBaseMarkerIndex(markerIndex);
        updateShapeLabel();
    }

    @Override
    public float getMarkerSize()
    {
        return markerSize;
    }

    @Override
    public void setMarkerSize(float markerSize) 
    {
        this.markerSize = markerSize;    	        	
        renderer.setBaseMarkerSize(markerSize);

        updateShapeLabel();
    }


    @Override
    public Paint getPaint() 
    {
        return this.markerFillPaint;
    }

    @Override
    public Paint getMarkerFillPaint()
    {
        return this.markerFillPaint;
    }

    @Override
    public boolean getDrawMarkerOutline()
    {
        return false;
    }

    @Override
    public Stroke getMarkerOutlineStroke()
    {
        return null;
    }

    @Override
    public Paint getMarkerOutlinePaint()
    {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals("SELECT_PAINT_COMMAND")) 
        {
            attemptPaintSelection();
        }
        else if (command.equals("SHOW_COMMAND"))
        {
            visible = showCheckBox.isSelected();
            renderer.setBaseSeriesVisible(visible);
        }
        else if (command.equals("VISIBLE_IN_LEGEND"))
        {
            visibleInLegend = boxVisibleInLegend.isSelected();
            renderer.setBaseSeriesVisibleInLegend(visibleInLegend);
        }
        else if (command.equals("SELECT_MARKER_COMMAND"))
        {
            attemptShapeSelection();
        }			
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedRenderers.size()>1;
    }

    public void attemptPaintSelection() 
    {
        Paint p = this.markerFillPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(this, "Color", defaultColor);
        if (c != null) 
        {
            this.markerFillPaint = c;
            this.paintSample.setPaint(markerFillPaint);
            updateShapeLabel();
            renderer.setBasePaint(markerFillPaint);
        }
    }

    public void attemptShapeSelection() 
    {
        if(shapeChooser == null)
        {
            shapeChooser = new ShapeAndSizeChooser(SwingUtilities.getWindowAncestor(this), this, PlotStyleUtilities.getNonZeroAreaShapes());
        }
        shapeChooser.setVisible(true);
    }

    private JLabel buildShapeLabel()
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);		
        Graphics2D g2 = img.createGraphics();
        g2.setColor((Color)markerFillPaint);
        g2.fill(SHAPES[markerIndex]);

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        JLabel shapeLabel = new JLabel(shapeString, new ImageIcon(img), SwingConstants.LEFT);
        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));

        return shapeLabel;
    }

    private void updateShapeLabel()
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);	

        Graphics2D g2 = img.createGraphics();
        g2.setColor((Color)markerFillPaint);
        g2.fill(SHAPES[markerIndex]);

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        shapeLabel.setText(shapeString);
        shapeLabel.setIcon(new ImageIcon(img));
        shapeLabel.repaint();
    }
}




