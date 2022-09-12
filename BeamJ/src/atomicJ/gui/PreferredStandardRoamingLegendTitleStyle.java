package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.LEGEND_BACKGROUND_PAINT;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredStandardRoamingLegendTitleStyle extends PreferredRoamingTitleLegendStyle
{
    private Paint backgroundPaint;

    private static Map<String, PreferredStandardRoamingLegendTitleStyle> instances = new LinkedHashMap<>();

    public PreferredStandardRoamingLegendTitleStyle(Preferences pref, String styleKey) 
    {
        super(pref, styleKey);

        Paint defaultBackgoundPaint = Color.white;  
        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
    }

    public static PreferredStandardRoamingLegendTitleStyle getInstance(Preferences pref, String styleKey)
    {
        String key = pref.absolutePath() + styleKey;
        PreferredStandardRoamingLegendTitleStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredStandardRoamingLegendTitleStyle(pref, styleKey);
            instances.put(key, style);
        }

        return style;
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);
        String key = evt.getKey();

        if(LEGEND_BACKGROUND_PAINT.equals(key))
        {
            Paint defaultBackgoundPaint = Color.white;  
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(getPreferences(), LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
        }      
    }
}
