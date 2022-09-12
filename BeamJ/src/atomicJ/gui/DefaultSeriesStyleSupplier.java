
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

package atomicJ.gui;

import java.awt.*;
import java.util.Hashtable;
import java.util.Map;

import chloroplastInterface.LightSignalType;

public class DefaultSeriesStyleSupplier implements SeriesStyleSupplier
{
    private static final DefaultSeriesStyleSupplier INSTANCE = new DefaultSeriesStyleSupplier();

    private static final Stroke DEFAULT_STROKE = new BasicStroke(2.f);

    private final Map<String, Paint[]> paintArrays = new Hashtable<>();
    private final Map<String, Paint> paints = new Hashtable<>();
    private final Map<String, Integer> markerSizes = new Hashtable<>();
    private final Map<String, Integer> markerIndices = new Hashtable<>();
    private final Map<String, Boolean> markerVisibilities = new Hashtable<>();
    private final Map<String, Boolean> lineVisibilities = new Hashtable<>();
    private final Map<String, Stroke> lineStrokes = new Hashtable<>();

    private  DefaultSeriesStyleSupplier()
    {
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(0), new Color(153, 0, 102));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(1), new Color(51, 153, 0));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(2), new Color(153, 0, 102));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(3), new Color(51, 153, 0));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(4), new Color(51, 204, 0));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(5), new Color(230, 10, 5));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(6), new Color(38, 117, 48));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(7), new Color(245, 0, 16));
        paints.put(LightSignalType.TRANSMITTANCE.getIdentifierForChannel(8), new Color(245, 0, 16));

        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(0), new Color(153, 0, 0));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(1), new Color(255, 0, 0));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(2), new Color(175, 175, 21));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(3), new Color(21, 177, 210));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(4), new Color(21, 177, 210));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(5), new Color(21, 177, 210));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(6), new Color(21, 177, 210));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(7), new Color(255, 153, 0));
        paints.put(LightSignalType.REFLECTANCE.getIdentifierForChannel(8), new Color(245, 0, 16));
    }

    public static DefaultSeriesStyleSupplier getSupplier()
    {
        return INSTANCE;
    }

    @Override
    public Paint getDefaultMarkerPaint(StyleTag key)
    {
        if(key instanceof IndexedStyleTag)
        {
            IndexedStyleTag indexedStyle = (IndexedStyleTag)key;
            return getDefaultMarkerPaint(indexedStyle.getInitialStyleKey(), indexedStyle.getIndex());
        }

        Paint paint = paints.get(key.getInitialStyleKey());
        return (paint != null) ? paint : Color.blue;
    }

    private Paint getDefaultMarkerPaint(Object key, int index)
    {      
        Paint[] paints = paintArrays.get(key);
        Paint paint = (paints != null) ? paints[index % paints.length] : Color.blue;       
        return paint;
    }

    @Override
    public int getDefaultMarkerIndex(StyleTag key) 
    {
        Integer index = markerIndices.get(key.getInitialStyleKey());

        return (index != null) ? index : 0;
    }

    @Override
    public int getDefaultMarkerSize(StyleTag key) 
    {
        Integer size = markerSizes.get(key.getInitialStyleKey());

        return (size != null) ? size : 4;
    }

    @Override
    public boolean getDefaultMarkersVisible(StyleTag key)
    {
        Boolean show = markerVisibilities.get(key.getInitialStyleKey());

        return (show != null) ? show : false;
    }

    @Override
    public boolean getDefaultJoiningLineVisible(StyleTag key)
    {
        Boolean show = lineVisibilities.get(key.getInitialStyleKey());

        return (show != null) ? show : true;
    }

    @Override
    public Stroke getDefaultJoiningLineStroke(StyleTag key)
    {
        Stroke stroke = lineStrokes.get(key.getInitialStyleKey());

        return (stroke != null) ? stroke : DEFAULT_STROKE;
    }
}
