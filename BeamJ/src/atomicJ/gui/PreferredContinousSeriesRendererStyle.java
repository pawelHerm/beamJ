package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.JOINED;
import static atomicJ.gui.PreferenceKeys.MARKERS;
import static atomicJ.gui.PreferenceKeys.PAINT;
import static atomicJ.gui.PreferenceKeys.SERIES_JOINING_LINE_PAINT;
import static atomicJ.gui.PreferenceKeys.SERIES_JOINING_LINE_STROKE;
import static atomicJ.gui.PreferenceKeys.SHAPE_INDEX;
import static atomicJ.gui.PreferenceKeys.SHAPE_SIZE;
import static atomicJ.gui.PreferenceKeys.SHOWN;
import static atomicJ.gui.PreferenceKeys.VISIBLE_IN_LEGEND;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredContinousSeriesRendererStyle implements PreferenceChangeListener
{
    private boolean visible;
    private boolean visibleInLegend;
    private boolean joined;
    private boolean markers;
    private int markerSize;
    private int markerIndex;
    private Stroke stroke;
    private Shape shape;
    private Paint fillPaint;
    private Paint strokePaint;

    private Channel1DRendererData rendererData;

    private final Preferences pref;

    private static final SeriesStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultSeriesStyleSupplier.getSupplier();

    private static Map<String, PreferredContinousSeriesRendererStyle> instances = new LinkedHashMap<>();

    public PreferredContinousSeriesRendererStyle(Preferences pref, StyleTag tag)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        boolean defJoined = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineVisible(tag);
        boolean defMarkers = DEFAULT_STYLE_SUPPLIER.getDefaultMarkersVisible(tag);
        int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(tag);
        int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(tag);
        Stroke defStroke = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineStroke(tag);
        Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(tag);   

        this.visible = pref.getBoolean(SHOWN,true);
        this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true);
        this.joined = pref.getBoolean(JOINED, defJoined);
        this.markers = pref.getBoolean(MARKERS, defMarkers);
        this.markerSize = pref.getInt(SHAPE_SIZE, defSize);
        this.markerIndex = pref.getInt(SHAPE_INDEX, defIndex);
        this.stroke = SerializationUtilities.getStroke(pref, SERIES_JOINING_LINE_STROKE, defStroke);
        this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
        this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, defPaint);
        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SERIES_JOINING_LINE_PAINT, defPaint);
        this.rendererData = buildChannel1DRendererData();
    }   

    public Channel1DRendererData getChannel1DRendererData()
    {
        return rendererData;
    }

    private Channel1DRendererData buildChannel1DRendererData()
    {
        Channel1DRendererDataMutable rendererDataMutable = new Channel1DRendererDataMutable();
        Shape shape = ShapeSupplier.createShape(this.markerIndex, this.markerSize);

        rendererDataMutable.setUseFillPaint(true);
        rendererDataMutable.setDrawOutlines(false);
        rendererDataMutable.setBaseMarkerIndex(markerIndex);
        rendererDataMutable.setBaseMarkerSize(markerSize);
        rendererDataMutable.setBaseSeriesVisible(this.visible);    
        rendererDataMutable.setBaseSeriesVisibleInLegend(this.visibleInLegend);
        rendererDataMutable.setBaseShape(shape);
        rendererDataMutable.setBaseStroke(this.stroke);
        rendererDataMutable.setBaseFillPaint(this.fillPaint);
        rendererDataMutable.setBasePaint(this.strokePaint);
        rendererDataMutable.setBaseLinesVisible(this.joined);
        rendererDataMutable.setBaseShapesVisible(this.markers);

        Channel1DRendererDataImmutable rendererDataImmutable = new Channel1DRendererDataImmutable(rendererDataMutable);

        return rendererDataImmutable;
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public boolean isVisible()
    {
        return visible;
    };

    public boolean isVisibleInLegend()
    {
        return visibleInLegend;
    };

    public boolean isJoined()
    {
        return joined;
    };

    public boolean isMarkerVisible()
    {
        return markers;
    };

    public int getMarkerSize()
    {
        return markerSize;
    };

    public int getMarkerIndex()
    {
        return markerIndex;
    };

    public Stroke getStroke()
    {
        return stroke;
    };

    public Shape getShape()
    {
        return shape;
    };

    public Paint getFillPaint()
    {
        return fillPaint;
    };

    public Paint getStrokePaint()
    {
        return strokePaint;
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
        else if(VISIBLE_IN_LEGEND.equals(key))
        {
            this.visibleInLegend = pref.getBoolean(VISIBLE_IN_LEGEND, true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(JOINED.equals(key))
        {
            //            boolean defJoined = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineVisible(key);
            this.joined = pref.getBoolean(JOINED, this.joined);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(MARKERS.equals(key))
        {
            //            boolean defMarkers = DEFAULT_STYLE_SUPPLIER.getDefaultMarkersVisible(key);
            this.markers = pref.getBoolean(MARKERS, this.markers);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_SIZE.equals(key))
        {
            //            int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(key);
            this.markerSize = pref.getInt(SHAPE_SIZE, this.markerSize);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_INDEX.equals(key))
        {
            //            int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(key);
            this.markerIndex = pref.getInt(SHAPE_INDEX, this.markerIndex);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SERIES_JOINING_LINE_STROKE.equals(key))
        {
            //            Stroke defStroke = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineStroke(key);
            this.stroke = SerializationUtilities.getStroke(pref, SERIES_JOINING_LINE_STROKE, this.stroke);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PAINT.equals(key))
        {
            //            Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(key);   
            this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PAINT, this.fillPaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SERIES_JOINING_LINE_PAINT.equals(key))
        {
            //            Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(key);   
            this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SERIES_JOINING_LINE_PAINT, this.strokePaint);
            this.rendererData = buildChannel1DRendererData();
        }
    }

    public static PreferredContinousSeriesRendererStyle getInstance(Preferences pref, StyleTag styleKey) 
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredContinousSeriesRendererStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredContinousSeriesRendererStyle(pref, styleKey);
            instances.put(key, style);
        }

        return style;    
    };

}
