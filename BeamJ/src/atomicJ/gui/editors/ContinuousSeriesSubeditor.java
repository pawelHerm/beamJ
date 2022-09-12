
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

public class ContinuousSeriesSubeditor extends SubPanel implements ActionListener, ItemListener, PaintReceiver, MarkerStyleReceiver, SeriesSubeditor 
{	
    private static final long serialVersionUID = 1L;

    private static final String SELECT_MARKER_PAINT_COMMAND = "SELECT_MARKER_PAINT_COMMAND";
    private static final String SELECT_MARKER_SHAPE_COMMAND = "SELECT_MARKER_SHAPE_COMMAND";
    private static final String EDIT_STROKE_COMMAND = "EDIT_STROKE_COMMAND";

    private static final Shape[] SHAPES = PlotStyleUtilities.getNonZeroAreaShapes();	

    private boolean seriesVisible;
    private boolean visibleInLegend;
    private boolean lineVisible;		
    private boolean markersVisible;		
    private int markerIndex;		
    private float markerSize;		
    private Stroke lineStroke;		
    private Paint markerFillPaint;
    private Paint strokePaint;

    private final Preferences pref;       
    private final String seriesName;
    private final StyleTag styleTag;
    private final ContinuousSeriesRenderer renderer;	
    private final List<ContinuousSeriesRenderer> boundedRenderers;

    private final JCheckBox boxSeriesVisible = new JCheckBox();
    private final JCheckBox boxVisibleInLegend = new JCheckBox();

    private final JCheckBox boxItemsJoined = new JCheckBox();
    private final JCheckBox boxItemMarked = new JCheckBox();
    private ShapeAndSizeChooser shapeChooser;
    private final PaintSampleFlexible markerPaintSample = new PaintSampleFlexible();		
    private final JLabel labelShape;
    private final JLabel labelStroke = new JLabel("Stroke");
    private final JLabel labelMarkerStyle = new JLabel("Marker style");
    private final JLabel labelMarkerPaint = new JLabel("Marker color");

    private final StraightStrokeSample lineStrokeSample = new StraightStrokeSample();
    private final JButton buttonSelectMarkerPaint = new JButton("Select");		
    private final JButton buttonEditStroke = new JButton("Edit");		
    private final JButton buttonSelectMarker = new JButton("Select");

    private final Channel1DRendererDataImmutable initRendererData;
    private StrokeChooser lineStrokeChooser;

    public ContinuousSeriesSubeditor(final ContinuousSeriesRenderer renderer, List<ContinuousSeriesRenderer> boundedRenderers) 
    {
        this.renderer = renderer;
        this.boundedRenderers = boundedRenderers;
        this.seriesName = renderer.getName();	
        this.styleTag = renderer.getStyleKey();
        this.pref = renderer.getPreferences();

        this.initRendererData = renderer.getImmutableData();
        Stroke initLineStroke = initRendererData.getBaseStroke();	
        Paint initStrokePaint = initRendererData.getBasePaint();

        setParametersToInitial();

        labelShape = buildShapeLabel();

        boxSeriesVisible.setSelected(seriesVisible);
        boxVisibleInLegend.setSelected(visibleInLegend);
        boxItemsJoined.setSelected(lineVisible);
        boxItemMarked.setSelected(markersVisible);

        lineStrokeSample.setStroke(initLineStroke);  		
        lineStrokeSample.setStrokePaint(initStrokePaint);
        markerPaintSample.setPaint(markerFillPaint);

        addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        addComponent(boxSeriesVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.25, 1);

        addComponent(new JLabel("Legend"), 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        addComponent(boxVisibleInLegend, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        addComponent(new JLabel("Markers"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        addComponent(boxItemMarked, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.25, 1);

        addComponent(new JLabel("Joined"), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        addComponent(boxItemsJoined, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        addComponent(labelStroke, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        addComponent(lineStrokeSample, 1, 2, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(buttonEditStroke, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        addComponent(labelMarkerStyle, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        addComponent(labelShape, 1, 3, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(buttonSelectMarker, 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        addComponent(labelMarkerPaint, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        addComponent(markerPaintSample, 1, 4, 3, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        addComponent(buttonSelectMarkerPaint, 4, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName));

        setEditorConsistentWithJoined();
        setEditorConsistentWithMarked();

        initActionListener();		
        initItemListener();
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName) : BorderFactory.createEtchedBorder();
        setBorder(border);
    }

    private void setParametersToInitial()
    {
        this.seriesVisible = initRendererData.getBaseSeriesVisible();
        this.visibleInLegend = initRendererData.getBaseSeriesVisibleInLegend();
        this.lineVisible = initRendererData.getBaseLinesVisible();		
        this.markersVisible = initRendererData.getBaseShapesVisible();	
        this.lineStroke = initRendererData.getBaseStroke();
        this.strokePaint = initRendererData.getBasePaint();
        this.markerIndex = initRendererData.getBaseMarkerIndex();	
        this.markerSize = initRendererData.getBaseMarkerSize();	
        this.markerFillPaint = initRendererData.getBaseFillPaint();		
    }

    private void initActionListener()
    {
        buttonSelectMarkerPaint.setActionCommand(SELECT_MARKER_PAINT_COMMAND);
        buttonSelectMarkerPaint.addActionListener(this);

        buttonSelectMarker.setActionCommand(SELECT_MARKER_SHAPE_COMMAND);
        buttonSelectMarker.addActionListener(this);

        buttonEditStroke.setActionCommand(EDIT_STROKE_COMMAND);
        buttonEditStroke.addActionListener(this);
    }	

    private void initItemListener()
    {
        boxItemMarked.addItemListener(this);
        boxItemsJoined.addItemListener(this);
        boxSeriesVisible.addItemListener(this);
        boxVisibleInLegend.addItemListener(this);
    }	

    private void setEditorConsistentWithMarked()
    {
        labelMarkerStyle.setEnabled(markersVisible);
        labelShape.setEnabled(markersVisible);
        labelMarkerPaint.setEnabled(markersVisible);
        buttonSelectMarker.setEnabled(markersVisible);
        buttonSelectMarkerPaint.setEnabled(markersVisible);
        markerPaintSample.setEnabled(markersVisible);
    }

    private void setEditorConsistentWithJoined()
    {
        labelStroke.setEnabled(lineVisible);
        lineStrokeSample.setEnabled(lineVisible);
        buttonEditStroke.setEnabled(lineVisible);
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

        boolean defJoined = defaultStyle.getDefaultJoiningLineVisible(styleTag);
        boolean defMarkers = defaultStyle.getDefaultMarkersVisible(styleTag);
        int defSize = defaultStyle.getDefaultMarkerSize(styleTag);
        int defIndex = defaultStyle.getDefaultMarkerIndex(styleTag);
        Stroke defThickness = defaultStyle.getDefaultJoiningLineStroke(styleTag);
        Paint defPaint = defaultStyle.getDefaultMarkerPaint(styleTag);

        this.seriesVisible = pref.getBoolean(SHOWN, true);
        this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true);
        this.lineVisible = pref.getBoolean(JOINED, defJoined);
        this.markersVisible = pref.getBoolean(MARKERS, defMarkers);
        this.markerIndex = pref.getInt(SHAPE_INDEX, defIndex);
        this.markerSize = pref.getFloat(SHAPE_SIZE, defSize);
        this.markerFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, defPaint);
        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SERIES_JOINING_LINE_PAINT, Color.black);
        this.lineStroke = SerializationUtilities.getStroke(pref, SERIES_JOINING_LINE_STROKE, defThickness);

        Channel1DRendererDataImmutable dataEdited = buildImmutableDataBasedOnEditor();
        renderer.setData(dataEdited);

        resetEditor();
    }

    @Override
    public void saveAsDefaults()
    {
        pref.putBoolean(SHOWN, seriesVisible);
        pref.putBoolean(VISIBLE_IN_LEGEND, visibleInLegend);
        pref.putBoolean(JOINED, lineVisible);
        pref.putBoolean(MARKERS, markersVisible);
        pref.putInt(SHAPE_INDEX, markerIndex);
        pref.putFloat(SHAPE_SIZE, getMarkerSize());
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PAINT, markerFillPaint);
            SerializationUtilities.putSerializableObject(pref, SERIES_JOINING_LINE_PAINT, strokePaint);
            SerializationUtilities.putStroke(pref, SERIES_JOINING_LINE_STROKE, lineStroke);
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
    public void applyChangesToAll() 
    {
        Channel1DRendererDataImmutable dataEdited = buildImmutableDataBasedOnEditor();
        for(ContinuousSeriesRenderer r: boundedRenderers)
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
        boxSeriesVisible.setSelected(seriesVisible);
        boxVisibleInLegend.setSelected(visibleInLegend);
        boxItemsJoined.setSelected(lineVisible);
        boxItemMarked.setSelected(markersVisible);
        lineStrokeSample.setStroke(lineStroke);
        lineStrokeSample.setStrokePaint(strokePaint);
        markerPaintSample.setPaint(markerFillPaint);
        updateShapeLabel();
    }

    private Channel1DRendererDataImmutable buildImmutableDataBasedOnEditor()
    {
        Channel1DRendererDataMutable dataMutable = initRendererData.getMutableCopy();
        dataMutable.setBaseSeriesVisible(seriesVisible);
        dataMutable.setBaseSeriesVisibleInLegend(visibleInLegend);
        dataMutable.setBaseLinesVisible(lineVisible);		
        dataMutable.setBaseShapesVisible(markersVisible);	
        dataMutable.setBaseStroke(lineStroke);	
        dataMutable.setBaseMarkerIndex(markerIndex);
        dataMutable.setBaseMarkerSize(markerSize);
        dataMutable.setBasePaint(strokePaint);
        dataMutable.setBaseFillPaint(markerFillPaint);		

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
    public void setMarkerSize(float markerSize) 
    {
        this.markerSize = markerSize;    	        	
        renderer.setBaseMarkerSize(markerSize);

        updateShapeLabel();
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals(SELECT_MARKER_PAINT_COMMAND)) 
        {
            attemptPaintSelection();
        }
        else if (command.equals(SELECT_MARKER_SHAPE_COMMAND))
        {
            attemptShapeSelection();
        }	
        else if(EDIT_STROKE_COMMAND.equals(command))
        {
            attemptStrokeEdition();
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
            markerPaintSample.setPaint(c);
            renderer.setBaseFillPaint(markerFillPaint);
            updateShapeLabel();
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

    private void attemptStrokeEdition()
    {
        if(lineStrokeChooser == null)
        {
            lineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)lineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    lineStroke = stroke;
                    lineStrokeSample.setStroke(stroke);
                    renderer.setBaseStroke(lineStroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return strokePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    strokePaint = paint;
                    lineStrokeSample.setStrokePaint(paint);
                    renderer.setBasePaint(strokePaint);
                }       	
            }
                    );

        }
        lineStrokeChooser.showDialog();
    }

    private JLabel buildShapeLabel()
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);		
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(getPaint());
        g2.fill(SHAPES[markerIndex]);
        g2.dispose();

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        JLabel shapeLabel = new JLabel(shapeString, new ImageIcon(img), SwingConstants.LEFT);
        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));

        return shapeLabel;
    }

    private void updateShapeLabel()
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);		
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(getPaint());
        g2.fill(SHAPES[markerIndex]);
        g2.dispose();

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        labelShape.setText(shapeString);
        labelShape.setIcon(new ImageIcon(img));
        labelShape.repaint();
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxSeriesVisible)
        {
            this.seriesVisible = selected;
            renderer.setBaseSeriesVisible(seriesVisible);
        }	
        else if(source == boxVisibleInLegend)
        {
            this.visibleInLegend = selected;
            renderer.setBaseSeriesVisibleInLegend(visibleInLegend);
        }
        else if(source == boxItemsJoined)
        {
            this.lineVisible = selected;
            setEditorConsistentWithJoined();
            renderer.setBaseLinesVisible(lineVisible);
        }
        else if(source == boxItemMarked)
        {
            this.markersVisible = selected;
            setEditorConsistentWithMarked();
            renderer.setBaseShapesVisible(markersVisible);
        }
    }

    @Override
    public void setPaint(Paint paint) {
        // TODO Auto-generated method stub

    }
}



