package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.PAINT;
import static atomicJ.gui.PreferenceKeys.SHAPE_INDEX;
import static atomicJ.gui.PreferenceKeys.SHAPE_SIZE;
import static atomicJ.gui.PreferenceKeys.SHOWN;
import static atomicJ.gui.PreferenceKeys.VISIBLE_IN_LEGEND;

import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredDiscreteSeriesRendererStyle implements PreferenceChangeListener
{
    private boolean visible;

    private int markerSize;
    private int markerIndex;

    private boolean visibleInLegend; 
    private Paint paint;

    private Channel1DRendererData rendererData;

    private final Preferences pref;

    private static final SeriesStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultSeriesStyleSupplier.getSupplier();

    private static Map<String, PreferredDiscreteSeriesRendererStyle> instances = new LinkedHashMap<>();

    public PreferredDiscreteSeriesRendererStyle(Preferences pref, StyleTag key)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(key);
        int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(key);
        Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(key);

        this.visible = pref.getBoolean(SHOWN,true);
        this.markerSize = pref.getInt(SHAPE_SIZE, defSize);
        this.markerIndex = pref.getInt(SHAPE_INDEX, defIndex);

        this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true); 
        this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, defPaint);
        this.rendererData = buildChannel1DRendererData();
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public Channel1DRendererData getChannel1DRendererData()
    {
        return rendererData;
    }


    private Channel1DRendererData buildChannel1DRendererData()
    {
        Channel1DRendererDataMutable rendererDataMutable = new Channel1DRendererDataMutable();

        rendererDataMutable.setBaseMarkerIndex(this.markerIndex);
        rendererDataMutable.setBaseMarkerSize(this.markerSize);
        rendererDataMutable.setBaseSeriesVisible(this.visible);      
        rendererDataMutable.setBaseSeriesVisibleInLegend(this.visibleInLegend);
        rendererDataMutable.setBasePaint(this.paint);
        rendererDataMutable.setBaseLinesVisible(false);
        rendererDataMutable.setBaseShapesVisible(true);

        Channel1DRendererDataImmutable rendererDataImmutable = new Channel1DRendererDataImmutable(rendererDataMutable);

        return rendererDataImmutable;
    }

    public static PreferredDiscreteSeriesRendererStyle getInstance(Preferences pref, StyleTag styleKey)
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredDiscreteSeriesRendererStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredDiscreteSeriesRendererStyle(pref, styleKey);
            instances.put(key, style);
        }

        return style;
    }

    public int getMarkerSize()
    {
        return markerSize;
    }

    public int getMarkerIndex()
    {
        return markerIndex;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean isVisibleInLegend()
    {
        return visibleInLegend;
    }

    public Paint getPaint()
    {
        return paint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(SHOWN.equals(key))
        {
            this.visible = pref.getBoolean(SHOWN,true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_SIZE.equals(key))
        {
            //            int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(key);
            this.markerSize = pref.getInt(SHAPE_SIZE, this.markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_INDEX.equals(key))
        {
            //            int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(key);
            this.markerIndex = pref.getInt(SHAPE_INDEX,  this.markerIndex);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PAINT.equals(key))
        {
            //            Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(key);
            this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, this.paint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(VISIBLE_IN_LEGEND.equals(key))
        {
            this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true); 
            this.rendererData = buildChannel1DRendererData();
        }
    }
}
