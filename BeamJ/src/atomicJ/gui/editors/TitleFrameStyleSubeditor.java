
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


import org.jfree.ui.PaintSample;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.ChartStyleSupplier;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.RoamingTextTitle;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

import static atomicJ.gui.PreferenceKeys.*;

public class TitleFrameStyleSubeditor extends SubPanel implements Subeditor, ActionListener, ItemListener, PaintReceiver
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_FRAME_STROKE_COMMAND = "SELECT_FRAME_STROKE_COMMAND";
    private static final String SELECT_BACKGROUND_PAINT_COMMAND = "SELECT_BACKGROUND_PAINT_COMMAND";

    private final Preferences pref;

    private final boolean initFrameVisible;
    private final Stroke initFrameStroke;
    private final Paint initFramePaint;
    private final Paint initBackgroundPaint;
    private final boolean initUseGradientBackgroundPaint;

    private boolean frameVisible;
    private Stroke frameStroke;
    private Paint framePaint;
    private Paint backgroundPaint;
    private boolean useBackgroundGradientPaint;

    private final JCheckBox boxFrameVisible = new JCheckBox();
    private final JCheckBox boxUseBackgroundGradient = new JCheckBox();
    private final PaintSample backgroundPaintSample;
    private final StraightStrokeSample frameStrokeSample = new StraightStrokeSample();

    private SkewedGradientEditionDialog backgroundGradientDialog;

    private StrokeChooser frameStrokeChooser;

    private final RoamingTextTitle title;
    private final List<? extends CustomizableXYBaseChart<?>> boundedCharts;

    public TitleFrameStyleSubeditor(RoamingTextTitle workingTitle, List<? extends
            CustomizableXYBaseChart<?>> boundedCharts)
    {
        this.boundedCharts = boundedCharts;
        this.title = workingTitle;
        this.pref = workingTitle.getPreferences();

        this.initFrameVisible = workingTitle.isFrameVisible();
        this.initFramePaint = workingTitle.getFramePaint();
        this.initBackgroundPaint = workingTitle.getBackgroundPaint() == null ? new Color(0,0,0,0) : workingTitle.getBackgroundPaint();

        this.initUseGradientBackgroundPaint = initBackgroundPaint instanceof GradientPaint;
        this.initFrameStroke = workingTitle.getFrameStroke();

        setParametersToInitial();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.boxFrameVisible.setSelected(initFrameVisible);
        this.boxUseBackgroundGradient.setSelected(initUseGradientBackgroundPaint);

        this.backgroundPaintSample = new PaintSample(initBackgroundPaint);

        this.frameStrokeSample.setStroke(initFrameStroke);
        this.frameStrokeSample.setStrokePaint(initFramePaint);



        addComponentsAndLayout();                  
        initItemListener();
    }

    private void initItemListener()
    {
        boxFrameVisible.addItemListener(this);
        boxUseBackgroundGradient.addItemListener(this);
    }

    private void setParametersToInitial()
    {
        frameVisible = initFrameVisible;
        backgroundPaint = initBackgroundPaint;
        useBackgroundGradientPaint = initUseGradientBackgroundPaint;
        frameStroke = initFrameStroke;
        framePaint = initFramePaint;
    }

    private void addComponentsAndLayout()
    {
        setLayout(new BorderLayout());
        SubPanel stylePanel = new SubPanel();    

        JButton buttonSelectFrameStroke = new JButton("Select");
        buttonSelectFrameStroke.setActionCommand(SELECT_FRAME_STROKE_COMMAND);
        buttonSelectFrameStroke.addActionListener(this);

        JButton buttonSelectBackgroundPaint = new JButton("Select");
        buttonSelectBackgroundPaint.setActionCommand(SELECT_BACKGROUND_PAINT_COMMAND);
        buttonSelectBackgroundPaint.addActionListener(this);

        stylePanel.addComponent(new JLabel("Show frame"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(boxFrameVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

        stylePanel.addComponent(new JLabel("Frame stroke"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(frameStrokeSample, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectFrameStroke, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        stylePanel.addComponent(new JLabel("Use gradient"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(boxUseBackgroundGradient, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

        stylePanel.addComponent(new JLabel("Background color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(backgroundPaintSample, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectBackgroundPaint, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);           

        add(stylePanel, BorderLayout.NORTH);   	
        setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableXYBaseChart<?> chart: boundedCharts)
        {
            RoamingTextTitle currentTitle = chart.getRoamingTitle();
            if(currentTitle != null)
            {
                resetTitle(currentTitle);
            }
        }		
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetTitle(title);	
        resetEditor();
    }

    private void resetTitle(RoamingTextTitle t)
    {
        t.setFrameVisible(frameVisible);
        t.setFramePaint(framePaint);
        t.setFrameStroke(frameStroke);
        t.setBackgroundPaint(backgroundPaint);		
    }

    private void resetEditor()
    {
        frameStrokeSample.setStroke(frameStroke);
        frameStrokeSample.setStrokePaint(framePaint);
        backgroundPaintSample.setPaint(backgroundPaint);

        boxFrameVisible.setSelected(frameVisible);
        boxUseBackgroundGradient.setSelected(useBackgroundGradientPaint);

    }

    @Override
    public void resetToDefaults() 
    {
        ChartStyleSupplier supplier = title.getSupplier();
        String key = title.getKey();
        boolean defaultFrameVisible = supplier.getDefaultTitleFrameVisible(key);

        frameVisible = pref.getBoolean(TITLE_FRAME_VISIBLE, defaultFrameVisible);
        framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, TITLE_FRAME_PAINT, Color.black);
        frameStroke = SerializationUtilities.getStroke(pref, TITLE_FRAME_STROKE, new BasicStroke(1.f));
        backgroundPaint = (Paint) SerializationUtilities.getSerializableObject(pref, TITLE_BACKGROUND_PAINT, new Color(255,255,255,0));
        useBackgroundGradientPaint = backgroundPaint instanceof GradientPaint;

        resetTitle(title);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(TITLE_FRAME_VISIBLE, frameVisible);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, TITLE_FRAME_PAINT, framePaint);
            SerializationUtilities.putSerializableObject(pref, TITLE_BACKGROUND_PAINT, backgroundPaint);

            SerializationUtilities.putStroke(pref, TITLE_FRAME_STROKE, frameStroke);
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
    public Component getEditionComponent()
    {
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedCharts.size()>1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(command.equals(SELECT_FRAME_STROKE_COMMAND))
        {
            attemptModifyFrameStroke();
        }
        else if(command.equals(SELECT_BACKGROUND_PAINT_COMMAND))
        {
            attemptModifyBackgroundPaint();
        }
    }   

    private void attemptModifyFrameStroke() 
    {		    
        if(frameStrokeChooser == null)
        {
            frameStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)frameStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    frameStroke = stroke;
                    frameStrokeSample.setStroke(stroke);
                    title.setFrameStroke(frameStroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return framePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    framePaint = paint;
                    frameStrokeSample.setStrokePaint(paint);
                    title.setFramePaint(framePaint);
                }       	
            }
                    );	            	    	
        }
        frameStrokeChooser.showDialog();
    }


    private void attemptModifyBackgroundPaint() 
    {    	
        if(useBackgroundGradientPaint)
        {
            if(backgroundGradientDialog == null)
            {
                backgroundGradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(this));
            }
            backgroundGradientDialog.showDialog(this);
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(TitleFrameStyleSubeditor.this, "Title background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                backgroundPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                title.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxUseBackgroundGradient)
        {
            this.useBackgroundGradientPaint = selected;
        }
        else if(source == boxFrameVisible)
        {
            this.frameVisible = selected;
            title.setFrameVisible(frameVisible);		
        }					
    }

    @Override
    public Paint getPaint() 
    {
        return backgroundPaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        if(paint != null)
        {			
            backgroundPaint = paint;
            backgroundPaintSample.setPaint(backgroundPaint);
            title.setBackgroundPaint(backgroundPaint);			
        }		
    }

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }
}
