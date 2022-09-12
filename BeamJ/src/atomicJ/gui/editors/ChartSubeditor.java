
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.PaintSample;

import atomicJ.gui.CustomizableChart;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

import static atomicJ.gui.PreferenceKeys.*;

public class ChartSubeditor extends JPanel implements Subeditor, ActionListener, ItemListener, ChangeListener, PaintReceiver
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_BACKGROUND_PAINT_COMMAND = "SELECT_BACKGROUND_PAINT_COMMAND";

    private final Preferences pref;

    private final boolean initAntialias;
    private final boolean initLockAspectRatio;
    private final Paint initBackgroundPaint;
    private final boolean initUseGradientPaint;
    private final double initPaddingTop;
    private final double initPaddingBottom;
    private final double initPaddingLeft;
    private final double initPaddingRight;

    private boolean antialias;
    private boolean lockAspectRatio;
    private Paint backgroundPaint;
    private boolean useGradientPaint;
    private double paddingTop;
    private double paddingBottom;
    private double paddingLeft;
    private double paddingRight;

    private final JCheckBox boxAntialias = new JCheckBox();
    private final JCheckBox boxLockAspectRatio = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final PaintSample backgroundPaintSample;
    private final JSpinner spinnerPaddingTop;
    private final JSpinner spinnerPaddingBottom;
    private final JSpinner spinnerPaddingLeft;
    private final JSpinner spinnerPaddingRight;	

    private SkewedGradientEditionDialog gradientDialog;

    private final CustomizableChart chart;
    private final List<? extends CustomizableChart> boundedCharts;

    public ChartSubeditor(List<? extends CustomizableChart> boundedCharts, CustomizableChart chart)
    {
        this.boundedCharts = boundedCharts;
        this.chart = chart;

        this.pref = chart.getPlotSpecificPreferences().node("Chart");

        this.initAntialias = chart.getAntiAlias();
        this.initLockAspectRatio = chart.getUseFixedChartAreaSize();
        this.initBackgroundPaint = chart.getBackgroundPaint();
        this.initUseGradientPaint = initBackgroundPaint instanceof GradientPaint;
        this.initPaddingTop = chart.getTopPadding();
        this.initPaddingBottom = chart.getBottomPadding();
        this.initPaddingLeft = chart.getLeftPadding();
        this.initPaddingRight = chart.getRightPadding();

        setParametersToInitial();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        boxAntialias.setSelected(initAntialias);     
        boxUseGradient.setSelected(initUseGradientPaint);
        boxLockAspectRatio.setSelected(initLockAspectRatio);
        this.backgroundPaintSample = new PaintSample(initBackgroundPaint);

        spinnerPaddingTop = new JSpinner(new SpinnerNumberModel(initPaddingTop,0,1000000,0.2));   		
        spinnerPaddingBottom = new JSpinner(new SpinnerNumberModel(initPaddingBottom,0,1000000,0.2));                     
        spinnerPaddingLeft = new JSpinner(new SpinnerNumberModel(initPaddingLeft,0,1000000,0.2));       
        spinnerPaddingRight = new JSpinner(new SpinnerNumberModel(initPaddingRight,0,1000000,0.2));

        JPanel mainPanel = buildMainPanel();       
        add(mainPanel, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        initItemListener();
        initChangeListener();
    }

    private void setParametersToInitial()
    {
        antialias =  initAntialias;
        lockAspectRatio = initLockAspectRatio;
        backgroundPaint = initBackgroundPaint;
        useGradientPaint = initUseGradientPaint;
        paddingTop = initPaddingTop;
        paddingBottom=  initPaddingBottom;
        paddingLeft =  initPaddingLeft;
        paddingRight = initPaddingRight;
    }

    private void initItemListener()
    {
        boxAntialias.addItemListener(this);
        boxLockAspectRatio.addItemListener(this);
        boxUseGradient.addItemListener(this);
    }

    private void initChangeListener()
    {
        spinnerPaddingTop.addChangeListener(this); 	
        spinnerPaddingBottom.addChangeListener(this);                      
        spinnerPaddingLeft.addChangeListener(this);        
        spinnerPaddingRight.addChangeListener(this);  
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableChart ch: boundedCharts)
        {
            resetChart(ch);
        }		
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetChart(chart);
        resetEditor();
    }

    @Override
    public void resetToDefaults() 
    {			
        backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, Color.gray);
        lockAspectRatio = pref.getBoolean(ASPECT_RATIO_LOCKED, false);
        useGradientPaint = backgroundPaint instanceof GradientPaint;
        antialias = pref.getBoolean(ANTIALIASING, true);
        paddingTop = pref.getDouble(CHART_PADDING_TOP, paddingTop);
        paddingBottom = pref.getDouble(CHART_PADDING_BOTTOM, paddingBottom);
        paddingLeft = pref.getDouble(CHART_PADDING_LEFT, paddingLeft);
        paddingRight = pref.getDouble(CHART_PADDING_RIGHT, paddingRight);

        resetChart(chart);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {
        pref.putBoolean(ANTIALIASING, antialias);
        pref.putBoolean(ASPECT_RATIO_LOCKED, lockAspectRatio);
        pref.putDouble(CHART_PADDING_TOP, paddingTop);
        pref.putDouble(CHART_PADDING_BOTTOM, paddingBottom);
        pref.putDouble(CHART_PADDING_LEFT, paddingLeft);
        pref.putDouble(CHART_PADDING_RIGHT, paddingRight);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, BACKGROUND_PAINT, backgroundPaint);
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

    private void resetChart(CustomizableChart ch)
    {
        ch.setAntiAlias(antialias);
        ch.setUseFixedChartAreaSize(lockAspectRatio);
        ch.setBackgroundPaint(backgroundPaint);

        ch.setTopPadding(paddingTop);
        ch.setBottomPadding(paddingBottom);
        ch.setLeftPadding(paddingLeft);
        ch.setRightPadding(paddingRight);
    }

    private void resetEditor()
    {
        backgroundPaintSample.setPaint(backgroundPaint);
        boxLockAspectRatio.setSelected(lockAspectRatio);
        boxUseGradient.setSelected(useGradientPaint);
        boxAntialias.setSelected(antialias);
        spinnerPaddingTop.setValue(paddingTop);
        spinnerPaddingBottom.setValue(paddingBottom);
        spinnerPaddingLeft.setValue(paddingLeft);
        spinnerPaddingRight.setValue(paddingRight);
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

        if (command.equals(SELECT_BACKGROUND_PAINT_COMMAND)) 
        {
            attemptModifyBackgroundPaint();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(this);
        root.setVisible(false);
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
            Paint backgroundPaintNew = JColorChooser.showDialog(ChartSubeditor.this, "Chart background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                backgroundPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                chart.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxAntialias)
        {		
            this.antialias = selected;
            chart.setAntiAlias(antialias);
        }	
        else if(source == boxLockAspectRatio)
        {
            this.lockAspectRatio = selected;
            chart.setUseFixedChartAreaSize(selected);
        }
        else if(source == boxUseGradient)
        {
            this.useGradientPaint = selected;
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerPaddingTop)
        {
            paddingTop = ((SpinnerNumberModel)spinnerPaddingTop.getModel()).getNumber().doubleValue();
            chart.setTopPadding(paddingTop);  
        }
        else if(source == spinnerPaddingBottom)
        {
            paddingBottom = ((SpinnerNumberModel)spinnerPaddingBottom.getModel()).getNumber().doubleValue();
            chart.setBottomPadding(paddingBottom);  
        }
        else if(source == spinnerPaddingLeft)
        {
            paddingLeft = ((SpinnerNumberModel)spinnerPaddingLeft.getModel()).getNumber().doubleValue();
            chart.setLeftPadding(paddingLeft);  
        }
        else if(source == spinnerPaddingRight)
        {
            paddingRight = ((SpinnerNumberModel)spinnerPaddingRight.getModel()).getNumber().doubleValue();
            chart.setRightPadding(paddingRight);  
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
            chart.setBackgroundPaint(backgroundPaint);						
        }		
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        JButton buttonSelectBackgroundPaint = new JButton("Select");
        buttonSelectBackgroundPaint.setActionCommand(SELECT_BACKGROUND_PAINT_COMMAND);
        buttonSelectBackgroundPaint.addActionListener(this);

        mainPanel.addComponent(new JLabel("Draw anti-aliased"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxAntialias, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Lock aspect ratio"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxLockAspectRatio, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Padding"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingTop, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        mainPanel.addComponent(new JLabel("from above"), 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingBottom, 3, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JLabel("from below"), 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        mainPanel.addComponent(spinnerPaddingLeft, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        mainPanel.addComponent(new JLabel("from left"), 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingRight, 3, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JLabel("from right"), 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);       

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxUseGradient, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Background color"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(backgroundPaintSample, 1, 5, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonSelectBackgroundPaint, 5, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .05, 1);    

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Chart style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        BatchApplyAction batchApplyAction = new BatchApplyAction();
        JButton buttonBatchApplyAll = new JButton(batchApplyAction);
        batchApplyAction.setEnabled(isApplyToAllEnabled());

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonSave = new JButton(new SaveAsDefaultsAction());
        JButton buttonReset = new JButton(new ResetToDefaultsAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBatchApplyAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBatchApplyAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose,buttonBatchApplyAll, buttonSave, buttonReset, buttonUndo);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return buttonPanel;
    }

    private class BatchApplyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(ChartSubeditor.this, "Chart style was applied to all charts of the same type", "AtomicJ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undoChanges();
        }
    }

    private class SaveAsDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Use as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(ChartSubeditor.this, "Default chart style was changed", "AtomicJ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ResetToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetToDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Reset to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetToDefaults();
        }
    }

    private class CloseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CloseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            hideRoot();
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
