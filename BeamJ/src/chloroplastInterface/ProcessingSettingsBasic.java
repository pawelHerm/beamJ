
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

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.NullCurveTransformation;

public class ProcessingSettingsBasic 
{	
    private final int signalIndex;
    private final String signalTypeDescription;
    private final double sensitivity;
    private final boolean smoothed;
    private final boolean trimmed;
    private final Channel1DDataTransformation trimmer;
    private final Channel1DDataTransformation smoother;

    protected ProcessingSettingsBasic(Builder builder)
    {
        this.signalIndex = builder.signalIndex;
        this.signalTypeDescription = builder.signalTypeDescription;
        this.sensitivity = builder.sensitivity;
        this.smoothed = builder.smoothed;
        this.trimmed = builder.trimmed;
        this.trimmer = builder.trimmer;
        this.smoother = builder.smoother;
    }

    public int getSignalIndex()
    {
        return signalIndex;
    }
    
    public double getSensitivity()
    {
        return sensitivity;
    }

    public boolean areDataSmoothed()
    {
        return smoothed;
    }	

    public boolean areDataTrimmed()
    {
        return trimmed;
    }

    public Channel1DDataTransformation getSmoother()
    {
        return smoother;
    }

    public Channel1DDataTransformation getTrimmer()
    {
        return trimmer;
    }

    public static class Builder
    {
        private int signalIndex;
        private String signalTypeDescription = "";
        private final double sensitivity;
        private boolean smoothed = false;
        private boolean trimmed = false;
        private Channel1DDataTransformation trimmer = NullCurveTransformation.getInstance();
        private Channel1DDataTransformation smoother = NullCurveTransformation.getInstance();

        public Builder(double sens)
        {
            this.sensitivity = sens;
        }

        public Builder smoothed(boolean s){this.smoothed = s; return this;}

        public Builder smoother(Channel1DDataTransformation s){this.smoother = s; return this;}

        public Builder trimmed(boolean t){this.trimmed = t; return this;}

        public Builder trimmer(Channel1DDataTransformation t){this.trimmer = t; return this;}

        public Builder signalIndex(int signalIndex){this.signalIndex = signalIndex; return this;}
        
        public Builder signalIndex(String signalTypeDescription){this.signalTypeDescription = signalTypeDescription; return this;}

        public ProcessingSettingsBasic build()
        {
            return new ProcessingSettingsBasic(this);
        }
    }
}
