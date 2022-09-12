
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

package atomicJ.gui;

import java.util.prefs.Preferences;

import atomicJ.data.ProcessedPhaseCurve;
import atomicJ.data.SpectroscopyCurve;


public class PhaseCurvePlot extends Channel1DPlot
{
    private static final long serialVersionUID = 1L;

    private static final Preferences PREF = Preferences.userNodeForPackage(PhaseCurvePlot.class).node("Phase curve plot");
    private static final Preferences prefDomainScaleBar = PREF.node("DomainScaleBar");
    private static final Preferences prefRangeScaleBar = PREF.node("RangeScaleBar");

    private static final PreferredScaleBarStyle preferredDomainScaleBar = new PreferredScaleBarStyle(prefDomainScaleBar);
    private static final PreferredScaleBarStyle preferredRangeScaleBar = new PreferredScaleBarStyle(prefRangeScaleBar);


    public PhaseCurvePlot(ProcessedPhaseCurve curve)
    {	
        super(curve, PREF, preferredDomainScaleBar, preferredRangeScaleBar);
    }
}
