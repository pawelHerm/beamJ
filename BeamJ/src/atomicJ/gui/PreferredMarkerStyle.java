
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;

public class PreferredMarkerStyle extends AbstractModel implements Cloneable, Serializable, PreferenceChangeListener
{
    private static final long serialVersionUID = 1L;

    public static final Stroke SOLID_STROKE = new BasicStroke(2.5f);
    public static final Stroke DASHED_STROKE = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {8.f, 5.f}, 0.0f);

    public static final String MARKER_VISIBLE = "MarkerVisible";	
    public static final String MARKER_LABEL_VISIBLE = "MarkerLabelVisible";
    public static final String MARKER_LABEL_FONT = "MarkerLabelFont";
    public static final String MARKER_LABEL_PAINT = "MarkerLabelPaint";
    public static final String MARKER_ALPHA = "MarkerAlpha";
    public static final String MARKER_PAINT = "MarkerPaint";
    public static final String MARKER_STROKE = "MarkerStroke";
    public static final String MARKER_STYLE_COMPLETELY_CHANGED = "MarkerStyleCompletelyChanged";

    private final boolean visible = true; 
    private boolean labelVisible;     
    private Font labelFont;
    private Paint labelPaint;

    private float alpha;
    private Stroke stroke;
    private Paint paint;

    private final boolean defaultLabelVisible = true;
    private final Paint defaultPaint;
    private final Stroke defaultStroke;
    private final float defaultAlpha;

    private final Preferences pref;

    private static Map<Object, PreferredMarkerStyle> instances = new LinkedHashMap<>();

    protected PreferredMarkerStyle(Preferences pref, Paint defaultPaint, Stroke defaultStroke, float defaultAlpha) 
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.defaultPaint = defaultPaint;
        this.defaultStroke = defaultStroke;
        this.defaultAlpha = defaultAlpha;

        pullMarkerPreferences();
    }  

    public static PreferredMarkerStyle getInstance(Preferences pref)
    {
        return getInstance(pref, Color.WHITE, SOLID_STROKE, 1.f);
    }

    public static PreferredMarkerStyle getInstance(Preferences pref, Paint defaultPaint, Stroke defaultStroke, float defaultAlpha)
    {
        PreferredMarkerStyle style = instances.get(pref.absolutePath());

        if(style == null)
        {
            style = new PreferredMarkerStyle(pref, defaultPaint, defaultStroke, defaultAlpha);
            instances.put(pref.absolutePath(), style);
        }

        return style;
    }


    public Preferences getPreferences()
    {
        return pref;
    }

    private void pullMarkerPreferences()
    {    	
        Font defaultLabelFont = new Font("Dialog", Font.BOLD, 14);

        Paint defaultLabelPaint = defaultPaint;

        this.stroke = SerializationUtilities.getStroke(pref, MARKER_STROKE, defaultStroke);
        this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, MARKER_PAINT, defaultPaint);
        this.alpha = pref.getFloat(MARKER_ALPHA, defaultAlpha);

        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, MARKER_LABEL_FONT, defaultLabelFont);
        this.labelVisible =  pref.getBoolean(MARKER_LABEL_VISIBLE, defaultLabelVisible);
        this.labelPaint  = (Paint)SerializationUtilities.getSerializableObject(pref, MARKER_LABEL_PAINT, defaultLabelPaint);
    }

    public boolean isVisible()
    {
        return visible;
    }

    public Paint getPaint()
    {
        return paint;
    }

    public float getAlpha()
    {
        return alpha;
    }

    public Stroke getStroke()
    {
        return stroke;
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public Paint getLabelPaint()
    {
        return labelPaint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(MARKER_LABEL_FONT.equals(key))
        {
            Font defaultLabelFontFinished = new Font("Dialog", Font.BOLD, 14);
            this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, MARKER_LABEL_FONT, defaultLabelFontFinished);
        }
        else if(MARKER_LABEL_VISIBLE.equals(key))
        {
            boolean defaultLabelVisible = true;
            this.labelVisible = pref.getBoolean(MARKER_LABEL_VISIBLE, defaultLabelVisible);
        }
        else if(MARKER_LABEL_PAINT.equals(key))
        {
            Paint defaultLabelFinishedStandardPaint = defaultPaint;
            this.labelPaint  = (Paint)SerializationUtilities.getSerializableObject(pref, MARKER_LABEL_PAINT, defaultLabelFinishedStandardPaint);
        }
        else if(MARKER_ALPHA.equals(key))
        {
            this.alpha = pref.getFloat(MARKER_ALPHA, defaultAlpha);
        }
        else if(MARKER_STROKE.equals(key))
        {
            Stroke defaultFinishedStandardStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            this.stroke = SerializationUtilities.getStroke(pref, MARKER_STROKE, defaultFinishedStandardStroke);
        }   
        else if(MARKER_PAINT.equals(key))
        {
            Paint defaultFinishedStandardPaint = defaultPaint;
            this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, MARKER_PAINT, defaultFinishedStandardPaint);
        }
    };
}
