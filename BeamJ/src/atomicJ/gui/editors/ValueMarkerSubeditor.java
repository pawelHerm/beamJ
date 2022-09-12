
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


import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.CustomizableValueMarker;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;


import static atomicJ.gui.PreferredMarkerStyle.*;

public class ValueMarkerSubeditor extends JPanel implements Subeditor, ActionListener, ChangeListener, ItemListener, PaintReceiver 
{
    private static final long serialVersionUID = 1L;

    private static final String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";

    private final Preferences pref;

    private final boolean initVisible;
    private final float initAlpha;
    private final Paint initOutlinePaint;
    private final Stroke initOutlineStroke;

    private boolean visible;
    private float alpha;
    private Paint outlinePaint;   
    private Stroke outlineStroke;

    private final JCheckBox boxVisible = new JCheckBox();
    private final JSpinner spinnerAlpha = new JSpinner(new SpinnerNumberModel(0.3f, 0.f, 1.f, 0.1f));
    private final StraightStrokeSample outlineStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditOutline = new JButton("Edit");        
    private StrokeChooser outlineStrokeChooser;
    private final List<CustomizableValueMarker> boundedMarkers;
    private final CustomizableValueMarker marker;

    public ValueMarkerSubeditor(CustomizableValueMarker marker, List<CustomizableValueMarker> boundedMarkers) 
    {       
        //sets initial parameters
        this.marker = marker;
        this.boundedMarkers = boundedMarkers;
        this.pref = marker.getPreferences();

        this.initVisible = marker.isVisible();
        this.initAlpha = marker.getAlpha();
        this.initOutlinePaint = marker.getPaint();

        this.initOutlineStroke = marker.getStroke();

        setParametersToInitial();

        //builds components and set editor 

        resetEditor();

        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();       
        add(mainPanel, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        initActionListener();
        initChangeListener();
        initItemListener();
    }

    private void setParametersToInitial()
    {
        this.visible = initVisible;
        this.alpha = initAlpha;
        this.outlinePaint = initOutlinePaint;
        this.outlineStroke = initOutlineStroke;            
    }

    private void initActionListener()
    {
        buttonEditOutline.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);
        buttonEditOutline.addActionListener(this);
    }

    private void initChangeListener()
    {
        spinnerAlpha.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxVisible.addItemListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals(EDIT_OUTLINE_STROKE_COMMAND)) 
        {
            attemptOutlineStrokeSelection();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(this);
        root.setVisible(false);
    }

    private void attemptOutlineStrokeSelection() 
    {
        if(outlineStrokeChooser == null)
        {
            outlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)outlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    outlineStroke = stroke;
                    outlineStrokeSample.setStroke(stroke);
                    marker.setStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return outlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    outlinePaint = paint;
                    outlineStrokeSample.setStrokePaint(paint);
                    marker.setPaint(paint);
                }           
            }
                    );

        }
        outlineStrokeChooser.showDialog();
    }

    private void resetEditor()
    {
        boxVisible.setSelected(visible);
        spinnerAlpha.setValue(alpha);
        outlineStrokeSample.setStroke(outlineStroke);
        outlineStrokeSample.setStrokePaint(outlinePaint);
    }

    private void resetMarker(CustomizableValueMarker p)
    {       
        //only the last method call should invoke notification mechanism 
        p.setVisible(visible);
        p.setAlpha(alpha);
        p.setPaint(outlinePaint);       
        p.setOutlineStroke(outlineStroke);
    }

    @Override
    public void resetToDefaults() 
    {
        this.visible = true;
        this.alpha = pref.getFloat(MARKER_ALPHA, 1.f);
        this.outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, MARKER_PAINT, Color.black);
        this.outlineStroke = SerializationUtilities.getStroke(pref, MARKER_STROKE, SOLID_STROKE);

        resetEditor();
        resetMarker(marker);       
    }

    @Override
    public void saveAsDefaults() 
    {       
        pref.putFloat(MARKER_ALPHA, alpha);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, MARKER_PAINT, outlinePaint);
            SerializationUtilities.putStroke(pref, MARKER_STROKE, outlineStroke);
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
        for(CustomizableValueMarker marker: boundedMarkers)
        {
            resetMarker(marker);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetMarker(marker);
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
        return boundedMarkers.size()>1;
    }

    @Override
    public void stateChanged(ChangeEvent evt)
    {
        Object source = evt.getSource();
        if(source == spinnerAlpha)
        {
            this.alpha = ((SpinnerNumberModel)spinnerAlpha.getModel()).getNumber().floatValue();
            marker.setAlpha(alpha);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();
        if(source == boxVisible)
        {
            this.visible = selected;
            marker.setVisible(visible);
        }      
    }

    @Override
    public Paint getPaint() 
    {
        return outlinePaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        if(paint != null)
        {           
            outlinePaint = paint;
            outlineStrokeSample.setStrokePaint(outlinePaint);
            marker.setPaint(paint);         
        }       
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        mainPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);        

        mainPanel.addComponent(new JLabel("Alpha"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerAlpha, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);        

        mainPanel.addComponent(new JLabel("Outline stroke"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(outlineStrokeSample, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);        
        mainPanel.addComponent(buttonEditOutline, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Marker style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

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

        layout.linkSize(buttonClose, buttonBatchApplyAll, buttonSave, buttonReset, buttonUndo);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
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
            JOptionPane.showMessageDialog(ValueMarkerSubeditor.this, "Marker style was applied to all charts of the same type", "AtomicJ", JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(ValueMarkerSubeditor.this, "Default marker style was changed", "AtomicJ", JOptionPane.INFORMATION_MESSAGE);
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
}
