
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

import static atomicJ.gui.PreferenceKeys.AXIS_LABEL_BOTTOM_SPACE;
import static atomicJ.gui.PreferenceKeys.AXIS_LABEL_TOP_SPACE;
import static atomicJ.gui.PreferenceKeys.AXIS_LINE_PAINT;
import static atomicJ.gui.PreferenceKeys.AXIS_LINE_STROKE;
import static atomicJ.gui.PreferenceKeys.AXIS_LINE_VISIBLE;
import static atomicJ.gui.PreferenceKeys.AXIS_LOCATION;
import static atomicJ.gui.PreferenceKeys.LABEL_FONT;
import static atomicJ.gui.PreferenceKeys.LABEL_PAINT;
import static atomicJ.gui.PreferenceKeys.TICK_LABELS_VISIBLE;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_DECIMAL_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_FONT;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_GROUPING_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_GROUPING_USED;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_PAINT;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_TRAILING_ZEROES;
import static atomicJ.gui.PreferenceKeys.TICK_LABEL_VERTICAL;
import static atomicJ.gui.PreferenceKeys.TICK_MARKS_VISIBLE;
import static atomicJ.gui.PreferenceKeys.TICK_MARK_LENGTH_INSIDE;
import static atomicJ.gui.PreferenceKeys.TICK_MARK_LENGTH_OUTSIDE;
import static atomicJ.gui.PreferenceKeys.TICK_MARK_PAINT;
import static atomicJ.gui.PreferenceKeys.TICK_MARK_STROKE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.AxisLocation;

import atomicJ.gui.units.LightweightTickUnits;
import atomicJ.utilities.SerializationUtilities;

public class PreferredAxisStyle implements PreferenceChangeListener
{
    private char groupingSeparator;         
    private char decimalSeparator;          
    private boolean showTrailingZeroes;
    private boolean groupingUsed;     

    private Paint labelPaint;
    private Font labelFont;

    private Paint tickLabelPaint;
    private Font tickLabelFont;

    private Paint axisLinePaint;
    private Stroke axisLineStroke;

    private Paint tickMarkPaint;
    private Stroke tickMarkStroke;

    private AxisLocation location;

    private float tickMarkLengthInside;
    private float tickMarkLengthOutside;

    private double labelOuterSpace;
    private double labelInnerSpace;
    private boolean axisLineVisible;
    private boolean tickLabelsVisible;
    private boolean tickMarksVisible;
    private boolean verticalTickLabel;

    private final Preferences pref;

    private static Map<String, PreferredAxisStyle> instances = new LinkedHashMap<>();

    public PreferredAxisStyle(Preferences pref)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.groupingSeparator = (char) pref.getInt(TICK_LABEL_GROUPING_SEPARATOR, ' ');         
        this.decimalSeparator = (char) pref.getInt(TICK_LABEL_DECIMAL_SEPARATOR, '.');          
        this.showTrailingZeroes = pref.getBoolean(TICK_LABEL_TRAILING_ZEROES, true);
        this.groupingUsed = pref.getBoolean(TICK_LABEL_GROUPING_USED, false);        

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, LABEL_FONT, new Font("SansSerif", Font.BOLD, 14));

        this.tickLabelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_PAINT, Color.black);
        this.tickLabelFont = (Font)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_FONT, new Font("SansSerif", Font.BOLD, 14));

        this.axisLinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, AXIS_LINE_PAINT, Color.black);
        this.axisLineStroke = SerializationUtilities.getStroke(pref, AXIS_LINE_STROKE, new BasicStroke(1.0f));

        this.tickMarkPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TICK_MARK_PAINT, Color.black);
        this.tickMarkStroke = SerializationUtilities.getStroke(pref, TICK_MARK_STROKE, new BasicStroke(1.0f));

        this.location = (AxisLocation)SerializationUtilities.getSerializableObject(pref, AXIS_LOCATION, AxisLocation.BOTTOM_OR_LEFT);

        this.tickMarkLengthInside = pref.getFloat(TICK_MARK_LENGTH_INSIDE, 0f);
        this.tickMarkLengthOutside = pref.getFloat(TICK_MARK_LENGTH_OUTSIDE, 1.2f);

        this.labelOuterSpace = pref.getDouble(AXIS_LABEL_TOP_SPACE,3.0);
        this.labelInnerSpace = pref.getDouble(AXIS_LABEL_BOTTOM_SPACE, 3.0);
        this.axisLineVisible = pref.getBoolean(AXIS_LINE_VISIBLE, true);
        this.tickLabelsVisible = pref.getBoolean(TICK_LABELS_VISIBLE, true);
        this.tickMarksVisible = pref.getBoolean(TICK_MARKS_VISIBLE, true);
        this.verticalTickLabel = pref.getBoolean(TICK_LABEL_VERTICAL, false);
    }

    public static PreferredAxisStyle getInstance(Preferences pref) 
    {
        String key = pref.absolutePath();
        PreferredAxisStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredAxisStyle(pref);
            instances.put(key, style);
        }

        return style;    
    };

    public LightweightTickUnits buildTickUnits(double prefixScalingFactor)
    {
        LightweightTickUnits tickUnits = new LightweightTickUnits();
        tickUnits.setConversionFactor(prefixScalingFactor);
        tickUnits.setTickLabelDecimalSeparator(decimalSeparator);
        tickUnits.setTickLabelGroupingSeparator(groupingSeparator);
        tickUnits.setTickLabelGroupingUsed(groupingUsed);
        tickUnits.setTickLabelShowTrailingZeroes(showTrailingZeroes);

        return tickUnits;
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public char getGroupingSeparator()
    {
        return groupingSeparator;
    };

    public char getDecimalSeparator()
    {
        return decimalSeparator;
    };

    public boolean getShowTrailingZeroes()
    {
        return showTrailingZeroes;
    };

    public boolean getGroupingUsed()
    {
        return groupingUsed;
    };

    public Paint getLabelPaint()
    {
        return labelPaint;
    };

    public Font getLabelFont()
    {
        return labelFont;
    };

    public Paint getTickLabelPaint()
    {
        return tickLabelPaint;
    };

    public Font getTickLabelFont()
    {
        return tickLabelFont;
    };

    public Paint getAxisLinePaint()
    {
        return axisLinePaint;
    };

    public Stroke getAxisLineStroke()
    {
        return axisLineStroke;
    };

    public Paint getTickMarkPaint()
    {
        return tickMarkPaint;
    };

    public Stroke getTickMarkStroke()
    {
        return tickMarkStroke;
    };

    public AxisLocation getLocation()
    {
        return location;
    };

    public float getTickMarkLengthInside()
    {
        return tickMarkLengthInside;
    };

    public float getTickMarkLengthOutside()
    {
        return tickMarkLengthOutside;
    };

    public double getLabelOuterSpace()
    {
        return labelOuterSpace;
    };

    public double getLabelInnerSpace()
    {
        return labelInnerSpace;
    };

    public boolean getAxisLineVisible()
    {
        return axisLineVisible;
    };

    public boolean getTickLabelsVisible()
    {
        return tickLabelsVisible;
    };

    public boolean getTickMarksVisible()
    {
        return tickMarksVisible;
    };

    public boolean getVerticalTickLabel()
    {
        return verticalTickLabel;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(TICK_LABEL_GROUPING_SEPARATOR.equals(key))
        {
            this.groupingSeparator = (char) pref.getInt(TICK_LABEL_GROUPING_SEPARATOR, ' ');         
        }
        else if(TICK_LABEL_DECIMAL_SEPARATOR.equals(key))
        {
            this.decimalSeparator = (char) pref.getInt(TICK_LABEL_DECIMAL_SEPARATOR, '.');          
        }
        else if(TICK_LABEL_TRAILING_ZEROES.equals(key))
        {
            this.showTrailingZeroes = pref.getBoolean(TICK_LABEL_TRAILING_ZEROES, true);
        }
        else if(TICK_LABEL_GROUPING_USED.equals(key))
        {
            this.groupingUsed = pref.getBoolean(TICK_LABEL_GROUPING_USED, false);        
        }
        else if(TICK_LABEL_PAINT.equals(key))
        {
            this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_PAINT, Color.black);
        }
        else if(TICK_LABEL_PAINT.equals(key))
        {
            this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_PAINT, new Font("SansSerif", Font.BOLD, 14));          
        }
        else if(TICK_LABEL_PAINT.equals(key))
        {
            this.tickLabelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_PAINT, Color.black);
        }else if(TICK_LABEL_FONT.equals(key))
        {
            this.tickLabelFont = (Font)SerializationUtilities.getSerializableObject(pref, TICK_LABEL_FONT, new Font("SansSerif", Font.BOLD, 14));
        }
        else if(AXIS_LINE_PAINT.equals(key))
        {
            this.axisLinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, AXIS_LINE_PAINT, Color.black);
        }
        else if(AXIS_LINE_STROKE.equals(key))
        {
            this.axisLineStroke = SerializationUtilities.getStroke(pref, AXIS_LINE_STROKE, new BasicStroke(1.0f));

        }else if(TICK_MARK_PAINT.equals(key))
        {
            this.tickMarkPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TICK_MARK_PAINT, Color.black);
        }
        else if(TICK_MARK_STROKE.equals(key))
        {
            this.tickMarkStroke = SerializationUtilities.getStroke(pref, TICK_MARK_STROKE, new BasicStroke(1.0f));
        }
        else if(AXIS_LOCATION.equals(key))
        {
            this.location = (AxisLocation)SerializationUtilities.getSerializableObject(pref, AXIS_LOCATION, AxisLocation.BOTTOM_OR_LEFT);
        }
        else if(TICK_MARK_LENGTH_INSIDE.equals(key))
        {
            this.tickMarkLengthInside = pref.getFloat(TICK_MARK_LENGTH_INSIDE, 0f);
        }
        else if(TICK_MARK_LENGTH_OUTSIDE.equals(key))
        {
            this.tickMarkLengthOutside = pref.getFloat(TICK_MARK_LENGTH_OUTSIDE, 1.2f);
        }
        else if(AXIS_LABEL_TOP_SPACE.equals(key))
        {
            this.labelOuterSpace = pref.getDouble(AXIS_LABEL_TOP_SPACE,3.0);
        }
        else if(AXIS_LABEL_BOTTOM_SPACE.equals(key))
        {
            this.labelInnerSpace = pref.getDouble(AXIS_LABEL_BOTTOM_SPACE, 3.0);
        }
        else if(AXIS_LINE_VISIBLE.equals(key))
        {
            this.axisLineVisible = pref.getBoolean(AXIS_LINE_VISIBLE, true);
        }
        else if(TICK_LABELS_VISIBLE.equals(key))
        {
            this.tickLabelsVisible = pref.getBoolean(TICK_LABELS_VISIBLE, true);
        }
        else if(TICK_MARKS_VISIBLE.equals(key))
        {
            this.tickMarksVisible = pref.getBoolean(TICK_MARKS_VISIBLE, true);
        }
        else if(TICK_LABEL_VERTICAL.equals(key))
        {
            this.verticalTickLabel = pref.getBoolean(TICK_LABEL_VERTICAL, false);
        }
    };
}