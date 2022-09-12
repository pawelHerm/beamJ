
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

public class ProcessingSettings extends ProcessingSettingsBasic
{	
    private final Channel1DDataTransformation derivativeTransformation;
    private final double offset;

    protected ProcessingSettings(BuilderPhotometric builder)
    {
        super(builder);
        this.derivativeTransformation = builder.derivativeTransformation;
        this.offset = builder.offset;
    }

    public double getOffset()
    {
        return offset;
    }

    public Channel1DDataTransformation getDerivativeTransformation()
    {
        return derivativeTransformation;
    }

    public static class BuilderPhotometric extends Builder
    {
        private final Channel1DDataTransformation derivativeTransformation;
        private final double offset;

        public BuilderPhotometric(double slope, double offset, Channel1DDataTransformation derivativeTransformation)
        {
            super(slope);
            this.offset = offset;
            this.derivativeTransformation = derivativeTransformation;
        }

        @Override
        public ProcessingSettings build()
        {
            return new ProcessingSettings(this);
        }
    }
}
