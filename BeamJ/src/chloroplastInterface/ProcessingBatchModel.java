
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

package chloroplastInterface;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import atomicJ.analysis.*;
import atomicJ.curveProcessing.Crop1DTransformation;
import atomicJ.curveProcessing.LocalRegressionTransformation;
import atomicJ.curveProcessing.SpanType;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DCollection;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.InputNotProvidedException;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.Dataset1DCroppingModel;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.gui.curveProcessing.SmootherType;
import atomicJ.gui.curveProcessing.SmootherType.BasicSmootherModel;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.Validation;

public class ProcessingBatchModel extends AbstractModel implements BasicSmootherModel
{
    private static final double TOLERANCE = 1e-10;

    static final String SAVITZKY_GOLAY = "SavitzkyGolay";
    static final String LOCAL_REGRESSION = "LocalRegression";

    static final String CALIBRATION_INPUT_ENABLED = "CalibrationInputEnabled";//
    static final String CALIBRATION_USE_READ_IN = "CalibrationUseReadIn";//
    static final String CALIBARTION_USE_READ_IN_ENABLED = "CalibrationUseReadInEnabled";//

    static final String CALIBRATION_SLOPE = "CalibrationSlope";//
    static final String CALIBRATION_OFFSET = "CalibrationOffset";//

    static final String DOMAIN_IS_TO_BE_CROPPED = "DomainCropped";//
    static final String RANGE_IS_TO_BE_CROPPED = "RangeCropped";//
    static final String LEFT_CROPPING = "LeftCropping";//
    static final String RIGHT_CROPPING = "RightCropping";//
    static final String UPPER_CROPPING = "UpperCropping";//
    static final String LOWER_CROPPING = "LowerCropping";//

    static final String CROPPING_ON_CURVE_SELECTION_POSSIBLE = "CroppingSelectionOnCurvePossible";//

    static final String CURVE_SMOOTHED = "CurveSmoothed";//
    static final String SMOOTHER_TYPE = "Smoother";//
    static final String LOESS_SPAN = "LoessSpan";//
    static final String LOESS_ITERATIONS = "LoessIterations";//
    static final String SAVITZKY_DEGREE = "Savitzky_Degree";//
    static final String SAVITZKY_SPAN = "Savitzky_Span";//

    static final String PLOT_RECORDED_CURVE = "PlotRecordedCurve";//
    static final String PLOT_DERIVATIVE = "PlotDerviative";//

    public static final String DERIVATIVE_SPAN = "DerivativeSpan";
    public static final String DERIVATIVE_SPAN_TYPE = "DerivativeSpanType";
    public static final String DERIVATIVE_SPAN_GEOMETRY = "DerivativeSpanGeometry";
    public static final String DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT = "DerivativeRobustnessIterationsCount";
    public static final String DERIVATIVE_WEIGHT_FUNCTION = "DerivativeWeightFunction";
    public static final String DERIVATIVE_POLYNOMIAL_DEGREE = "DerivativePolynomialDegree";

    public static final String SIGNAL_INDEX = "SignalIndex";
    public static final String MAX_SIGNAL_INDEX = "MaxSignalIndex";
    
    private static volatile AtomicInteger PROCESSING_BATCH_COUNT = new AtomicInteger(0);

    private static final Preferences PREF = Preferences.userNodeForPackage(ProcessingBatchModel.class).node(ProcessingBatchModel.class.getName());

    private List<SimplePhotometricSource> sources;

    private double calibrationSlope = Double.NaN;	
    private double calibrationOffset = Double.NaN;

    private boolean useReadInCalibration = true;
    private double lowerCropping = Double.NaN;
    private double upperCropping = Double.NaN;
    private double rightCropping = Double.NaN;
    private double leftCropping = Double.NaN;
    private boolean domainCropped = false;
    private boolean rangeCropped = false;	

    private SmootherType smootherType = SmootherType.LOCAL_REGRESSION;
    private boolean smoothed = false;
    private double loessSpan = Double.NaN;; 
    private Number loessIterations = Double.NaN;
    private Number savitzkyDegree = Double.NaN;
    private double savitzkySpan = Double.NaN;
    private boolean plotRecordedCurve = PREF.getBoolean(PLOT_RECORDED_CURVE, true);
    private boolean plotDerivativeCurve = PREF.getBoolean(PLOT_DERIVATIVE, true);

    private double readInCalibrationSlope = Double.NaN;
    private double readInCalibrationOffset = Double.NaN;

    private boolean calibrationInputCanBeUsed = false;
    private boolean calibrationReadInCanBeUsed = false;
    private boolean calibrationInputNecessary = false;
    private boolean calibrationReadInNecessary = false;
    private boolean calibrationUseReadInEnabled = false;
    private boolean calibrationInputEnabled = false;

    private double derivativeSpan = PREF.getDouble(DERIVATIVE_SPAN, 0.01);
    private SpanType derivativeSpanType = SpanType.getValue(PREF.get(DERIVATIVE_SPAN_TYPE, SpanType.POINT_FRACTION.getIdentifier()), SpanType.POINT_FRACTION);
    private SpanGeometry derivativeSpanGeometry = SpanGeometry.getValue(PREF.get(DERIVATIVE_SPAN_GEOMETRY, SpanGeometry.NEAREST_NEIGHBOUR.getIdentifier()), SpanGeometry.NEAREST_NEIGHBOUR) ;
    private int derivativeRobustnessIterationsCount = PREF.getInt(DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT, 0);
    private LocalRegressionWeightFunction derivativeWeightFunction = LocalRegressionWeightFunction.getValue(PREF.get(DERIVATIVE_WEIGHT_FUNCTION, LocalRegressionWeightFunction.TRICUBE.getIdentifier()), LocalRegressionWeightFunction.TRICUBE);
    private int derivativePolynomialDegree = PREF.getInt(DERIVATIVE_POLYNOMIAL_DEGREE, 2);

    private int signalIndex = 0;
    private int maxSignalIndex = 0;
    
    private File parentDirectory;

    private boolean basicSettingsSpecified;
    private boolean settingsSpecified;
    private boolean croppingOnCurveSelectionPossible;
    private boolean nonEmpty;

    private final ResultDestinationPhotometric destination;

    private String batchName;
    private final int batchNumber;

    private final int processingBatchUniqueId;

    public ProcessingBatchModel(ResultDestinationPhotometric resultDestination, int batchNumber)
    {
        this(resultDestination, new ArrayList<>(), Integer.toString(batchNumber), batchNumber);
    }

    public ProcessingBatchModel(ResultDestinationPhotometric resultDestination, String name, int batchNumber)
    {
        this(resultDestination, new ArrayList<>(), name, batchNumber);
    }

    public ProcessingBatchModel(ResultDestinationPhotometric resultDestination, List<SimplePhotometricSource> sources, String name, int batchNumber)
    {
        this.destination = resultDestination;
        this.sources = sources;
        this.batchName = name;
        this.batchNumber = batchNumber;
        this.processingBatchUniqueId = PROCESSING_BATCH_COUNT.incrementAndGet();
        this.maxSignalIndex = calculateMaxSignalIndex(sources);

        initDefaults();

        checkIfNonEmpty();
        checkCalibrationSpecificationSettings();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified();
        checkIfSelectionOfCroppingOnCurvePossible();
    }

    public ProcessingBatchModel(ProcessingBatchMementoPhotometric memento, VisualizationSettingsPhotometric visSettings, List<SimplePhotometricSource> sources)
    {
        this.batchName = memento.getBatchName();
        this.batchNumber = memento.getBatchNumber();
        this.processingBatchUniqueId = PROCESSING_BATCH_COUNT.incrementAndGet();

        this.destination = memento.getResultDestination();      

        this.calibrationSlope = memento.getCalibrationSlope(); 
        this.calibrationOffset = memento.getCalibrationOffset();

        this.useReadInCalibration = memento.getUseReadInCalibration();
        this.lowerCropping = memento.getLowerCropping();
        this.upperCropping = memento.getUpperCropping();
        this.rightCropping = memento.getRightCropping();
        this.leftCropping = memento.getLeftCropping();    
        this.domainCropped = memento.isDomainToBeCropped();
        this.rangeCropped = memento.isRangeToBeCropped();   

        this.smootherType = memento.getSmootherName();
        this.smoothed = memento.areDataSmoothed();
        this.loessSpan = memento.getLoessSpan(); 
        this.loessIterations = memento.getLoessIterations();
        this.savitzkyDegree = memento.getSavitzkyDegree();
        this.savitzkySpan = memento.getSavitzkySpan();

        this.derivativeSpan = memento.getDerivativeSpan();
        this.derivativePolynomialDegree = memento.getDerivativePolynomialDegree();
        this.derivativeSpanType = memento.getDerivativeSpanType();
        this.derivativeSpanGeometry = memento.getDerivativeSpanGeometry();
        this.derivativeRobustnessIterationsCount = memento.getDerivativeRobustnessIterationsCount();
        this.derivativeWeightFunction = memento.getDerivativeWeightFunction();

        this.plotRecordedCurve = visSettings.isPlotRecordedCurve();
        this.plotDerivativeCurve = visSettings.isPlotDerivativeCurve();

        this.sources = sources;
        this.maxSignalIndex = calculateMaxSignalIndex(sources);
        
        this.parentDirectory = findParentDirectory();

        this.readInCalibrationSlope = Double.NaN;
        this.readInCalibrationOffset = Double.NaN;

        checkIfNonEmpty();
        checkCalibrationSpecificationSettings();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified();
        checkIfSelectionOfCroppingOnCurvePossible();
    }

    private void initDefaults()
    {  
        this.parentDirectory = findParentDirectory();
    }

    public int getProcessingBatchUniqueId()
    {
        return processingBatchUniqueId;
    }

    public ResultDestinationPhotometric getResultDestination()
    {
        return destination;
    }

    public List<SimplePhotometricSource> getSources()
    {
        return sources;
    }

    public void addSources(List<SimplePhotometricSource> sourcesToAdd)
    {
        List<SimplePhotometricSource> sources = new ArrayList<>(this.sources);
        sources.addAll(sourcesToAdd);

        setSources(sources);
    }

    public void removeSources(List<SimplePhotometricSource> sourcesToRemove)
    {
        List<SimplePhotometricSource> sources = new ArrayList<>(this.sources);
        sources.remove(sourcesToRemove);

        setSources(sources);
    }

    public void setSources(List<SimplePhotometricSource> sourcesNew)
    {
        List<SimplePhotometricSource> sourcesOld = sources;
        this.sources = sourcesNew;

        firePropertyChange(ProcessingBatchModelInterface.SOURCES, sourcesOld, sourcesNew);      

        checkIfParentDirectoryChanged();
        checkIfMaxSignalIndexChanged();
        checkIfNonEmpty();
        checkCalibrationSpecificationSettings();
    }

    private int calculateMaxSignalIndex(List<SimplePhotometricSource> sources)
    {
        int maxIndex = 0;
        
        for(SimplePhotometricSource source : sources)
        {
            maxIndex = Math.max(maxIndex, source.getRecordedSignalCount() - 1);
        }
        
        return maxIndex;
    }
    
    private void checkIfMaxSignalIndexChanged()
    {
        int maxSignalIndexOld = this.maxSignalIndex;
        this.maxSignalIndex = calculateMaxSignalIndex(this.sources);
        if(this.maxSignalIndex != maxSignalIndexOld)
        {
            firePropertyChange(MAX_SIGNAL_INDEX,maxSignalIndexOld, maxSignalIndex);
        }
    }
    
    public File getCommonSourceDirectory()
    {
        return parentDirectory;
    }

    private File findParentDirectory()
    {
        return BatchUtilities.findLastCommonSourceDirectory(sources);
    }

    public int getBatchNumber()
    {
        return batchNumber;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public void setBatchName(String batchNameNew)
    {
        String batchNameOld = this.batchName;
        this.batchName = batchNameNew;

        firePropertyChange(ProcessingBatchModelInterface.BATCH_NAME, batchNameOld, batchNameNew);

        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified(); 
    }

    public int getSignalIndex()
    {
        return signalIndex;
    }
    
    public void setSignalIndex(int signalIndexNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(signalIndexNew, 0, maxSignalIndex, "signalIndexNew");
        
        int signalIndexOld = this.signalIndex;
        this.signalIndex = signalIndexNew;
        
        firePropertyChange(SIGNAL_INDEX, signalIndexOld, signalIndexNew);
        checkCalibrationSpecificationSettings();
    }
    
    public int getMaximalSignalIndex()
    {
        return maxSignalIndex;
    }
    
    public double getCalibrationSlope()
    {
        return calibrationSlope;
    }

    public void setCalibrationSlope(double calibrationSlopeNew)
    {
        if(calibrationInputEnabled)
        {
            setCalibrationSlopePrivate(calibrationSlopeNew);
            checkIfBasicSettingsSpecified();   
            checkIfSettingsSpecified(); 
            checkIfSelectionOfCroppingOnCurvePossible();
        }
    }

    private void setCalibrationSlopePrivate(double calibrationSlopeyNew)
    {
        double calibrationSlopeOld = this.calibrationSlope;
        this.calibrationSlope = calibrationSlopeyNew;

        firePropertyChange(CALIBRATION_SLOPE, calibrationSlopeOld, this.calibrationSlope);
    }

    public double getCalibrationOffset()
    {
        return calibrationOffset;
    }

    public void setCalibrationOffset(double calibrationOffsetNew)
    {
        if(calibrationInputEnabled)
        {
            setCalibrationOffsetPrivate(calibrationOffsetNew);
            checkIfBasicSettingsSpecified();   
            checkIfSettingsSpecified(); 
            checkIfSelectionOfCroppingOnCurvePossible();
        }
    }

    private void setCalibrationOffsetPrivate(double calibrationOffsetNew)
    {
        double calibrationOffsetOld = this.calibrationOffset;
        this.calibrationOffset = calibrationOffsetNew;

        firePropertyChange(CALIBRATION_OFFSET, calibrationOffsetOld, this.calibrationOffset);
    }

    public boolean getUseReadInCalibration()
    {
        return useReadInCalibration;
    }  

    public void setUseReadInCalibration(boolean valueNew)
    {        
        if(calibrationUseReadInEnabled)
        {
            setUseReadInCalibrationPrivate(valueNew);
        }
    }   

    private void setUseReadInCalibrationPrivate(boolean valueNew)
    {        
        Boolean useReadInCalibrationOld = this.useReadInCalibration;
        this.useReadInCalibration = valueNew;

        firePropertyChange(CALIBRATION_USE_READ_IN, useReadInCalibrationOld, this.useReadInCalibration);

        checkIfCalibrationInputEnabled();
        ensureConsistencyOfCalibrationValues();
        checkIfBasicSettingsSpecified();
        checkIfSettingsSpecified(); 
        checkIfSelectionOfCroppingOnCurvePossible();
    }

    public double getLeftCropping()
    {
        return leftCropping;
    }

    public void setLeftCropping(double leftTrimmingNew)
    {
        double leftTrimmingOld = this.leftCropping;
        this.leftCropping = leftTrimmingNew;

        firePropertyChange(LEFT_CROPPING, leftTrimmingOld, leftTrimmingNew);

        checkIfSettingsSpecified();
    }

    public double getRightCropping()
    {
        return rightCropping;
    }

    public void setRightCropping(double rightTrimmingNew)
    {
        double rightTrimmingOld = this.rightCropping;
        this.rightCropping = rightTrimmingNew;

        firePropertyChange(RIGHT_CROPPING, rightTrimmingOld, rightTrimmingNew);

        checkIfSettingsSpecified();
    }

    public double getLowerCropping()
    {
        return lowerCropping;
    }

    public void setLowerCropping(double lowerTrimmingNew)
    {
        double lowerTrimmingOld = this.lowerCropping;
        this.lowerCropping = lowerTrimmingNew;

        firePropertyChange(LOWER_CROPPING, lowerTrimmingOld, lowerTrimmingNew);

        checkIfSettingsSpecified();
    }

    public double getUpperCropping()
    {
        return upperCropping;
    }

    public void setUpperCropping(double upperTrimmingNew)
    {
        double upperTrimmingOld = this.upperCropping;
        this.upperCropping = upperTrimmingNew;

        firePropertyChange(UPPER_CROPPING, upperTrimmingOld, upperTrimmingNew);

        checkIfSettingsSpecified();
    }

    public boolean isDomainCropped()
    {
        return domainCropped; 
    }

    public void setDomainCropped(boolean domainTrimmedNew)
    {
        boolean domainTrimmedOld = this.domainCropped;
        this.domainCropped = domainTrimmedNew;

        firePropertyChange(DOMAIN_IS_TO_BE_CROPPED, domainTrimmedOld, domainTrimmedNew);

        checkIfSettingsSpecified();
        checkIfSelectionOfCroppingOnCurvePossible();
    }

    public boolean isRangeTrimmed()
    {
        return rangeCropped; 
    }

    public void setRangeCropped(boolean rangeTrimmedNew)
    {		
        boolean rangeTrimmedOld = this.rangeCropped;
        this.rangeCropped = rangeTrimmedNew;

        firePropertyChange(RANGE_IS_TO_BE_CROPPED, rangeTrimmedOld, rangeTrimmedNew);

        checkIfSettingsSpecified();
        checkIfSelectionOfCroppingOnCurvePossible();
    }

    public SmootherType getSmootherType()
    {
        return smootherType;
    }

    public void setSmootherType(SmootherType smootherTypeNew)
    {
        if(smootherTypeNew == null)
        {
            return;
        }

        SmootherType smootherTypeOld = this.smootherType;
        this.smootherType = smootherTypeNew;

        firePropertyChange(SMOOTHER_TYPE, smootherTypeOld, smootherTypeNew);

        checkIfSettingsSpecified();
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }

    public void setDataSmoothed(boolean dataSmoothedNew)
    {		
        boolean dataSmoothedOld = this.smoothed;
        this.smoothed = dataSmoothedNew;

        firePropertyChange(CURVE_SMOOTHED, dataSmoothedOld, dataSmoothedNew);

        checkIfSettingsSpecified();
    }

    @Override
    public double getLoessSpan()
    {
        return loessSpan;
    }

    @Override
    public void setLoessSpan(double loessSpanNew)
    {
        double loessSpanOld = this.loessSpan;
        this.loessSpan = loessSpanNew;

        firePropertyChange(LOESS_SPAN, loessSpanOld, loessSpanNew);

        checkIfSettingsSpecified();
    }

    @Override
    public Number getLoessIterations()
    {
        return loessIterations;
    }

    @Override
    public void setLoessIterations(Number loessIterationsNew)
    {        
        if(loessIterationsNew == null)
        {
            return;
        }

        Double loessIterationsNewDouble = Math.rint(loessIterationsNew.doubleValue());  
        Double loessIterationsOld = this.loessIterations.doubleValue(); 
        this.loessIterations = loessIterationsNewDouble;

        firePropertyChange(LOESS_ITERATIONS, loessIterationsOld, loessIterationsNewDouble);

        checkIfSettingsSpecified();
    }

    @Override
    public Number getSavitzkyDegree()
    {
        return savitzkyDegree;
    }

    @Override
    public void setSavitzkyDegree(Number savitzkyDegreeNew)
    {
        if(savitzkyDegreeNew == null)
        {
            return;
        }       

        Double savitzkyDegreeNewDouble = Math.rint(savitzkyDegreeNew.doubleValue());
        Double savitzkyDegreeOld = this.savitzkyDegree.doubleValue();      
        this.savitzkyDegree = savitzkyDegreeNewDouble;

        firePropertyChange(SAVITZKY_DEGREE, savitzkyDegreeOld, savitzkyDegreeNewDouble);
        flushPreferences();

        checkIfSettingsSpecified();
    }

    @Override
    public double getSavitzkySpan()
    {
        return savitzkySpan;
    }

    @Override
    public void setSavitzkySpan(double savitzkySpanNew)
    {
        double savitzkySpanOld = this.savitzkySpan;
        this.savitzkySpan = savitzkySpanNew;

        firePropertyChange(SAVITZKY_SPAN, savitzkySpanOld, savitzkySpanNew);

        checkIfSettingsSpecified();
    }


    public boolean isPlotRecordedCurve()
    {
        return plotRecordedCurve;
    }

    public void setPlotRecordedCurve(boolean plotRecordedCurveNew)
    {   
        boolean plotRecordedCurveOld = this.plotRecordedCurve;
        this.plotRecordedCurve = plotRecordedCurveNew;

        firePropertyChange(PLOT_RECORDED_CURVE, plotRecordedCurveOld, plotRecordedCurveNew);

        PREF.putBoolean(PLOT_RECORDED_CURVE, this.plotRecordedCurve);
        flushPreferences();
    }

    public boolean isPlotDerivativeCurve()
    {
        return plotDerivativeCurve;
    }

    public void setPlotDerivativeCurve(boolean plotIndentationNew)
    {
        boolean plotIndentationOld = this.plotDerivativeCurve;
        this.plotDerivativeCurve = plotIndentationNew;

        firePropertyChange(PLOT_DERIVATIVE, plotIndentationOld, plotIndentationNew);

        PREF.putBoolean(PLOT_DERIVATIVE, this.plotDerivativeCurve);
        flushPreferences();
    }

    public boolean isSelectionOfCroppingOnCurvePossible()
    {
        return croppingOnCurveSelectionPossible;
    }

    private void checkIfSelectionOfCroppingOnCurvePossible()
    {
        boolean croppingOnCurveSelectionPossibleNew = calculateIfCroppingOnCurveSelectionPossible();
        boolean trimmingOnCurveSelectionPossibleOld = this.croppingOnCurveSelectionPossible;     

        if(croppingOnCurveSelectionPossibleNew != this.croppingOnCurveSelectionPossible)
        {
            this.croppingOnCurveSelectionPossible = croppingOnCurveSelectionPossibleNew;
            firePropertyChange(CROPPING_ON_CURVE_SELECTION_POSSIBLE, trimmingOnCurveSelectionPossibleOld, croppingOnCurveSelectionPossibleNew);
        }
    }   

    public boolean calculateIfCroppingOnCurveSelectionPossible()
    {
        boolean isSensitivitySpecified = calculateCalibrationIsSpecifiedIfNecessary();
        boolean trimmingSelectionPossible = (domainCropped || rangeCropped) && isSensitivitySpecified;
        return trimmingSelectionPossible;
    }

    public Dataset1DCroppingModel<Channel1DCollection> getCroppingModel()
    {
        SimplePhotometricSource source = sources.get(0);
        List<? extends Channel1D> channels = source.getChannels();
        Channel1DCollection channelCollection = new Channel1DCollection(RecordingModel.PHOTOMETRIC_RECORDING_KEY);
        for(Channel1D ch : channels)
        {
            channelCollection.addChannel(ch.getChannelData(), ch.getIdentifier());
        }

        Dataset1DCroppingModel<Channel1DCollection> model = new Dataset1DCroppingModel<>(channelCollection, channelCollection.getCombinedXRange(), channelCollection.getCombinedYRange(), domainCropped, rangeCropped);
        model.setLeftCropping(leftCropping);
        model.setRightCropping(rightCropping);
        model.setLowerCropping(lowerCropping);
        model.setUpperCropping(upperCropping);

        return model;
    }

    boolean isNonEmpty()
    {
        return nonEmpty;
    }

    boolean areSettingSpecified()
    {
        return settingsSpecified;
    }

    boolean isNecessaryInputProvided()
    {
        boolean specifiedSettings = areSettingSpecified();
        boolean isNonempty = isNonEmpty();
        boolean necessaryInputProvided = ((isNonempty&&specifiedSettings)||(!isNonempty));

        return necessaryInputProvided;
    }

    private boolean isCroppingInputProvided()
    {
        boolean inputProvided = true;
        if(domainCropped)
        {
            inputProvided = (!Double.isNaN(leftCropping))&&(!Double.isNaN(rightCropping));
        }
        if(rangeCropped)
        {
            inputProvided = inputProvided&&(!Double.isNaN(lowerCropping))&&(!Double.isNaN(upperCropping));
        }
        return inputProvided;
    }

    private boolean isSmoothingInputProvided()
    {
        boolean inputProvided = smoothed ? smootherType.isInputProvided(this) : true;

        return inputProvided;
    }

    boolean areBasicSettingsSpecified()
    {
        return basicSettingsSpecified;
    }

    private CropSettings buildCropSettings()
    {
        double upperCrop = rangeCropped ? getUpperCropping() : 0;
        double bottomCrop = rangeCropped ? getLowerCropping() : 0;
        double leftCrop = domainCropped ? getLeftCropping() : 0;
        double rightCrop = domainCropped ? getRightCropping() : 0;

        CropSettings cropSettings = new CropSettings(upperCrop, leftCrop,bottomCrop,rightCrop);

        return cropSettings;
    }

    private Channel1DDataTransformation buildDerivativeTrasformation()
    {
        Channel1DDataTransformation tr = new LocalRegressionTransformation(derivativeSpan, derivativeSpanGeometry, derivativeSpanType, derivativeRobustnessIterationsCount, derivativePolynomialDegree, 1, derivativeWeightFunction);
        return tr;
    }

    public int getDerivativePolynomialDegree()
    {
        return derivativePolynomialDegree;
    }

    public void setDerivativePolynomialDegree(int derivativePolynomialDegreeNew)
    {
        Validation.requireValueGreaterOrEqualTo(derivativePolynomialDegreeNew, 1);

        if(derivativePolynomialDegree != derivativePolynomialDegreeNew)
        {
            int derivativePolynomialDegreeOld = this.derivativePolynomialDegree;
            this.derivativePolynomialDegree = derivativePolynomialDegreeNew;

            firePropertyChange(DERIVATIVE_POLYNOMIAL_DEGREE, derivativePolynomialDegreeOld, this.derivativePolynomialDegree);
            PREF.putInt(DERIVATIVE_POLYNOMIAL_DEGREE, this.derivativePolynomialDegree);
            flushPreferences();
        }
    }

    public double getDerivativeSpan()
    {
        return derivativeSpan;
    }

    public void setDerivativeSpan(double derivativeSpanNew)
    {
        if(Double.compare(derivativeSpan, derivativeSpanNew) != 0)
        {
            double derivativeSpanOld = this.derivativeSpan;
            this.derivativeSpan = derivativeSpanNew;

            firePropertyChange(DERIVATIVE_SPAN, derivativeSpanOld, this.derivativeSpan);
            PREF.putDouble(DERIVATIVE_SPAN, this.derivativeSpan);
            flushPreferences();
        }
    }

    public SpanType getDerivativeSpanType()
    {
        return derivativeSpanType;
    }

    public void setDerivativeSpanType(SpanType spanTypeNew)
    {
        if(!Objects.equals(this.derivativeSpanType, spanTypeNew))
        {
            SpanType spanTypeOld = this.derivativeSpanType;
            this.derivativeSpanType = spanTypeNew;

            firePropertyChange(DERIVATIVE_SPAN_TYPE, spanTypeOld, spanTypeNew);
            PREF.put(DERIVATIVE_SPAN_TYPE, this.derivativeSpanType.getIdentifier());
            flushPreferences();
        }        
    }

    public SpanGeometry getDerivativeSpanGeometry()
    {
        return derivativeSpanGeometry;
    }

    public void setDerivativeSpanGeometry(SpanGeometry spanGeometryNew)
    {
        if(!Objects.equals(this.derivativeSpanGeometry, spanGeometryNew))
        {
            SpanGeometry spanGeometryOld = this.derivativeSpanGeometry;
            this.derivativeSpanGeometry = spanGeometryNew;

            firePropertyChange(DERIVATIVE_SPAN_GEOMETRY, spanGeometryOld, spanGeometryNew);
            PREF.put(DERIVATIVE_SPAN_GEOMETRY, this.derivativeSpanGeometry.getIdentifier());
            flushPreferences();
        }        
    }

    public LocalRegressionWeightFunction getDerivativeWeightFunction()
    {
        return derivativeWeightFunction;
    }

    public void setDerivativeWeightFunction(LocalRegressionWeightFunction derivativeWeightFunctionNew)
    {
        if(!Objects.equals(this.derivativeWeightFunction, derivativeWeightFunctionNew))
        {
            LocalRegressionWeightFunction derivativeWeightFunctionOld = this.derivativeWeightFunction;
            this.derivativeWeightFunction = derivativeWeightFunctionNew;

            firePropertyChange(DERIVATIVE_WEIGHT_FUNCTION, derivativeWeightFunctionOld, derivativeWeightFunctionNew);
            PREF.put(DERIVATIVE_WEIGHT_FUNCTION, this.derivativeWeightFunction.getIdentifier());
            flushPreferences();
        }        
    }

    public int getDerivativeRobustnessIterationsCount()
    {
        return derivativeRobustnessIterationsCount;
    }

    public void setDerivativeRobustnessIterationsCount(int derivativeRobustnessIterationsCountNew)
    {
        Validation.requireValueGreaterOrEqualTo(derivativeRobustnessIterationsCountNew, 0);

        if(this.derivativeRobustnessIterationsCount != derivativeRobustnessIterationsCountNew)
        {
            int derivativeRobustnessIterationsCountOld = this.derivativeRobustnessIterationsCount;
            this.derivativeRobustnessIterationsCount = derivativeRobustnessIterationsCountNew;

            firePropertyChange(DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT, derivativeRobustnessIterationsCountOld, this.derivativeRobustnessIterationsCount);
            PREF.putInt(DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT, this.derivativeRobustnessIterationsCount);
            flushPreferences();
        }
    }

    private ProcessingSettings buildProcessingSettings(SimplePhotometricSource source) throws InputNotProvidedException, UserCommunicableException
    {    
        CalibrationSettingsImmutable sourceCalibration = source.getCalibrationSettings(signalIndex);
        double calibrationSlope = useReadInCalibration && source.isCalibrationKnown(signalIndex) ? sourceCalibration.getCalibrationSlopeInPercentsPerVolt() : this.calibrationSlope;
        double calibrationOffset = useReadInCalibration && source.isCalibrationKnown(signalIndex) ? sourceCalibration.getCalibrationOffsetInVolts() : this.calibrationOffset; 

        CropSettings cropSettings = buildCropSettings();
        Channel1DDataTransformation trimmer = new Crop1DTransformation(cropSettings);

        Channel1DDataTransformation derivativeTransformation  = buildDerivativeTrasformation();

        ProcessingSettings.BuilderPhotometric builder = new ProcessingSettings.BuilderPhotometric(calibrationSlope,calibrationOffset, derivativeTransformation);
        builder.trimmer(trimmer).trimmed(cropSettings.isAnythingCropped());

        if(smoothed && isSmoothingInputProvided())
        {
            try 
            {
                Channel1DDataTransformation smoother = smootherType.getSmoothingTransformation(this);
                builder.smoother(smoother);
                builder.smoothed(true);
            } 
            catch (Exception e) 
            {                   
                throw new UserCommunicableException("Due to error, smoothing cannot be carried out", e);
            }
        };

        ProcessingSettings settings = builder.build();
        return settings;
    }

    public List<ProcessablePackPhotometric> buildProcessingBatch()
    {        
        try
        {
            VisualizationSettingsPhotometric visSettings = new VisualizationSettingsPhotometric(plotRecordedCurve, plotDerivativeCurve);


            List<ProcessablePackPhotometric> allPacks = new ArrayList<>();

            ProcessingBatchMementoPhotometric memento = new ProcessingBatchMementoPhotometric(this);

            for(SimplePhotometricSource source: sources)
            {
                ProcessingSettings procSettings = buildProcessingSettings(source);

                ProcessablePackPhotometric pack = new ProcessablePackPhotometric(source, procSettings, visSettings, new IdentityTag(batchNumber, batchName));
                source.setProcessingMemento(memento);

                allPacks.add(pack);                        
            }

            return allPacks;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(destination.getPublicationSite(), "An error occured during the computation", "", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /*
     * This methods check whether:
     * - calibrationInputCanBeUsed - at least one source contains enough information to use user - specified sensitivity (i.e. is not calibrated or if it is - then the sensitivity is know)
     * - calibrationReadInCanBeUsed - at least one source contains enough information to use read - in sensitivity (i.e. is calibrated or sensitivity is known)
     * - calibrationInputNecessary - at least one source does not contain enough information to use read in sensitivity (i.e. it is not calibrated and the read-in sensitivity is not known)
     * - calibrationReadInNecessary - at least one source does not contain enough information to use user - specified sensitivity (i.e. it is calibrated, but sensitivity used for calibration is not known, so that it cannot be converted back to raw voltage readings)
     */

    private void checkCalibrationSpecificationSettings()
    {      
        boolean calibrationInputCanBeUsed = false;
        boolean calibrationInputNecessary = false;
        boolean calibrationReadInCanBeUsed = false;
        boolean calibrationReadInNecessary = false; 

        for(SimplePhotometricSource simpleSource : sources)
        {
            if(simpleSource.getRecordedSignalCount() > this.signalIndex)
            {
                boolean calibrated = simpleSource.isCalibrated(this.signalIndex);

                boolean readInCalibrationKnown = simpleSource.isCalibrationKnown(this.signalIndex);

                boolean onlyReadInCanBeUsed = (calibrated && !readInCalibrationKnown);
                boolean onlyUserInputCanBeUsed = (!calibrated && !readInCalibrationKnown);

                calibrationInputCanBeUsed = calibrationInputCanBeUsed || !onlyReadInCanBeUsed;
                calibrationReadInNecessary = calibrationReadInNecessary || onlyReadInCanBeUsed;
                calibrationInputNecessary = calibrationInputNecessary || onlyUserInputCanBeUsed;
                calibrationReadInCanBeUsed = calibrationReadInCanBeUsed || !onlyUserInputCanBeUsed;
            }
        }

        this.calibrationInputCanBeUsed = calibrationInputCanBeUsed;
        this.calibrationInputNecessary = calibrationInputNecessary;
        this.calibrationReadInNecessary = calibrationReadInNecessary;
        this.calibrationReadInCanBeUsed = calibrationReadInCanBeUsed;

        if(calibrationReadInNecessary)
        {
            setUseReadInCalibrationPrivate(calibrationReadInNecessary);
        }

        //if calibrationInputNecessary or calibrationReadInNecessary, then for sure calibration is not specified
        //for all source files
        if(!calibrationInputNecessary && !calibrationReadInNecessary)
        {
            this.readInCalibrationSlope = calculateUniversalReadInCalibrationSlope();
            this.readInCalibrationOffset = calculateUniversalReadInCalibrationOffset();
        }

        checkIfCalibrationInputEnabled();
        checkIfCalibrationUseReadInEnabled();

        ensureConsistencyOfReadInCalibrationSlopeUse();
        ensureConsistencyOfCalibrationValues();                    
    }    

    private void ensureConsistencyOfReadInCalibrationSlopeUse()
    {
        if(calibrationReadInNecessary)
        {
            setUseReadInCalibrationPrivate(true);
        }
        else if(!calibrationReadInCanBeUsed)
        {
            setUseReadInCalibrationPrivate(false);
        }
    }

    //we impose read in calibration slope only if useReadInCalibration is true AND 
    //EITHER (calibrationSlope is NaN OR calibration input is not necessary)
    private void ensureConsistencyOfCalibrationValues()
    {
        if(useReadInCalibration)
        {
            boolean enforceReadInCalibrationSlope = Double.isNaN(calibrationSlope) || !calibrationInputNecessary;

            if(enforceReadInCalibrationSlope)
            {
                setCalibrationSlopePrivate(readInCalibrationSlope);
            }

            boolean enforceReadInCalibrationOffset = Double.isNaN(calibrationOffset) || !calibrationInputNecessary;
            if(enforceReadInCalibrationOffset)
            {
                setCalibrationOffsetPrivate(readInCalibrationOffset);
            }
        }
    }

    public double getReadInCalibrationSlope()
    {
        return readInCalibrationSlope;
    }

    /*
     * This method return the calibration slope if all read in sources have the same known read in calibration slope,
     * and Double.NaN otherwise
     */
    private double calculateUniversalReadInCalibrationSlope()
    {
        double slope = Double.NaN;

        for(SimplePhotometricSource source : sources)
        {           
            if(source.getRecordedSignalCount() > this.signalIndex)
            {
                if(source.isCalibrationKnown(this.signalIndex))
                {
                    CalibrationSettingsImmutable calibration = source.getCalibrationSettings(this.signalIndex);
                    slope = calibration.getCalibrationSlopeInPercentsPerVolt();
                }
                else
                {
                    break;
                }
            }
        }

        //check whether all other sources have the calibration the same as the first one
        for(SimplePhotometricSource source : sources)
        { 
            if(source.getRecordedSignalCount() > this.signalIndex)
            {
                if(source.isCalibrationKnown(this.signalIndex))
                {
                    CalibrationSettingsImmutable calibration = source.getCalibrationSettings(this.signalIndex);

                    double currentSlope = calibration.getCalibrationSlopeInPercentsPerVolt();
                    if(Double.isNaN(slope) ||  Math.abs(currentSlope - slope) > TOLERANCE)
                    {
                        slope = Double.NaN;
                        break;
                    }              
                }            
                else
                {
                    slope = Double.NaN;
                    break;
                }
            }
        }

        return slope;
    }


    private double calculateUniversalReadInCalibrationOffset()
    {
        double offset = Double.NaN;

        for(SimplePhotometricSource source : sources)
        {     
            if(source.getRecordedSignalCount() > this.signalIndex)
            {
                if(source.isCalibrationKnown(this.signalIndex))
                {
                    CalibrationSettingsImmutable calibration = source.getCalibrationSettings(this.signalIndex);
                    offset = calibration.getCalibrationOffsetInVolts();
                }
                else
                {
                    break;
                }
            }
        }

        //check whether all other sources have the calibration the same as the first one
        for(SimplePhotometricSource source : sources)
        {     
            if(source.getRecordedSignalCount() > this.signalIndex)
            {
                if(source.isCalibrationKnown(this.signalIndex))
                {
                    CalibrationSettingsImmutable calibration = source.getCalibrationSettings(this.signalIndex);

                    double currentOffset = calibration.getCalibrationOffsetInVolts();
                    if(Double.isNaN(offset) || Math.abs(currentOffset - offset) > TOLERANCE)
                    {
                        offset = Double.NaN;
                        break;
                    }
                }            
                else
                {
                    offset = Double.NaN;
                    break;
                }
            }
        }

        return offset;
    }

    public boolean isCalibrationInputEnabled()
    {
        return calibrationInputEnabled;
    }

    private void checkIfCalibrationInputEnabled()
    {
        boolean calibrationSlopeInputEnabledOld = this.calibrationInputEnabled;

        boolean calibrationSlopeEnabledNew = calibrationInputCanBeUsed ? (!useReadInCalibration || calibrationInputNecessary) : false;

        this.calibrationInputEnabled = calibrationSlopeEnabledNew;
        firePropertyChange(CALIBRATION_INPUT_ENABLED, calibrationSlopeInputEnabledOld, this.calibrationInputEnabled);    
    }

    public boolean isCalibrationUseReadInEnabled()
    {
        return calibrationUseReadInEnabled;
    }

    private void checkIfCalibrationUseReadInEnabled()
    {
        boolean calibrationUseReadInEnabledOld = this.calibrationUseReadInEnabled;

        //in both cases the user could not change whether to use read-in sensitivity
        boolean calibrationUseReadInEnabledNew = calibrationReadInCanBeUsed && !calibrationReadInNecessary;

        this.calibrationUseReadInEnabled = calibrationUseReadInEnabledNew;   

        firePropertyChange(CALIBARTION_USE_READ_IN_ENABLED, calibrationUseReadInEnabledOld, this.calibrationUseReadInEnabled);
    }

    private void checkIfNonEmpty()
    {
        boolean batchNonEmptyNew = !sources.isEmpty();
        boolean batchNonEmptyOld = this.nonEmpty;

        if(batchNonEmptyNew != this.nonEmpty)
        {
            this.nonEmpty = batchNonEmptyNew;
            firePropertyChange(ProcessingBatchModelInterface.SOURCES_SELECTED, batchNonEmptyOld, batchNonEmptyNew);
        }
    }

    private void checkIfParentDirectoryChanged()
    {
        File parentDirectoryOld = this.parentDirectory;
        File parentDirectoryNew = BatchUtilities.findLastCommonSourceDirectory(sources);

        if(!Objects.equals(parentDirectoryOld, parentDirectoryNew))
        {
            this.parentDirectory = parentDirectoryNew;
            firePropertyChange(ProcessingBatchModelInterface.PARENT_DIRECTORY, parentDirectoryOld, parentDirectoryNew);
        }
    }

    private void checkIfBasicSettingsSpecified()
    {
        boolean basicSettingsSpecifiedNew = calculateBasicSettingsSpecified();
        boolean basicSettingsSpecifiedOld = basicSettingsSpecified;     

        if(basicSettingsSpecifiedNew != basicSettingsSpecified)
        {
            basicSettingsSpecified = basicSettingsSpecifiedNew;
            firePropertyChange(ProcessingBatchModelInterface.BASIC_SETTINGS_SPECIFIED, basicSettingsSpecifiedOld, basicSettingsSpecifiedNew);
        }
    }   

    private void checkIfSettingsSpecified()
    {
        boolean settingsSpecifiedNew = calculateSettingSpecified();
        boolean settingsSpecifiedOld = this.settingsSpecified;

        if(settingsSpecifiedNew != this.settingsSpecified)
        {
            this.settingsSpecified = settingsSpecifiedNew;
            firePropertyChange(ProcessingBatchModelInterface.SETTINGS_SPECIFIED, settingsSpecifiedOld, settingsSpecifiedNew);
        }   
    }

    private boolean calculateSettingSpecified()
    {
        boolean nameProvied = (batchName != null) && (!batchName.isEmpty());
        boolean basicSettingsSpecified = calculateBasicSettingsSpecified();
        boolean croppingInputProvided = isCroppingInputProvided();
        boolean smoothingInputProvided = isSmoothingInputProvided();

        boolean settingsSpecified = nameProvied && basicSettingsSpecified 
                && croppingInputProvided && smoothingInputProvided;

        return settingsSpecified;       
    }

    private boolean calculateBasicSettingsSpecified()
    {
        boolean isSensitivitySpecified = calculateCalibrationIsSpecifiedIfNecessary();

        return isSensitivitySpecified;
    }

    private boolean calculateCalibrationIsSpecifiedIfNecessary()
    {
        boolean isCalibrationSpecified =  !((Double.isNaN(calibrationSlope) || Double.isNaN(calibrationOffset)) && (calibrationInputNecessary || !useReadInCalibration));

        return isCalibrationSpecified;
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();

        properties.setProperty(SIGNAL_INDEX, Integer.toString(signalIndex));
       
        properties.setProperty(CALIBRATION_SLOPE, Double.toString(calibrationSlope));
        properties.setProperty(CALIBRATION_OFFSET, Double.toString(calibrationOffset));

        properties.setProperty(DOMAIN_IS_TO_BE_CROPPED, Boolean.toString(domainCropped));
        properties.setProperty(RANGE_IS_TO_BE_CROPPED, Boolean.toString(rangeCropped));
        properties.setProperty(LEFT_CROPPING, Double.toString(leftCropping));
        properties.setProperty(RIGHT_CROPPING, Double.toString(rightCropping));
        properties.setProperty(UPPER_CROPPING, Double.toString(upperCropping));
        properties.setProperty(LOWER_CROPPING, Double.toString(lowerCropping));

        properties.setProperty(CURVE_SMOOTHED, Boolean.toString(smoothed));
        properties.setProperty(LOESS_SPAN, Double.toString(loessSpan));
        if(loessIterations != null){properties.setProperty(LOESS_ITERATIONS,loessIterations.toString());}
        if(savitzkyDegree != null){properties.setProperty(SAVITZKY_DEGREE, savitzkyDegree.toString());}
        properties.setProperty(SAVITZKY_SPAN, Double.toString(savitzkySpan));
        properties.setProperty(PLOT_RECORDED_CURVE, Boolean.toString(plotRecordedCurve));
        properties.setProperty(PLOT_DERIVATIVE, Boolean.toString(plotDerivativeCurve));

        properties.setProperty(DERIVATIVE_SPAN, Double.toString(derivativeSpan));
        properties.setProperty(DERIVATIVE_SPAN_TYPE, derivativeSpanType.getIdentifier());
        properties.setProperty(DERIVATIVE_SPAN_GEOMETRY, derivativeSpanGeometry.getIdentifier()) ;
        properties.setProperty(DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT, Integer.toString(derivativeRobustnessIterationsCount));
        properties.setProperty(DERIVATIVE_WEIGHT_FUNCTION, derivativeWeightFunction.toString());
        properties.setProperty(DERIVATIVE_POLYNOMIAL_DEGREE, Integer.toString(derivativePolynomialDegree));

        return properties;
    } 

    public void loadProperties(Properties properties)
    {
        int signalIndex = FileInputUtilities.parseSafelyInt(properties.getProperty(SIGNAL_INDEX), this.signalIndex);
        if(signalIndex >= 0 && signalIndex <= maxSignalIndex)
        {
            setSignalIndex(signalIndex);
        }
        
        double calibrationSlope = FileInputUtilities.parseSafelyDouble(properties.getProperty(CALIBRATION_SLOPE), this.calibrationSlope);
        setCalibrationSlope(calibrationSlope);

        double calibrationOffset = FileInputUtilities.parseSafelyDouble(properties.getProperty(CALIBRATION_OFFSET), this.calibrationOffset);
        setCalibrationOffset(calibrationOffset);

        boolean domainTrimmed = FileInputUtilities.parseSafelyBoolean(properties.getProperty(DOMAIN_IS_TO_BE_CROPPED), this.domainCropped);
        setDomainCropped(domainTrimmed);

        boolean rangeTrimmed = FileInputUtilities.parseSafelyBoolean(properties.getProperty(RANGE_IS_TO_BE_CROPPED), this.rangeCropped);
        setRangeCropped(rangeTrimmed);

        double leftTrimming = FileInputUtilities.parseSafelyDouble(properties.getProperty(LEFT_CROPPING), this.leftCropping);
        setLeftCropping(leftTrimming);

        double rightTrimming = FileInputUtilities.parseSafelyDouble(properties.getProperty(RIGHT_CROPPING), this.rightCropping);
        setRightCropping(rightTrimming);

        double upperTrimming = FileInputUtilities.parseSafelyDouble(properties.getProperty(UPPER_CROPPING), this.upperCropping);
        setUpperCropping(upperTrimming);

        double lowerTrimming = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOWER_CROPPING), this.lowerCropping);
        setLowerCropping(lowerTrimming);

        boolean smoothed = FileInputUtilities.parseSafelyBoolean(properties.getProperty(CURVE_SMOOTHED), this.smoothed);
        setDataSmoothed(smoothed);

        double loessSpan = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOESS_SPAN), this.loessSpan);
        setLoessSpan(loessSpan);

        Number loessIterations = FileInputUtilities.parseSafelyDouble(properties.getProperty(LOESS_ITERATIONS));
        setLoessIterations(loessIterations);

        Number savitzkyDegree = FileInputUtilities.parseSafelyDouble(properties.getProperty(SAVITZKY_DEGREE));
        setSavitzkyDegree(savitzkyDegree);

        double savitzkySpan = FileInputUtilities.parseSafelyDouble(properties.getProperty(SAVITZKY_SPAN), this.savitzkySpan);
        setSavitzkySpan(savitzkySpan);

        boolean ploRecordedCurve = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_RECORDED_CURVE), this.plotRecordedCurve);
        setPlotRecordedCurve(ploRecordedCurve);

        boolean plotDerivativeCurve = FileInputUtilities.parseSafelyBoolean(properties.getProperty(PLOT_DERIVATIVE), this.plotDerivativeCurve);
        setPlotDerivativeCurve(plotDerivativeCurve);

        String derivativeSpanProperty = properties.getProperty(DERIVATIVE_SPAN);
        if(FileInputUtilities.canBeParsedToDouble(derivativeSpanProperty))
        {
            double derivativeSpanNew = FileInputUtilities.parseSafelyDouble(properties.getProperty(DERIVATIVE_SPAN), this.derivativeSpan);
            setDerivativeSpan(derivativeSpanNew);
        }

        String derivativeSpanTypeProperty = properties.getProperty(DERIVATIVE_SPAN_TYPE);
        if(SpanType.canBeParsed(derivativeSpanTypeProperty))
        {
            SpanType derivativeSpanTypeNew = SpanType.getValue(derivativeSpanTypeProperty, this.derivativeSpanType);
            setDerivativeSpanType(derivativeSpanTypeNew);
        }

        String derivativeSpanGeometryProperty = properties.getProperty(DERIVATIVE_SPAN_GEOMETRY);
        if(SpanGeometry.canBeParsed(derivativeSpanGeometryProperty))
        {
            SpanGeometry derivativeSpanGeometryNew = SpanGeometry.getValue(derivativeSpanGeometryProperty, this.derivativeSpanGeometry);
            setDerivativeSpanGeometry(derivativeSpanGeometryNew);
        }

        String derivativeRobustnessIterationsCountProperty = properties.getProperty(DERIVATIVE_ROBUSTNESS_ITERATIONS_COUNT);
        if(FileInputUtilities.canBeParsedToInteger(derivativeRobustnessIterationsCountProperty))
        {
            int derivativeRobustnessIterationsCountNew = FileInputUtilities.parseSafelyInt(derivativeRobustnessIterationsCountProperty, this.derivativeRobustnessIterationsCount);
            setDerivativeRobustnessIterationsCount(derivativeRobustnessIterationsCountNew);
        }

        String derivativeWeightFunctionProperty = properties.getProperty(DERIVATIVE_WEIGHT_FUNCTION);
        if(LocalRegressionWeightFunction.canBeParsed(derivativeWeightFunctionProperty))
        {
            LocalRegressionWeightFunction derivativeWeightFunctionNew = LocalRegressionWeightFunction.getValue(derivativeWeightFunctionProperty, this.derivativeWeightFunction);
            setDerivativeWeightFunction(derivativeWeightFunctionNew);
        }

        String derivativePolynomialDegreeProperty = properties.getProperty(DERIVATIVE_POLYNOMIAL_DEGREE);
        if(FileInputUtilities.canBeParsedToInteger(derivativePolynomialDegreeProperty))
        {
            int derivativePolynomialDegreeNew = FileInputUtilities.parseSafelyInt(derivativePolynomialDegreeProperty, this.derivativePolynomialDegree);
            setDerivativePolynomialDegree(derivativePolynomialDegreeNew);
        }
    }

    private void flushPreferences()
    {
        try
        {
            PREF.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }
}
