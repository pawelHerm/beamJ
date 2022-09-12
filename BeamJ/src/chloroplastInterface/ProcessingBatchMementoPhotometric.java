
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

import atomicJ.curveProcessing.SpanType;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.curveProcessing.SmootherType;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public class ProcessingBatchMementoPhotometric extends AbstractModel
{
    private final double calibrationSlope;	
    private final double calibrationOffset;

    private final boolean useReadInCalibration;
    private final double lowerCropping;
    private final double upperCropping;
    private final double rightCropping;
    private final double leftCropping;	
    private final boolean domainCropped;
    private final boolean rangeCropped;	

    private final SmootherType smootherType;
    private final boolean smoothed;
    private final double loessSpan; 
    private final Number loessIterations;
    private final Number savitzkyDegree;
    private final double savitzkySpan;

    private final int derivativePolynomialDegree;
    private final double derivativeSpan;
    private final SpanType derivativeSpanType;
    private final SpanGeometry derivativeSpanGeometry;
    private final int derivativeRobustnessIterationsCount;
    private final LocalRegressionWeightFunction derivativeWeightFunction;

    private final boolean plotRecordedCurve;
    private final boolean plotIndentation;

    private final ResultDestinationPhotometric destination;

    private final String batchName;
    private final int batchNumber;

    public ProcessingBatchMementoPhotometric(ProcessingBatchModel model)
    {
        this.batchName = model.getBatchName();
        this.batchNumber = model.getBatchNumber();

        this.calibrationSlope = model.getCalibrationSlope(); 
        this.calibrationOffset = model.getCalibrationOffset();

        this.useReadInCalibration = model.getUseReadInCalibration();
        this.lowerCropping = model.getLowerCropping();
        this.upperCropping = model.getUpperCropping();
        this.rightCropping = model.getRightCropping();
        this.leftCropping = model.getLeftCropping();    
        this.domainCropped = model.isDomainCropped();
        this.rangeCropped = model.isRangeTrimmed();   

        this.smootherType = model.getSmootherType();
        this.smoothed = model.areDataSmoothed();
        this.loessSpan = model.getLoessSpan(); 
        this.loessIterations = model.getLoessIterations();
        this.savitzkyDegree = model.getSavitzkyDegree();
        this.savitzkySpan = model.getSavitzkySpan();

        this.derivativePolynomialDegree = model.getDerivativePolynomialDegree();
        this.derivativeSpan = model.getDerivativeSpan();
        this.derivativeSpanType = model.getDerivativeSpanType();
        this.derivativeSpanGeometry = model.getDerivativeSpanGeometry();
        this.derivativeRobustnessIterationsCount = model.getDerivativeRobustnessIterationsCount();
        this.derivativeWeightFunction = model.getDerivativeWeightFunction();

        this.plotRecordedCurve = model.isPlotRecordedCurve();
        this.plotIndentation = model.isPlotDerivativeCurve();

        this.destination = model.getResultDestination();

        //        this.batchName = this.model.get;
        //        this.destination = this.model.getR;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public int getBatchNumber()
    {
        return batchNumber;
    }

    public ResultDestinationPhotometric getResultDestination()
    {
        return destination;
    }

    public double getCalibrationSlope()
    {
        return calibrationSlope; 
    }

    public double getCalibrationOffset()
    {
        return calibrationOffset;
    }

    public boolean getUseReadInCalibration()
    {
        return useReadInCalibration;
    }   

    public double getLeftCropping()
    {
        return leftCropping;
    }

    public double getRightCropping()
    {
        return rightCropping;
    }

    public double getLowerCropping()
    {
        return lowerCropping;
    }

    public double getUpperCropping()
    {
        return upperCropping;
    }

    public boolean isDomainToBeCropped()
    {
        return domainCropped; 
    }

    public boolean isRangeToBeCropped()
    {
        return rangeCropped; 
    }

    public SmootherType getSmootherName()
    {
        return smootherType;
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }

    public double getLoessSpan()
    {
        return loessSpan;
    }

    public Number getLoessIterations()
    {
        return loessIterations;
    }

    public Number getSavitzkyDegree()
    {
        return savitzkyDegree;
    }

    public double getSavitzkySpan()
    {
        return savitzkySpan;
    }

    public int getDerivativePolynomialDegree()
    {
        return derivativePolynomialDegree;
    }

    public double getDerivativeSpan()
    {
        return derivativeSpan;
    };

    public SpanType getDerivativeSpanType()
    {
        return derivativeSpanType;
    };

    public SpanGeometry getDerivativeSpanGeometry()
    {
        return derivativeSpanGeometry;
    };

    public int getDerivativeRobustnessIterationsCount()
    {
        return derivativeRobustnessIterationsCount;
    };

    public LocalRegressionWeightFunction getDerivativeWeightFunction()
    {
        return derivativeWeightFunction;
    };

    public boolean isPlotRecordedCurve()
    {
        return plotRecordedCurve;
    }

    public boolean isPlotIndentation()
    {
        return plotIndentation;
    }

}
