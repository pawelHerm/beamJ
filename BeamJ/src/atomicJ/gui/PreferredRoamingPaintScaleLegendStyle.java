package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.LEGEND_BACKGROUND_PAINT;
import static atomicJ.gui.PreferenceKeys.LEGEND_STRIP_OUTLINE_PAINT;
import static atomicJ.gui.PreferenceKeys.LEGEND_STRIP_OUTLINE_STROKE;
import static atomicJ.gui.PreferenceKeys.LEGEND_STRIP_OUTLINE_VISIBLE;
import static atomicJ.gui.PreferenceKeys.LEGEND_STRIP_WIDTH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredRoamingPaintScaleLegendStyle extends PreferredRoamingTitleLegendStyle
{
    private Paint backgroundPaint;

    private Paint stripOutlinePaint;
    private Stroke stripOutlineStroke;
    private boolean stripOutlineVisible;
    private double stripWidth;

    private static Map<String, PreferredRoamingPaintScaleLegendStyle> instances = new LinkedHashMap<>();

    public PreferredRoamingPaintScaleLegendStyle(Preferences pref, String styleKey) 
    {
        super(pref, styleKey);

        Paint defaultBackgoundPaint = new Color(0f, 0f, 0f, 0f);

        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);    
        this.stripOutlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_STRIP_OUTLINE_PAINT, Color.black);
        this.stripOutlineStroke = SerializationUtilities.getStroke(pref, LEGEND_STRIP_OUTLINE_STROKE, new BasicStroke(1.f));
        this.stripOutlineVisible = pref.getBoolean(LEGEND_STRIP_OUTLINE_VISIBLE, true);
        this.stripWidth = pref.getDouble(LEGEND_STRIP_WIDTH, 20);
    }

    public static PreferredRoamingPaintScaleLegendStyle getInstance(Preferences pref, String styleKey)
    {
        String key = pref.absolutePath() + styleKey;
        PreferredRoamingPaintScaleLegendStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredRoamingPaintScaleLegendStyle(pref, styleKey);
            instances.put(key, style);
        }

        return style;
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public Paint getStripOutlinePaint()
    {
        return stripOutlinePaint;
    }

    public Stroke getStripOutlineStroke()
    {
        return stripOutlineStroke;
    }

    public boolean isStripOutlineVisible()
    {
        return stripOutlineVisible;
    }

    public double getStripWidth()
    {
        return stripWidth;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);
        String key = evt.getKey();

        Preferences pref = getPreferences();

        if(LEGEND_BACKGROUND_PAINT.equals(key))
        {
            Paint defaultBackgoundPaint = new Color(0f, 0f, 0f, 0f);
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(getPreferences(), LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
        } 
        else if(LEGEND_STRIP_OUTLINE_PAINT.equals(key))
        {
            this.stripOutlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_STRIP_OUTLINE_PAINT, Color.black);
        }
        else if(LEGEND_STRIP_OUTLINE_STROKE.equals(key))
        {
            this.stripOutlineStroke = SerializationUtilities.getStroke(pref, LEGEND_STRIP_OUTLINE_STROKE, new BasicStroke(1.f));
        }
        else if(LEGEND_STRIP_OUTLINE_VISIBLE.equals(key))
        {
            this.stripOutlineVisible = pref.getBoolean(LEGEND_STRIP_OUTLINE_VISIBLE, true);
        }
        else if(LEGEND_STRIP_WIDTH.equals(key))
        {
            this.stripWidth = pref.getDouble(LEGEND_STRIP_WIDTH, 20);
        }
    }
}
