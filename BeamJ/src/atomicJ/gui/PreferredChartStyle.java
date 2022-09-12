package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.ANTIALIASING;
import static atomicJ.gui.PreferenceKeys.ASPECT_RATIO_LOCKED;
import static atomicJ.gui.PreferenceKeys.BACKGROUND_PAINT;
import static atomicJ.gui.PreferenceKeys.CHART_PADDING_BOTTOM;
import static atomicJ.gui.PreferenceKeys.CHART_PADDING_LEFT;
import static atomicJ.gui.PreferenceKeys.CHART_PADDING_RIGHT;
import static atomicJ.gui.PreferenceKeys.CHART_PADDING_TOP;

import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


import org.jfree.chart.JFreeChart;
import org.jfree.ui.RectangleInsets;

import atomicJ.utilities.SerializationUtilities;

public class PreferredChartStyle implements PreferenceChangeListener
{
    private double paddingTop;
    private double paddingBottom;
    private double paddingLeft;
    private double paddingRight;
    private RectangleInsets paddingInsets;

    private Paint backgroundPaint;
    private boolean antialias;

    private boolean lockAspectRatio;

    private final boolean defaultLockAspect;
    private final Preferences pref;

    private static Map<String, PreferredChartStyle> instances = new LinkedHashMap<>();

    public PreferredChartStyle(Preferences pref, boolean defaultLockAspect)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.defaultLockAspect = defaultLockAspect;

        this.paddingTop = pref.getDouble(CHART_PADDING_TOP, 0);
        this.paddingBottom = pref.getDouble(CHART_PADDING_BOTTOM, 0);
        this.paddingLeft = pref.getDouble(CHART_PADDING_LEFT, 0);
        this.paddingRight = pref.getDouble(CHART_PADDING_RIGHT, 0);

        refreshPaddings();

        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, JFreeChart.DEFAULT_BACKGROUND_PAINT);
        this.antialias = pref.getBoolean(ANTIALIASING, true);

        this.lockAspectRatio = pref.getBoolean(ASPECT_RATIO_LOCKED, defaultLockAspect);
    }

    public static PreferredChartStyle getInstance(Preferences pref, boolean defaultLockAspect) 
    {
        String key = pref.absolutePath() + Boolean.toString(defaultLockAspect);
        PreferredChartStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredChartStyle(pref, defaultLockAspect);
            instances.put(key, style);
        }

        return style;    
    };


    public double paddingTop()
    {
        return paddingTop;
    }

    public double paddingBottom()
    {
        return paddingBottom;
    }

    public double paddingLeft()
    {
        return paddingLeft;
    }

    public double paddingRight()
    {
        return paddingRight;
    }

    public RectangleInsets paddingInsets()
    {
        return paddingInsets;
    }

    public Paint backgroundPaint()
    {
        return backgroundPaint;
    }

    public boolean antialias()
    {
        return antialias;
    }

    public boolean lockAspectRatio()
    {
        return lockAspectRatio;
    }

    private void refreshPaddings()
    {
        boolean paddingPresent = paddingTop + paddingBottom + paddingLeft + paddingRight > 0.001;
        this.paddingInsets = paddingPresent ? new RectangleInsets(paddingTop, paddingLeft, paddingBottom, paddingRight) : null;       
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        String key = evt.getKey();

        if(CHART_PADDING_TOP.equals(key))
        {
            this.paddingTop = pref.getDouble(CHART_PADDING_TOP, 0);
            refreshPaddings();
        }
        else if(CHART_PADDING_BOTTOM.equals(key))
        {
            this.paddingBottom = pref.getDouble(CHART_PADDING_BOTTOM, 0);
            refreshPaddings();
        }
        else if(CHART_PADDING_LEFT.equals(key))
        {
            this.paddingLeft = pref.getDouble(CHART_PADDING_LEFT, 0);
            refreshPaddings();
        }
        else if(CHART_PADDING_RIGHT.equals(key))
        {
            this.paddingRight = pref.getDouble(CHART_PADDING_RIGHT, 0);
            refreshPaddings();
        }
        else if(BACKGROUND_PAINT.equals(key))
        {
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, JFreeChart.DEFAULT_BACKGROUND_PAINT);
        }
        else if(ANTIALIASING.equals(key))
        {
            this.antialias = pref.getBoolean(ANTIALIASING, true);
        }
        else if(ASPECT_RATIO_LOCKED.equals(key))
        {
            this.lockAspectRatio = pref.getBoolean(ASPECT_RATIO_LOCKED, defaultLockAspect);
        }
    }
}
