
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
import java.awt.Font;
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
import atomicJ.gui.FontDisplayField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.RoamingLegend;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

import static atomicJ.gui.PreferenceKeys.*;

public class LegendStyleSubeditor extends SubPanel implements Subeditor, ActionListener, ItemListener, PaintReceiver
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_TEXT_FONT_COMMAND = "SELECT_TEXT_FONT_COMMAND";
    private static final String SELECT_TEXT_PAINT_COMMAND = "SELECT_TEXT_PAINT_COMMAND";
    private static final String SELECT_FRAME_STROKE_COMMAND = "SELECT_FRAME_STROKE_COMMAND";
    private static final String SELECT_BACKGROUND_PAINT_COMMAND = "SELECT_BACKGROUND_PAINT_COMMAND";

    private final Preferences pref;

    private final boolean initFrameVisible;
    private final Font initTextFont;
    private final Paint initTextPaint;
    private final Stroke initFrameStroke;
    private final Paint initFramePaint;
    private final Paint initBackgroundPaint;
    private final boolean initUseGradientPaint;

    private final boolean containsText;

    private boolean frameVisible;
    private Font textFont;
    private Paint textPaint;
    private Stroke frameStroke;
    private Paint framePaint;
    private Paint backgroundPaint;
    private boolean useGradientPaint;

    private final JLabel labelTextFont = new JLabel("Text font");
    private final JLabel labelTextPaint = new JLabel("Text color");

    private final JCheckBox boxFrameVisible = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final FontDisplayField fieldFont;
    private final PaintSample textPaintSample;
    private final PaintSample backgroundPaintSample;
    private final StraightStrokeSample frameStrokeSample = new StraightStrokeSample();

    private final JButton buttonSelectTextPaint = new JButton("Select");
    private final JButton buttonSelectTextFont = new JButton("Select");

    private SkewedGradientEditionDialog gradientDialog;

    private StrokeChooser frameStrokeChooser;
    private FontChooserDialog fontChooserDialog;

    private final RoamingLegend legend;
    private final List<RoamingLegend> boundedLegends;

    public LegendStyleSubeditor(RoamingLegend legend, List<RoamingLegend> boundedLegends)
    {
        this.boundedLegends = boundedLegends;
        this.legend = legend;
        this.pref = legend.getPreferences();

        this.initFrameVisible = legend.isFrameVisible();
        this.initTextFont = legend.getLegendItemFont();
        this.initTextPaint = legend.getLegendItemPaint();
        this.initFramePaint = legend.getFramePaint();
        this.initBackgroundPaint = legend.getBackgroundPaint();
        this.initUseGradientPaint = initBackgroundPaint instanceof GradientPaint;
        this.initFrameStroke = legend.getFrameStroke();

        setParametersToInitial();
        containsText = legend.containsText();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.boxFrameVisible.setSelected(initFrameVisible);
        this.boxUseGradient.setSelected(initUseGradientPaint);

        this.backgroundPaintSample = new PaintSample(initBackgroundPaint);

        this.frameStrokeSample.setStroke(initFrameStroke);
        this.frameStrokeSample.setStrokePaint(initFramePaint);

        this.fieldFont = new FontDisplayField(initTextFont);
        this.textPaintSample = new PaintSample(initTextPaint);

        if(!containsText)
        {
            fieldFont.setEnabled(false);
            textPaintSample.setEnabled(false);
            labelTextFont.setEnabled(false);
            labelTextPaint.setEnabled(false);
            buttonSelectTextFont.setEnabled(false);
            buttonSelectTextPaint.setEnabled(false);
            textPaintSample.setPaint(new Color(0f, 0f, 0f, 0f));
        }          

        addComponentsAndDoLayout();                  
        initItemListener();
    }

    private void initItemListener()
    {
        boxFrameVisible.addItemListener(this);
        boxUseGradient.addItemListener(this);
    }

    private void setParametersToInitial()
    {
        frameVisible = initFrameVisible;
        textFont = initTextFont;
        textPaint = initTextPaint;
        backgroundPaint = initBackgroundPaint;
        useGradientPaint = initUseGradientPaint;
        frameStroke = initFrameStroke;
        framePaint = initFramePaint;
    }

    private void addComponentsAndDoLayout()
    {
        setLayout(new BorderLayout());
        SubPanel stylePanel = new SubPanel();    

        JButton buttonSelectFrameStroke = new JButton("Select");
        buttonSelectFrameStroke.setActionCommand(SELECT_FRAME_STROKE_COMMAND);
        buttonSelectFrameStroke.addActionListener(this);

        JButton buttonSelectBackgroundPaint = new JButton("Select");
        buttonSelectBackgroundPaint.setActionCommand(SELECT_BACKGROUND_PAINT_COMMAND);
        buttonSelectBackgroundPaint.addActionListener(this);

        buttonSelectTextPaint.setActionCommand(SELECT_TEXT_PAINT_COMMAND);
        buttonSelectTextPaint.addActionListener(this);

        buttonSelectTextFont.setActionCommand(SELECT_TEXT_FONT_COMMAND);
        buttonSelectTextFont.addActionListener(this);

        stylePanel.addComponent(new JLabel("Use gradient"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(boxUseGradient, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

        stylePanel.addComponent(new JLabel("Background color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(backgroundPaintSample, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectBackgroundPaint, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);           

        SubPanel frameStrokePanel = new SubPanel();

        frameStrokePanel.addComponent(boxFrameVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        frameStrokePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        frameStrokePanel.addComponent(frameStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);             

        stylePanel.addComponent(new JLabel("Show frame"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(frameStrokePanel, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectFrameStroke, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        if(containsText)
        {
            stylePanel.addComponent(labelTextFont, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
            stylePanel.addComponent(fieldFont, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
            stylePanel.addComponent(buttonSelectTextFont, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

            stylePanel.addComponent(labelTextPaint, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
            stylePanel.addComponent(textPaintSample, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
            stylePanel.addComponent(buttonSelectTextPaint, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     
        }

        add(stylePanel, BorderLayout.NORTH);   	
        setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    @Override
    public void applyChangesToAll() 
    {
        for(RoamingLegend leg: boundedLegends)
        {
            resetLegend(leg);
        }		
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetLegend(legend);	
        resetEditor();
    }

    private void resetLegend(RoamingLegend legend)
    {
        legend.setFrameVisible(frameVisible);
        legend.setFramePaint(framePaint);
        legend.setFrameStroke(frameStroke);
        legend.setBackgroundPaint(backgroundPaint);
        legend.setLegendItemFont(textFont);
        legend.setLegendItemPaint(textPaint);
    }

    private void resetEditor()
    {
        frameStrokeSample.setStroke(frameStroke);
        frameStrokeSample.setStrokePaint(framePaint);
        backgroundPaintSample.setPaint(backgroundPaint);

        boxFrameVisible.setSelected(frameVisible);
        boxUseGradient.setSelected(useGradientPaint);

        if(containsText)
        {
            fieldFont.setDisplayFont(textFont);
            textPaintSample.setPaint(textPaint);
        }
    }

    @Override
    public void resetToDefaults() 
    {
        ChartStyleSupplier supplier = legend.getSupplier();
        String key = legend.getKey();
        boolean defaultFrameVisible = supplier.getDefaultLegendFrameVisible(key);

        frameVisible = pref.getBoolean(LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        textFont = (Font)SerializationUtilities.getSerializableObject(pref, LEGEND_ITEM_FONT, new Font("Dialog", Font.PLAIN, 14));
        textPaint = (Paint)SerializationUtilities.getSerializableObject(pref, LEGEND_ITEM_PAINT, Color.black);
        framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_FRAME_PAINT, Color.black);
        frameStroke = SerializationUtilities.getStroke(pref, LEGEND_FRAME_STROKE, new BasicStroke(1.f));
        backgroundPaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_BACKGROUND_PAINT, new Color(255,255,255,0));
        useGradientPaint = backgroundPaint instanceof GradientPaint;

        resetLegend(legend);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(LEGEND_FRAME_VISIBLE, frameVisible);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, LEGEND_ITEM_FONT, textFont);
            SerializationUtilities.putSerializableObject(pref, LEGEND_ITEM_PAINT, textPaint);
            SerializationUtilities.putSerializableObject(pref, LEGEND_FRAME_PAINT, framePaint);
            SerializationUtilities.putSerializableObject(pref, LEGEND_BACKGROUND_PAINT, backgroundPaint);

            SerializationUtilities.putStroke(pref, LEGEND_FRAME_STROKE, frameStroke);
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
        return boundedLegends.size()>1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(command.equals(SELECT_TEXT_FONT_COMMAND))
        {
            attemptModifyTextFont();
        }
        else if(command.equals(SELECT_TEXT_PAINT_COMMAND))
        {
            attemptModifyTextPaint();
        }
        else if(command.equals(SELECT_FRAME_STROKE_COMMAND))
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
                    legend.setFrameStroke(frameStroke);
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
                    legend.setFramePaint(framePaint);
                }       	
            }
                    );	            	    	
        }
        frameStrokeChooser.showDialog();
    }

    private void attemptModifyTextPaint() 
    {
        Color defaultColor = (textPaint instanceof Color ? (Color) textPaint : Color.black);
        Color c = JColorChooser.showDialog(this, "Legend text color", defaultColor);
        if (c != null) 
        {
            textPaint = c;
            textPaintSample.setPaint(textPaint);
            legend.setLegendItemPaint(textPaint);
        }
    }

    private void attemptModifyTextFont() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(this), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                textFont = newFont;
                fieldFont.setDisplayFont(textFont);
                legend.setLegendItemFont(textFont);
                revalidate();	
            }

            @Override
            public Font getFont()
            {
                return textFont;
            }
        });
    }

    private void attemptModifyBackgroundPaint() 
    {

        if(useGradientPaint)
        {
            if(gradientDialog == null)
            {
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(this));
            }
            gradientDialog.showDialog(this);
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(LegendStyleSubeditor.this, "Legend background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                backgroundPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                legend.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxUseGradient)
        {
            this.useGradientPaint = selected;
        }
        else if(source == boxFrameVisible)
        {
            this.frameVisible = selected;
            legend.setFrameVisible(frameVisible);
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
            legend.setBackgroundPaint(backgroundPaint);			
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
