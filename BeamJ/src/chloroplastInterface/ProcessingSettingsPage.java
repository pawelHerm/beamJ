
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

package chloroplastInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.curveProcessing.SpanType;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.curveProcessing.CroppingDialog;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.gui.curveProcessing.SmootherType;
import atomicJ.sources.ChannelSource;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.Validation;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.prefs.Preferences;


import static atomicJ.gui.NumericalField.VALUE_EDITED;


public class ProcessingSettingsPage extends JPanel implements PropertyChangeListener, WizardPage
{
    private static final long serialVersionUID = 1L;

    public static final String INPUT_PROVIDED = "InputProvided";

    private static final String IDENTIFIER = "Settings";
    private static final String TASK_NAME = "Specify processing settings";
    private static final String DESCRIPTION = "All settings in the general tab are mandatory";

    private static final String PROPERTIES_FILE_COMMENT = "JPhotometer properties file";

    private static final String ADVANCED_TAB = "Advanced";
    private static final String GENERAL_TAB = "General";

    private final Action croppingAction = new CroppingSpecificationAction();

    private final JLabel labelBatchNumber = new JLabel();
    private final JFormattedTextField fieldName = new JFormattedTextField(new DefaultFormatter());

    private final JComboBox<SmootherType> comboCurveSmoothers = new JComboBox<>(SmootherType.values());  

    private final JCheckBox boxPlotRecordedCurve = new JCheckBox("Show");
    private final JCheckBox boxPlotDerivativeCurve = new JCheckBox("Show");

    private final JCheckBox boxCropDomain = new JCheckBox("Crop domain");
    private final JCheckBox boxCropRange = new JCheckBox("Crop range");
    private final JCheckBox boxSmooth = new JCheckBox("Smooth");
    private final JCheckBox boxCalibrationUseReadIn = new JCheckBox("Read-in"); 

    private final JButton buttonImport = new JButton(new ImportAction());
    private final JButton buttonExport = new JButton(new ExportAction());
    private final JButton buttonSelectTrimming = new JButton(croppingAction);

    private final JComboBox<SignalIndexItem> comboSignalIndex;
    
    private final NumericalField fieldCalibrationSlope = new NumericalField();   
    private final NumericalField fieldCalibrationOffset = new NumericalField();   

    private final NumericalField fieldCropLeft = new NumericalField("Left cropping must be a nonnegative number", 0);
    private final NumericalField fieldCropRight = new NumericalField("Right cropping must be a nonnegative number", 0);
    private final NumericalField fieldCropLower = new NumericalField("Lower cropping must be a nonnegative number", 0);
    private final NumericalField fieldCropUpper = new NumericalField("Upper cropping must be a nonnegative number", 0); 
    private final NumericalField fieldSavitzkySpan = new NumericalField("Span must be a nonnegative integer", 0, false);
    private final NumericalField fieldSavitzkyDegree = new NumericalField("Degree must be a nonnegative integer", 0, false);    
    private final NumericalField fieldLoessSpan = new NumericalField("Span must be a positive number between 0 and 100", Double.MIN_VALUE, 100);
    private final NumericalField fieldIterLocal = new NumericalField("Iteration numbers must be a nonnegative integer", 0, false);

    //derivative

    private final JSpinner spinnerDerivativeSpan = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spinnerDerivativePolynomialDegree = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    private final JSpinner spinnerDerivativeRobustnessIterationCount = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));

    private final JComboBox<SpanType> comboDerivativeSpanType = new JComboBox<>(SpanType.values());
    private final JComboBox<SpanGeometry> comboDerivativeSpanGeometry = new JComboBox<>(SpanGeometry.values());
    private final JComboBox<LocalRegressionWeightFunction> comboDerivativeRegressionWeights = new JComboBox<>(LocalRegressionWeightFunction.values());

    //end of derivative

    private final ExtensionFileChooser propertiesChooser = new ExtensionFileChooser(Preferences.userRoot().node(getClass().getName()), "Properties file (.properties)", "properties", true);

    private final CroppingDialog croppingDialog;

    private ProcessingModel model;

    private final JPanel panelControls;

    private boolean settingsSpecified;
    private boolean basicSettingsSpecified;

    public ProcessingSettingsPage(ProcessingModel model)
    {          
        this.model = Validation.requireNonNullParameterName(model, "model");
        this.croppingDialog = new CroppingDialog(model.getResultDestination().getPublicationSite());
        this.model.addPropertyChangeListener(this);

        int currentSignalIndex = model.getSignalIndex();
        int maxSignalIndex = model.getMaximalSignalIndex();

        this.comboSignalIndex = new JComboBox<>(new DefaultComboBoxModel<>(SignalIndexItem.buildSignalIndexArray(maxSignalIndex + 1)));
        comboSignalIndex.setSelectedItem(new SignalIndexItem(currentSignalIndex));
        
        pullModelProperties();      
        initNumericFieldListeners();
        initTextFieldListener();
        initItemListener();
        initChangeListener();

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel generalPanel = buildGeneralPanel();
        JPanel advancedPanel = buildAdvancedPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(generalPanel, GENERAL_TAB);
        tabbedPane.add(advancedPanel,ADVANCED_TAB);

        panelControls = buildControlPanel();

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);   
    }

    public void setProcessingModel(ProcessingModel modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);

        pullModelProperties();
    }

    public ProcessingModel getModel()
    {
        return model;
    }

    private void pullModelProperties()
    {
        this.settingsSpecified = model.areSettingsSpecified();
        this.basicSettingsSpecified = model.areBasicSettingsSpecified();

        
        
        String batchName = model.getCurrentBatchName();

        int batchNumber = model.getCurrentBatchNumber();
        File sourceParentDir = model.getCommonSourceDirectory();
        
        fieldName.setValue(batchName);
        labelBatchNumber.setText(Integer.toString(batchNumber));

        boolean plotRecordedCurve = model.isPlotRecordedCurve();
        boolean plotDerivativeCurve = model.isPlotDerivativeCurve();

        boxPlotRecordedCurve.setSelected(plotRecordedCurve);
        boxPlotDerivativeCurve.setSelected(plotDerivativeCurve);

        boolean domainCropped = model.isDomainCropped();
        boolean rangeCropped = model.isRangeTrimmed();  
        double lowerTrimming = model.getLowerTrimming();
        double upperTrimming = model.getUpperTrimming();
        double rightTrimming = model.getRightTrimming();
        double leftTrimming = model.getLeftTrimming();  
        boolean croppingSelectionOnCurvePossible = model.isSelectionOfCroppingOnCurvePossible();

        boxCropDomain.setSelected(domainCropped);
        boxCropRange.setSelected(rangeCropped);

        fieldCropLeft.setValue(leftTrimming);
        fieldCropRight.setValue(rightTrimming);
        fieldCropLower.setValue(lowerTrimming);
        fieldCropUpper.setValue(upperTrimming); 
        croppingAction.setEnabled(croppingSelectionOnCurvePossible);

        //sets the interface controls consistent with the state

        fieldCropLeft.setEnabled(domainCropped);
        fieldCropRight.setEnabled(domainCropped);
        fieldCropLower.setEnabled(rangeCropped);
        fieldCropUpper.setEnabled(rangeCropped);

        boolean calibrationInputEnabled = model.isCalibrationInputEnabled();       
        double calibrationSlope = model.getCalibrationSlope();
        double calibrationOffset = model.getCalibrationOffset();

        boolean useReadInCalibration = model.isUseReadInCalibration();
        boolean useReadInCalibrationEnabled = model.isCalibrationUseReadInEnabled();

        fieldCalibrationSlope.setValue(calibrationSlope);   
        fieldCalibrationOffset.setValue(calibrationOffset);

        fieldCalibrationSlope.setEnabled(calibrationInputEnabled);
        fieldCalibrationOffset.setEnabled(calibrationInputEnabled);

        boxCalibrationUseReadIn.setSelected(useReadInCalibration);
        boxCalibrationUseReadIn.setEnabled(useReadInCalibrationEnabled);

        boolean smoothed = model.areDataSmoothed();
        SmootherType smootherType = model.getSmootherType();
        double savitzkyDegree = model.getSavitzkyDegree().doubleValue();
        double iterations = model.getLoessIterations().doubleValue();
        double span = model.getLoessSpan(); 
        double savitzkyWindow = model.getSavitzkySpan();

        boxSmooth.setSelected(smoothed);

        fieldSavitzkySpan.setValue(savitzkyWindow);
        fieldSavitzkyDegree.setValue(savitzkyDegree);   
        fieldLoessSpan.setValue(span);
        fieldIterLocal.setValue(iterations);     
        comboCurveSmoothers.setSelectedItem(smootherType);

        //derivative

        int derivativePolynomialDegree = model.getDerivativePolynomialDegree();
        double derivativeSpan = model.getDerivativeSpan();
        SpanType derivativeSpanType = model.getDerivativeSpanType();
        SpanGeometry derivativeSpanGeometry = model.getDerivativeSpanGeometry();
        LocalRegressionWeightFunction derivativeLocalWeightFunction = model.getDerivativeWeightFunction();
        int derivativeInterationsCount = model.getDerivativeRobustnessIterationsCount(); 

        spinnerDerivativeSpan.setValue(derivativeSpan);
        spinnerDerivativePolynomialDegree.setValue(derivativePolynomialDegree);
        spinnerDerivativeRobustnessIterationCount.setValue(derivativeInterationsCount);
        comboDerivativeSpanType.setSelectedItem(derivativeSpanType);
        comboDerivativeSpanGeometry.setSelectedItem(derivativeSpanGeometry);
        comboDerivativeRegressionWeights.setSelectedItem(derivativeLocalWeightFunction);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(ProcessingBatchModelInterface.BATCH_NAME.equals(property))
        {
            String newName = evt.getNewValue().toString();
            fieldName.setValue(newName);
        }
        else if(ProcessingBatchModelInterface.SOURCES.equals(property))
        {            
            List<?> valNew = (List<?>)evt.getNewValue();

            if(!valNew.isEmpty())
            {
                ChannelSource source = (ChannelSource) valNew.get(0);

                File dir =  IOUtilities.findClosestDirectory(source.getCorrespondingFile(), ".properties", 3);
                if(dir != null)
                {
                    propertiesChooser.setCurrentDirectory(dir);
                }           
            }
        }
        else if(ProcessingBatchModel.SIGNAL_INDEX.equals(property))
        {
            int valNew = ((Number)evt.getNewValue()).intValue();
            int valOld = comboSignalIndex.getItemAt(comboSignalIndex.getSelectedIndex()).getZeroBasedSignalIndex();
            if(valNew != valOld)
            {
                comboSignalIndex.setSelectedItem(new SignalIndexItem(valNew));
            }
        }
        else if(ProcessingBatchModel.SMOOTHER_TYPE.equals(property))
        {
            SmootherType newVal = (SmootherType)evt.getNewValue();
            SmootherType oldVal = (SmootherType)comboCurveSmoothers.getSelectedItem();
            if(!(newVal.equals(oldVal)))
            {
                comboCurveSmoothers.setSelectedItem(newVal);
            }
        }
        else if(ProcessingBatchModel.CURVE_SMOOTHED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxSmooth.isSelected();
            if(newVal != oldVal)
            {
                boxSmooth.setSelected(newVal);
            }
        }
        else if(ProcessingBatchModel.LOESS_SPAN.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldLoessSpan.getValue().doubleValue();
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldLoessSpan.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.LOESS_ITERATIONS.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldIterLocal.getValue().doubleValue() ;
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldIterLocal.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.SAVITZKY_SPAN.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldSavitzkySpan.getValue().doubleValue();
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldSavitzkySpan.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.SAVITZKY_DEGREE.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldSavitzkyDegree.getValue().doubleValue();
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldSavitzkyDegree.setValue(newVal);
            }
        }

        else if(ProcessingBatchModel.PLOT_RECORDED_CURVE.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotRecordedCurve.isSelected();

            if(newVal != oldVal)
            {
                boxPlotRecordedCurve.setSelected(newVal);
            }
        }
        else if(ProcessingBatchModel.PLOT_DERIVATIVE.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = boxPlotDerivativeCurve.isSelected();

            if(newVal != oldVal)
            {
                boxPlotDerivativeCurve.setSelected(newVal);
            }
        }


        else if(ProcessingBatchModel.CALIBRATION_INPUT_ENABLED.equals(property))
        {
            boolean newVal = (Boolean)evt.getNewValue();
            fieldCalibrationSlope.setEnabled(newVal);
            fieldCalibrationOffset.setEnabled(newVal);
        }
        else if(ProcessingBatchModel.CALIBRATION_USE_READ_IN.equals(property))
        {
            boolean newVal = (Boolean)evt.getNewValue();
            boolean oldVal = boxCalibrationUseReadIn.isSelected();
            if(newVal != oldVal)
            {
                boxCalibrationUseReadIn.setSelected(newVal);
            }
        }
        else if(ProcessingBatchModel.CALIBARTION_USE_READ_IN_ENABLED.equals(property))
        {
            boolean newVal = (Boolean)evt.getNewValue();
            boxCalibrationUseReadIn.setEnabled(newVal);
        }      
        else if(ProcessingBatchModel.CALIBRATION_SLOPE.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCalibrationSlope.getValue().doubleValue();
            if(Double.compare(oldVal, newVal) != 0)
            {
                fieldCalibrationSlope.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.CALIBRATION_OFFSET.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCalibrationOffset.getValue().doubleValue();
            if(Double.compare(oldVal, newVal) != 0)
            {
                fieldCalibrationOffset.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.DOMAIN_IS_TO_BE_CROPPED.equals(property))
        {
            boolean trimDomain = (boolean)evt.getNewValue();
            fieldCropLeft.setEnabled(trimDomain);
            fieldCropRight.setEnabled(trimDomain);
            boxCropDomain.setSelected(trimDomain);
        }
        else if(ProcessingBatchModel.RANGE_IS_TO_BE_CROPPED.equals(property))
        {
            boolean trimRange = (boolean)evt.getNewValue();
            fieldCropLower.setEnabled(trimRange);
            fieldCropUpper.setEnabled(trimRange);
            boxCropRange.setSelected(trimRange);
        }
        else if(ProcessingBatchModel.LEFT_CROPPING.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCropLeft.getValue().doubleValue();
            if(Double.compare(oldVal, newVal) != 0)
            {
                fieldCropLeft.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.RIGHT_CROPPING.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCropRight.getValue().doubleValue();
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldCropRight.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.LOWER_CROPPING.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCropLower.getValue().doubleValue();
            if(Double.compare(oldVal, newVal) != 0)
            {
                fieldCropLower.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.UPPER_CROPPING.equals(property))
        {
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = fieldCropUpper.getValue().doubleValue();
            if(Double.compare(newVal, oldVal) != 0)
            {
                fieldCropUpper.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.CROPPING_ON_CURVE_SELECTION_POSSIBLE.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            croppingAction.setEnabled(valNew);
        }
        else if(ProcessingModel.CURRENT_BATCH_NUMBER.equals(property))
        {
            pullModelProperties();
        }
        else if(ProcessingBatchModelInterface.SETTINGS_SPECIFIED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();
            if(settingsSpecified != newVal)
            {                
                firePropertyChange(INPUT_PROVIDED, settingsSpecified, newVal);
                settingsSpecified = newVal;
            }
        }
        else if(ProcessingBatchModelInterface.BASIC_SETTINGS_SPECIFIED.equals(property))
        {
            boolean newVal = (boolean)evt.getNewValue();

            if(basicSettingsSpecified != newVal)
            {
                basicSettingsSpecified = newVal;
            }
        }
        else if(ProcessingBatchModelInterface.PARENT_DIRECTORY.equals(property))
        {
        }

        //jumps

        else if(ProcessingBatchModel.DERIVATIVE_SPAN.equals(property))
        {            
            double newVal = ((Number)evt.getNewValue()).doubleValue();
            double oldVal = ((Number)spinnerDerivativeSpan.getValue()).doubleValue();

            if(Double.compare(oldVal, newVal) != 0)
            {
                spinnerDerivativeSpan.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.DERIVATIVE_POLYNOMIAL_DEGREE.equals(property))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();
            int oldVal = ((Number)spinnerDerivativePolynomialDegree.getValue()).intValue();

            if(oldVal != newVal)
            {
                spinnerDerivativePolynomialDegree.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT.equals(property))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();
            int oldVal = ((Number)spinnerDerivativeRobustnessIterationCount.getValue()).intValue();

            if(oldVal != newVal)
            {
                spinnerDerivativeRobustnessIterationCount.setValue(newVal);
            }
        }
        else if(ProcessingBatchModel.DERIVATIVE_SPAN_TYPE.equals(property))
        {
            SpanType newVal = (SpanType)evt.getNewValue();
            SpanType oldVal = (SpanType)comboDerivativeSpanType.getSelectedItem();

            if(!Objects.equals(oldVal, newVal))
            {
                this.comboDerivativeSpanType.setSelectedItem(newVal);
            }
        }
        else if(ProcessingBatchModel.DERIVATIVE_SPAN_GEOMETRY.equals(property))
        {
            SpanGeometry newVal = (SpanGeometry)evt.getNewValue();
            SpanGeometry oldVal = (SpanGeometry)comboDerivativeSpanGeometry.getSelectedItem();

            if(!Objects.equals(oldVal, newVal))
            {
                this.comboDerivativeSpanGeometry.setSelectedItem(newVal);
            }
        }
        else if(ProcessingBatchModel.DERIVATIVE_WEIGHT_FUNCTION.equals(property))
        {
            LocalRegressionWeightFunction newVal = (LocalRegressionWeightFunction)evt.getNewValue();
            LocalRegressionWeightFunction oldValue = (LocalRegressionWeightFunction)comboDerivativeRegressionWeights.getSelectedItem();

            if(!Objects.equals(oldValue, newVal))
            {
                this.comboDerivativeRegressionWeights.setSelectedItem(newVal);
            }
        }
    }

    private void initTextFieldListener()
    {
        fieldName.addPropertyChangeListener("value", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                String batchName = evt.getNewValue().toString();
                model.setBatchName(batchName);
            }
        });
    }

    private void initNumericFieldListeners()
    {

        fieldCalibrationSlope.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setCalibrationSlope(valueNew);
            }
        }); 

        fieldCalibrationOffset.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {           
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setCalibrationOffset(valueNew);
            }
        }); 

        fieldCropLeft.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setLeftCropping(valueNew);
            }
        });
        fieldCropRight.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setRightCropping(valueNew);
            }
        });
        fieldCropLower.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setLowerCropping(valueNew);
            }
        });
        fieldCropUpper.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setUpperCropping(valueNew);
            }
        });     


        fieldLoessSpan.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setLoessSpan(valueNew);
            }
        });
        fieldIterLocal.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setLoessIterations(valueNew);
            }
        });
        fieldSavitzkySpan.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setSavitzkySpan(valueNew);
            }
        });
        fieldSavitzkyDegree.addPropertyChangeListener(VALUE_EDITED, new PropertyChangeListener() {          
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double valueNew = ((Number)evt.getNewValue()).doubleValue();
                model.setSavitzkyDegree(valueNew);
            }
        });

    }

    private void initItemListener()
    {       
        boxCropDomain.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDomainCropped(selected);                
            }
        });
        boxCropRange.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setRangeCropped(selected);
            }
        });
        boxSmooth.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                fieldSavitzkySpan.setEnabled(selected);
                fieldSavitzkyDegree.setEnabled(selected);
                fieldLoessSpan.setEnabled(selected);
                fieldIterLocal.setEnabled(selected);
                comboCurveSmoothers.setEnabled(selected);        

                model.setDataSmoothed(selected);
            }
        });

        boxPlotRecordedCurve.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotRecordedCurve(selected); 
            }
        });

        boxPlotDerivativeCurve.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setPlotDerivativeCurve(selected); 
            }
        });


        boxCalibrationUseReadIn.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setUseReadInCalibration(selected);
            }
        });

        comboCurveSmoothers.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                SmootherType smoother = comboCurveSmoothers.getItemAt(comboCurveSmoothers.getSelectedIndex());        
                model.setSmootherType(smoother);                
            }
        });

        comboDerivativeRegressionWeights.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt)
            {                
                LocalRegressionWeightFunction weightNew = comboDerivativeRegressionWeights.getItemAt(comboDerivativeRegressionWeights.getSelectedIndex());
                model.setDerivativeWeightFunction(weightNew);
            }
        });

        comboDerivativeSpanType.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                SpanType spanType = comboDerivativeSpanType.getItemAt(comboDerivativeSpanType.getSelectedIndex());              
                model.setDerivativeSpanType(spanType);
            }
        });

        comboDerivativeSpanGeometry.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                SpanGeometry spanGeometry = comboDerivativeSpanGeometry.getItemAt(comboDerivativeSpanGeometry.getSelectedIndex());              
                model.setDerivativeSpanGeometry(spanGeometry);
            }
        });
        
        comboSignalIndex.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                SignalIndexItem signalIndexNew = comboSignalIndex.getItemAt(comboSignalIndex.getSelectedIndex());
                model.setSignalIndex(signalIndexNew.getZeroBasedSignalIndex());                
            }
        });
    }

    private void initChangeListener()
    {
        spinnerDerivativeSpan.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double spanNew = ((SpinnerNumberModel)spinnerDerivativeSpan.getModel()).getNumber().doubleValue();
                getModel().setDerivativeSpan(spanNew);              
            }
        });

        spinnerDerivativePolynomialDegree.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int derivativePolynomialDegreeNew = ((SpinnerNumberModel)spinnerDerivativePolynomialDegree.getModel()).getNumber().intValue();
                getModel().setDerivativePolynomialDegree(derivativePolynomialDegreeNew);
            }
        });

        spinnerDerivativeRobustnessIterationCount.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                int derivativeRobustnessIterationCount = ((SpinnerNumberModel)spinnerDerivativeRobustnessIterationCount.getModel()).getNumber().intValue();
                getModel().setDerivativeRobustnessIterationsCount(derivativeRobustnessIterationCount);
            }
        });
    }

    private void saveProperties(File f)
    {
        try(FileOutputStream out = new FileOutputStream(f))
        {
            Properties properties = model.getProperties();
            properties.store(out, PROPERTIES_FILE_COMMENT);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error occured during saving", "", JOptionPane.ERROR_MESSAGE);  
        }
    }

    private void loadProperties(File f)
    {
        try(FileInputStream in = new FileInputStream(f))
        {
            Properties properties = new Properties();
            properties.load(in);
            model.loadProperties(properties);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error occurred during import", "", JOptionPane.ERROR_MESSAGE);  
        }
    }   

    @Override
    public String getTaskName() 
    {
        return TASK_NAME;
    }

    @Override
    public String getTaskDescription() 
    {
        return DESCRIPTION;
    }

    @Override
    public Component getView()
    {
        return this;
    }

    @Override
    public Component getControls()
    {
        return panelControls;
    }

    @Override
    public String getIdentifier()
    {
        return IDENTIFIER; 
    }

    @Override
    public boolean isLast() 
    {
        return true;
    }

    @Override
    public boolean isFirst()
    {
        return false;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return settingsSpecified;
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel(); 
        JLabel labelBatch = new JLabel("Batch no ");

        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonImport).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonExport).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addComponent(buttonImport)
                .addComponent(buttonExport));

        layout.linkSize(buttonImport,buttonExport);

        return panelControl;
    }

    private JPanel buildNamePanel()
    {
        DefaultFormatter formatter = (DefaultFormatter)fieldName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        JLabel labelName = new JLabel("Batch name");
        labelName.setLabelFor(fieldName);
        labelName.setDisplayedMnemonic(KeyEvent.VK_T);
        //labelName.setHorizontalAlignment(SwingConstants.RIGHT);

        SubPanel namePanel = new SubPanel();
        namePanel.addComponent(labelName, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1);
        namePanel.addComponent(fieldName, 0, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        return namePanel;
    }  

    private SubPanel buildDerivativePanel()
    {
        SubPanel panelDerivative = new SubPanel();

        panelDerivative.addComponent(new JLabel("Window width"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        panelDerivative.addComponent(spinnerDerivativeSpan, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelDerivative.addComponent(comboDerivativeSpanType, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelDerivative.addComponent(comboDerivativeSpanGeometry, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelDerivative.addComponent(new JLabel("Polynomial degree"), 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        panelDerivative.addComponent(spinnerDerivativePolynomialDegree, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelDerivative.addComponent(new JLabel("Weight function"), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        panelDerivative.addComponent(comboDerivativeRegressionWeights, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        panelDerivative.addComponent(new JLabel("Iterations"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 1);
        panelDerivative.addComponent(spinnerDerivativeRobustnessIterationCount, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelDerivative.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Rate calculation"));

        return panelDerivative;
    }

    private SubPanel buildCalibrationPanel()
    {       
        JLabel labelCalibrationSlope= new JLabel("Slope (%/V)");
        labelCalibrationSlope.setHorizontalAlignment(SwingConstants.RIGHT);
        labelCalibrationSlope.setLabelFor(fieldCalibrationSlope);
        labelCalibrationSlope.setDisplayedMnemonic(KeyEvent.VK_S);

        SubPanel calibrationPanel = new SubPanel();
        calibrationPanel.addComponent(labelCalibrationSlope, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0, new Insets(5,3,3,3));
        calibrationPanel.addComponent(fieldCalibrationSlope, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0, new Insets(5,3,3,3));
        calibrationPanel.addComponent(boxCalibrationUseReadIn, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);

        JLabel labelCalibrationOffset = new JLabel("Offset (V)");
        labelCalibrationOffset.setHorizontalAlignment(SwingConstants.RIGHT);
        labelCalibrationOffset.setLabelFor(fieldCalibrationOffset);
        labelCalibrationOffset.setDisplayedMnemonic(KeyEvent.VK_O);

        calibrationPanel.addComponent(labelCalibrationOffset, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);
        calibrationPanel.addComponent(fieldCalibrationOffset, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0);

        calibrationPanel.addComponent(Box.createVerticalGlue(), 0, 2, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1);

        calibrationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Calibration"));

        return calibrationPanel;
    }

    private SubPanel buildDataToProcessPanel()
    {       
        JLabel dataToProcessPanel= new JLabel("Channels to process");
        dataToProcessPanel.setHorizontalAlignment(SwingConstants.RIGHT);
        dataToProcessPanel.setLabelFor(comboSignalIndex);
        dataToProcessPanel.setDisplayedMnemonic(KeyEvent.VK_I);

        SubPanel dataPanel = new SubPanel();
        dataPanel.addComponent(dataToProcessPanel, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0, new Insets(5,3,3,3));
        dataPanel.addComponent(comboSignalIndex, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0, new Insets(5,3,3,3));

        dataPanel.addComponent(Box.createVerticalGlue(), 0, 2, 2, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1);

        dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Channels"));

        return dataPanel;
    }

    
    private SubPanel buildCroppingPanel()
    {
        JLabel labelLeft = new JLabel("Left (s)");
        JLabel labelRight = new JLabel("Right (s)");
        JLabel labelLower = new JLabel("Lower (%)");
        JLabel labelUpper = new JLabel("Upper (%)");

        boxCropDomain.setMnemonic(KeyEvent.VK_D);
        boxCropRange.setMnemonic(KeyEvent.VK_R);
        boxCropRange.setDisplayedMnemonicIndex(5);

        SubPanel trimmingPanel = new SubPanel();

        trimmingPanel.addComponent(labelLeft, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelRight, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(boxCropDomain, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(fieldCropLeft, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(fieldCropRight, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);           
        trimmingPanel.addComponent(buttonSelectTrimming, 2, 1, 1, 5, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelLower, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(labelUpper, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(boxCropRange, 0, 3, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        trimmingPanel.addComponent(fieldCropLower, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        trimmingPanel.addComponent(fieldCropUpper, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);           
        trimmingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Cropping"));

        return trimmingPanel;
    }

    private JPanel buildSmoothingPanel()
    {           
        final JPanel smoothingDetails = new JPanel(new CardLayout());
        JPanel panelCombo = new JPanel();
        SubPanel cardSavitzkyGolay = new SubPanel();
        SubPanel cardLocalRegression = new SubPanel();

        boxSmooth.setMnemonic(KeyEvent.VK_M);

        smoothingDetails.add(cardLocalRegression, SmootherType.LOCAL_REGRESSION.toString());
        smoothingDetails.add(cardSavitzkyGolay, SmootherType.SAVITZKY_GOLAY.toString());

        fieldSavitzkySpan.setEnabled(false);
        fieldSavitzkyDegree.setEnabled(false);
        fieldLoessSpan.setEnabled(false);
        fieldIterLocal.setEnabled(false);
        comboCurveSmoothers.setEnabled(false);

        panelCombo.add(boxSmooth);
        panelCombo.add(comboCurveSmoothers);
        comboCurveSmoothers.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent event) 
            {
                CardLayout cl = (CardLayout)(smoothingDetails.getLayout());
                cl.show(smoothingDetails, event.getItem().toString());
            }

        });

        JPanel smoothingPanel = new JPanel(new BorderLayout());

        smoothingPanel.add(smoothingDetails,BorderLayout.CENTER);
        smoothingPanel.add(panelCombo,BorderLayout.SOUTH);

        JLabel labelSpanSavitzky = new JLabel("Half-width (pts)", SwingConstants.RIGHT);
        JLabel labelDegreeSavitzky = new JLabel("Degree", SwingConstants.RIGHT);

        JLabel labelSpanLoess = new JLabel("Span (%)", SwingConstants.RIGHT);
        JLabel labelIterLoess = new JLabel("Iterations", SwingConstants.RIGHT);

        cardSavitzkyGolay.addComponent(labelSpanSavitzky, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardSavitzkyGolay.addComponent(fieldSavitzkySpan, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        cardSavitzkyGolay.addComponent(labelDegreeSavitzky, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardSavitzkyGolay.addComponent(fieldSavitzkyDegree, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        cardLocalRegression.addComponent(labelSpanLoess, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardLocalRegression.addComponent(fieldLoessSpan, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        cardLocalRegression.addComponent(labelIterLoess, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        cardLocalRegression.addComponent(fieldIterLocal, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        smoothingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Smoothing"));

        return smoothingPanel;
    }

    private JPanel buildGeneralPanel()
    {
        SubPanel generalPanel = new SubPanel();

        JPanel namePanel = buildNamePanel();

        JPanel dataToProcessPanel = buildDataToProcessPanel();
        JPanel calibrationPanel = buildCalibrationPanel();
        JPanel plotPanel = buildPlotPanel();
        JPanel smoothingPanel = buildSmoothingPanel();
        JPanel derivativePanel = buildDerivativePanel();

        generalPanel.addComponent(namePanel, 0, 0, 3, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        generalPanel.addComponent(dataToProcessPanel, 0, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        generalPanel.addComponent(calibrationPanel, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        generalPanel.addComponent(plotPanel, 2, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        generalPanel.addComponent(smoothingPanel, 0, 2, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        generalPanel.addComponent(derivativePanel, 1, 2, 2, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        return generalPanel;
    }

    private JPanel buildAdvancedPanel()
    {
        JPanel advancedPanel = new JPanel(new BorderLayout());
        SubPanel inner = new SubPanel();

        JPanel croppingPanel = buildCroppingPanel();

        inner.addComponent(croppingPanel, 0, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        advancedPanel.add(inner, BorderLayout.NORTH);
        return advancedPanel;
    }

    private JPanel buildPlotPanel()
    {
        SubPanel panelPlots = new SubPanel();

        JLabel labelRecordedCurve = new JLabel("Recorded cure");
        JLabel labelIndentationCurve = new JLabel("Rate curve");

        panelPlots.addComponent(labelRecordedCurve, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotRecordedCurve, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);

        panelPlots.addComponent(labelIndentationCurve, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .7, 1);
        panelPlots.addComponent(boxPlotDerivativeCurve, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .25, 1);

        panelPlots.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Graphs"));

        boxPlotRecordedCurve.setMnemonic(KeyEvent.VK_R);
        boxPlotDerivativeCurve.setMnemonic(KeyEvent.VK_D);

        return panelPlots;
    }

    private class ImportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ImportAction() 
        {           
            putValue(NAME, "Import");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            propertiesChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
            int op = propertiesChooser.showOpenDialog(ProcessingSettingsPage.this);
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = propertiesChooser.getSelectedFile();
                loadProperties(f);              
            }
        }
    }

    private class ExportAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExportAction() 
        {           
            putValue(NAME, "Export");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            propertiesChooser.setApproveButtonMnemonic(KeyEvent.VK_S);

            int op = propertiesChooser.showSaveDialog(ProcessingSettingsPage.this);
            if(op == JFileChooser.APPROVE_OPTION)
            {
                File f = propertiesChooser.getSelectedFile();
                saveProperties(f);
            }
        }
    }

    private class CroppingSpecificationAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public CroppingSpecificationAction() 
        {           
            putValue(NAME, "Select");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            croppingDialog.showDialog(PlotFactoryPhotometric.getInstance(), model.getCroppingModel(), model);
            croppingDialog.setVisible(true);
        }
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

    @Override
    public boolean isNextEnabled() {
        return false;
    }

    @Override
    public boolean isSkipEnabled() {
        return false;
    }

    @Override
    public boolean isFinishEnabled() {
        return false;
    }
}
