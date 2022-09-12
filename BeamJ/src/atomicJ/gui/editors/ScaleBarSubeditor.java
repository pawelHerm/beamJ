
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

import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_FONT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_OFFSET;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_PAINT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_POSITION;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_VISIBLE;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LENGTH_AUTOMATIC;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_POSITION_X;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_POSITION_Y;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_STROKE;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_STROKE_PAINT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_VISIBLE;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.FontField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.ScaleBar;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;



public class ScaleBarSubeditor extends JPanel implements ActionListener, ItemListener, ChangeListener, Subeditor
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_LABEL_FONT = "SELECT_LABEL_FONT";	
    private static final String EDIT_STROKE = "EDIT_STROKE";	
    private static final String SELECT_LABEL_PAINT = "SELECT_LABEL_PAINT";

    //initial settings

    private final boolean initVisible;
    private final double initLength;
    private final boolean initLengthAutomatic;
    private final float initLabelOffset;
    private final float initLabelLengthwisePosition;

    private final int initPositionX;
    private final int initPositionY;

    private final boolean initLabelVisible; 
    private final Font initLabelFont;
    private final Paint initLabelPaint;  
    private final Stroke initStroke;	
    private final Paint initStrokePaint;


    //current settings

    private boolean visible;

    private double length;
    private boolean lengthAutomatic;

    private int positionX;
    private int positionY;

    private float labelOffset;
    private float labelLengthwisePosition;
    private boolean labelVisible;         
    private Font labelFont;
    private Paint labelPaint;	
    private Stroke stroke;	
    private Paint strokePaint;	


    //GUI components

    private final StraightStrokeSample strokeSample = new StraightStrokeSample();	
    private final PaintSampleFlexible labelPaintSample = new PaintSampleFlexible();   
    private final FontField fieldLabelFont = new FontField();   

    private final JButton buttonSelectLabelFont = new JButton("Select");   
    private final JButton buttonEditStroke = new JButton("Edit"); 
    private final JButton buttonSelectLabelPaint = new JButton("Select");

    private final JCheckBox boxScalebarVisible = new JCheckBox(); 
    private final JCheckBox boxLabelVisible = new JCheckBox();


    private StrokeChooser chooserStrokeUnfinishedStandard;

    //gui for lenth
    private final JCheckBox boxAutomaticLength = new JCheckBox("Automatic");
    private final JSpinner spinnerLength;   

    // gui components for position

    private final JLabel labelPositionX = new JLabel("Position");
    private final JLabel labelLeft = new JLabel("Left");
    private final JLabel labelRight = new JLabel("Right");
    private final JLabel labelBottom = new JLabel("Bottom");
    private final JLabel labelTop = new JLabel("Top");

    private final JSlider sliderX;
    private final JSlider sliderY;

    private FontChooserDialog fontChooserDialog;

    private final Preferences pref;

    private final ScaleBar scaleBar;
    private final List<ScaleBar>boundedScaleBars;

    public ScaleBarSubeditor(ScaleBar scaleBar, List<ScaleBar> boundedScaleBars, CustomizableNumberAxis axis) 
    {   
        this.pref = scaleBar.getPreferences();

        this.scaleBar = scaleBar;
        this.boundedScaleBars = boundedScaleBars;

        this.initVisible = scaleBar.isVisible();
        this.initLengthAutomatic = scaleBar.isLengthAutomatic();
        this.initLength =  scaleBar.getLength();

        this.initLabelOffset = scaleBar.getLabelOffset();
        this.initLabelLengthwisePosition = scaleBar.getLabelLengthwisePosition();

        this.initPositionX = Math.max(0, (int) Math.round(scaleBar.getPositionX()*100));
        this.initPositionY = Math.max(0,(int) Math.round(scaleBar.getPositionY()*100));

        this.initLabelVisible = scaleBar.isLabelVisible(); 
        this.initLabelFont = scaleBar.getLabelFont();
        this.initLabelPaint = scaleBar.getLabelPaint();  
        this.initStrokePaint = scaleBar.getStrokePaint();	
        this.initStroke = scaleBar.getStroke();

        setParametersToInitial();

        //sets the editor
        this.fieldLabelFont.setDisplayFont(initLabelFont);   	
        this.labelPaintSample.setPaint(labelPaint);        
        this.strokeSample.setStroke(stroke);      
        this.strokeSample.setStrokePaint(strokePaint);       
        this.boxScalebarVisible.setSelected(visible);
        this.boxLabelVisible.setSelected(labelVisible);       
        this.boxAutomaticLength.setSelected(lengthAutomatic);

        this.sliderX = new JSlider(0, 100, initPositionX);
        sliderX.setMajorTickSpacing(5);

        this.sliderY = new JSlider(0, 100, initPositionY);
        sliderY.setMajorTickSpacing(5);

        double rangeLength = axis.getRange().getLength();
        int exp = (int)Math.rint(Math.floor(Math.log10(rangeLength))) - 1;
        double step = Math.pow(10, exp);

        this.spinnerLength = new JSpinner(new SpinnerNumberModel(initLength, 0, Integer.MAX_VALUE, step));        
        this.spinnerLength.setEnabled(!lengthAutomatic);

        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();  
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        add(mainPanel, BorderLayout.NORTH);

        initActionListener();
        initChangeListener();
        initItemListener();

    }

    private void setParametersToInitial()
    {
        this.visible = initVisible;

        this.lengthAutomatic = initLengthAutomatic;
        this.length = initLength;
        this.positionX = initPositionX;
        this.positionY = initPositionY;

        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;

        this.labelVisible = initLabelVisible;

        this.labelFont = initLabelFont;     
        this.labelPaint = initLabelPaint; 
        this.stroke = initStroke;    
        this.strokePaint = initStrokePaint;
    }

    private void initActionListener()
    {
        buttonSelectLabelFont.setActionCommand(SELECT_LABEL_FONT);       
        buttonEditStroke.setActionCommand(EDIT_STROKE);     
        buttonSelectLabelPaint.setActionCommand(SELECT_LABEL_PAINT);

        buttonSelectLabelFont.addActionListener(this);      
        buttonEditStroke.addActionListener(this);      
        buttonSelectLabelPaint.addActionListener(this);
    }

    private void initChangeListener()
    {
        spinnerLength.addChangeListener(this);
        sliderX.addChangeListener(this);
        sliderY.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxScalebarVisible.addItemListener(this);  	
        boxLabelVisible.addItemListener(this);
        boxAutomaticLength.addItemListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(SELECT_LABEL_FONT.equals(command))
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
                    labelFont = newFont;
                    fieldLabelFont.setDisplayFont(labelFont);
                    scaleBar.setLabelFont(labelFont);	
                }

                @Override
                public Font getFont()
                {
                    return labelFont;
                }
            });
        }

        else if(EDIT_STROKE.equals(command))
        {
            attemptStrokeSelection();
        }

        else if(SELECT_LABEL_PAINT.equals(command))
        {
            Paint paintLabelUnfinishedStandardNew = JColorChooser.showDialog(this,"Label color", (Color)labelPaint);	        
            if (paintLabelUnfinishedStandardNew != null) 
            {
                labelPaint = paintLabelUnfinishedStandardNew;
                labelPaintSample.setPaint(paintLabelUnfinishedStandardNew);
                scaleBar.setLabelPaint(labelPaint);		
            }
        }
    }

    private void attemptStrokeSelection() 
    {
        if(chooserStrokeUnfinishedStandard == null)
        {
            chooserStrokeUnfinishedStandard = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) stroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    ScaleBarSubeditor.this.stroke = stroke;
                    strokeSample.setStroke(stroke);
                    scaleBar.setStroke(stroke);
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    strokePaint = paint;
                    strokeSample.setStrokePaint(paint);
                    scaleBar.setStrokePaint(paint);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return strokePaint;
                }     	
            }
                    );
        }        
        chooserStrokeUnfinishedStandard.showDialog();
    }



    private void resetEditor()
    {
        this.sliderX.setValue(positionX);
        this.sliderY.setValue(positionY);
        this.fieldLabelFont.setDisplayFont(initLabelFont);   	
        this.labelPaintSample.setPaint(labelPaint);

        this.strokeSample.setStroke(stroke);      
        this.strokeSample.setStrokePaint(strokePaint);

        this.boxScalebarVisible.setSelected(visible);
        this.boxLabelVisible.setSelected(labelVisible);

        this.boxAutomaticLength.setSelected(lengthAutomatic);
        this.spinnerLength.setValue(length);

        this.spinnerLength.setEnabled(!lengthAutomatic);
    }

    @Override
    public void resetToDefaults() 
    {

        this.visible = pref.getBoolean(SCALEBAR_VISIBLE, false);
        this.lengthAutomatic = pref.getBoolean(SCALEBAR_LENGTH_AUTOMATIC, true);

        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_STROKE_PAINT, Color.black);
        this.stroke = SerializationUtilities.getStroke(pref, SCALEBAR_STROKE, new BasicStroke(1.f));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));

        this.positionX = (int) (Math.round(pref.getDouble(SCALEBAR_POSITION_X, 0.8)*100));
        this.positionY = (int) (Math.round(pref.getDouble(SCALEBAR_POSITION_Y, 0.1)*100));

        this.labelVisible = pref.getBoolean(SCALEBAR_LABEL_VISIBLE, true);
        this.labelOffset = pref.getFloat(SCALEBAR_LABEL_OFFSET, 1.f);

        resetScaleBar(scaleBar);
        resetEditor();
    }

    private void resetScaleBar(ScaleBar s)
    {
        s.setVisible(visible, false);

        s.setLength(length, false);
        s.setLengthAutomatic(lengthAutomatic, false);

        s.setPositionX(positionX/100., false);
        s.setPositionY(positionY/100., false);

        s.setLabelOffset(labelOffset,false);
        s.setLabelLengthwisePosition(labelLengthwisePosition, false);
        s.setLabelVisible(labelVisible, false);         
        s.setLabelFont(labelFont, false);
        s.setLabelPaint(labelPaint, false);	
        s.setStroke(stroke, false);

        s.setStrokePaint(strokePaint, true);
    }

    @Override
    public void saveAsDefaults() 
    {	    
        pref.putBoolean(SCALEBAR_VISIBLE, visible);
        pref.getBoolean(SCALEBAR_LENGTH_AUTOMATIC, lengthAutomatic);

        pref.putDouble(SCALEBAR_POSITION_X, positionX/100.);
        pref.putDouble(SCALEBAR_POSITION_Y, positionY/100.);

        pref.putBoolean(SCALEBAR_LABEL_VISIBLE, labelVisible);
        pref.putFloat(SCALEBAR_LABEL_OFFSET, labelOffset);
        pref.putFloat(SCALEBAR_LABEL_POSITION, labelLengthwisePosition);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, SCALEBAR_STROKE_PAINT, strokePaint);
            SerializationUtilities.putStroke(pref, SCALEBAR_STROKE, stroke);

            SerializationUtilities.putSerializableObject(pref, SCALEBAR_LABEL_PAINT, labelPaint);
            SerializationUtilities.putSerializableObject(pref, SCALEBAR_LABEL_FONT, labelFont);
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
        for(ScaleBar s : boundedScaleBars)
        {
            resetScaleBar(s);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetScaleBar(scaleBar);
        resetEditor();
    }

    @Override
    public Component getEditionComponent()
    {
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return false;
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxScalebarVisible)
        {
            this.visible = selected;
            scaleBar.setVisible(visible);
        }
        else if(source == boxLabelVisible)
        {
            this.labelVisible = selected;
            scaleBar.setLabelVisible(labelVisible);
        }
        else if(source == boxAutomaticLength)
        {
            this.lengthAutomatic = selected;
            scaleBar.setLengthAutomatic(lengthAutomatic);
            spinnerLength.setEnabled(!lengthAutomatic);
        }
    }

    private JPanel buildMainPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());
        SubPanel innerPanel = new SubPanel();

        innerPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(boxScalebarVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        innerPanel.addComponent(new JLabel("Label"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(boxLabelVisible, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        innerPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(fieldLabelFont, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonSelectLabelFont, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(labelPaintSample, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonSelectLabelPaint, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Stroke"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(strokeSample, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonEditStroke, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Length"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        SubPanel panelLength = new SubPanel();
        panelLength.addComponent(boxAutomaticLength, 1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelLength.addComponent(new JLabel(""), 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     
        panelLength.addComponent(spinnerLength, 3, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);     
        innerPanel.addComponent(panelLength, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        SubPanel panelPosition = new SubPanel();

        panelPosition.addComponent(labelLeft, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelPosition.addComponent(sliderX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);
        panelPosition.addComponent(labelRight, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        panelPosition.addComponent(labelBottom, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelPosition.addComponent(sliderY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelPosition.addComponent(labelTop, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        innerPanel.addComponent(labelPositionX, 0, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,.05, 1);
        innerPanel.addComponent(panelPosition, 1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);

        outerPanel.add(innerPanel, BorderLayout.NORTH);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 6, 6, 8));

        return outerPanel;
    }


    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerLength)
        {
            length = ((SpinnerNumberModel)spinnerLength.getModel()).getNumber().doubleValue();
            scaleBar.setLength(length);  
        }

        else if(source == sliderX)
        {
            double value = sliderX.getValue();
            positionX = (int) value;
            double x = positionX/100.;
            scaleBar.setPositionX(x);
        }
        else if(source == sliderY)
        {
            double value = sliderY.getValue();
            positionY = (int) value;
            double y = positionY/100.;
            scaleBar.setPositionY(y);
        }
    }

    @Override
    public String getSubeditorName() 
    {
        return null;
    }

    @Override
    public void setNameBorder(boolean b) 
    {

    }
}
