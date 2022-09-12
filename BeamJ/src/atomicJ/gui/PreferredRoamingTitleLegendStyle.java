package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.LEGEND_FRAME_PAINT;
import static atomicJ.gui.PreferenceKeys.LEGEND_FRAME_STROKE;
import static atomicJ.gui.PreferenceKeys.LEGEND_FRAME_VISIBLE;
import static atomicJ.gui.PreferenceKeys.LEGEND_INSIDE;
import static atomicJ.gui.PreferenceKeys.LEGEND_INSIDE_X;
import static atomicJ.gui.PreferenceKeys.LEGEND_INSIDE_Y;
import static atomicJ.gui.PreferenceKeys.LEGEND_MARGIN_BOTTOM;
import static atomicJ.gui.PreferenceKeys.LEGEND_MARGIN_LEFT;
import static atomicJ.gui.PreferenceKeys.LEGEND_MARGIN_RIGHT;
import static atomicJ.gui.PreferenceKeys.LEGEND_MARGIN_TOP;
import static atomicJ.gui.PreferenceKeys.LEGEND_OUTSIDE_POSITION;
import static atomicJ.gui.PreferenceKeys.LEGEND_VISIBLE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import atomicJ.utilities.SerializationUtilities;

public class PreferredRoamingTitleLegendStyle implements PreferenceChangeListener
{
    private boolean frameVisible;
    private Stroke frameStroke;
    private Paint framePaint;

    private boolean legendVisible;
    private boolean legendInside;
    private double legendInsideX;
    private double legendInsideY;

    private double marginTop;
    private double marginBottom;
    private double marginLeft;
    private double marginRight;

    private RectangleInsets marginInsets;
    private RectangleEdge outsidePosition;

    private final String styleKey;
    private final Preferences pref;

    private static final ChartStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultChartStyleSupplier.getSupplier();

    public PreferredRoamingTitleLegendStyle(Preferences pref, String styleKey)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.styleKey = styleKey;

        boolean defaultFrameVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendFrameVisible(styleKey);
        boolean defaultLegendVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendVisible(styleKey);
        boolean defaultLegendInside = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInside(styleKey);
        double defaultInsideX = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideX(styleKey);
        double defaultInsideY = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideY(styleKey);

        this.frameVisible = pref.getBoolean(LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        this.legendVisible = pref.getBoolean(LEGEND_VISIBLE, defaultLegendVisible);
        this.legendInside = pref.getBoolean(LEGEND_INSIDE, defaultLegendInside);
        this.legendInsideX = pref.getDouble(LEGEND_INSIDE_X, defaultInsideX);
        this.legendInsideY = pref.getDouble(LEGEND_INSIDE_Y, defaultInsideY);

        this.outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, LEGEND_OUTSIDE_POSITION, RectangleEdge.RIGHT);

        this.marginTop = pref.getDouble(LEGEND_MARGIN_TOP, 5);
        this.marginBottom = pref.getDouble(LEGEND_MARGIN_BOTTOM, 5);
        this.marginLeft = pref.getDouble(LEGEND_MARGIN_LEFT, 5);
        this.marginRight = pref.getDouble(LEGEND_MARGIN_RIGHT, 5);

        boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
        this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;

        this.framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_FRAME_PAINT, Color.black);
        this.frameStroke = SerializationUtilities.getStroke(pref, LEGEND_FRAME_STROKE, new BasicStroke(1.f));
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public String getStyleKey()
    {
        return styleKey;
    }

    public ChartStyleSupplier getSupplier()
    {
        return DEFAULT_STYLE_SUPPLIER;
    }

    public boolean frameVisible()
    {
        return frameVisible;
    };

    public Stroke frameStroke()
    {
        return frameStroke;
    };

    public Paint framePaint()
    {
        return framePaint;
    };    

    public boolean legendVisible()
    {
        return legendVisible;
    };

    public boolean legendInside()
    {
        return legendInside;
    };

    public double legendInsideX()
    {
        return legendInsideX;
    };

    double legendInsideY()
    {
        return legendInsideY;
    };

    public RectangleInsets marginInsets()
    {
        return marginInsets;
    };

    public RectangleEdge outsidePosition()
    {
        return outsidePosition;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(LEGEND_FRAME_VISIBLE.equals(key))
        {
            boolean defaultFrameVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendFrameVisible(styleKey);
            this.frameVisible = pref.getBoolean(LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        }
        else if(LEGEND_VISIBLE.equals(key))
        {
            boolean defaultLegendVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendVisible(styleKey);
            this.legendVisible = pref.getBoolean(LEGEND_VISIBLE, defaultLegendVisible);
        }
        else if(LEGEND_INSIDE.equals(key))
        {
            boolean defaultLegendInside = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInside(styleKey);
            this.legendInside = pref.getBoolean(LEGEND_INSIDE, defaultLegendInside);
        }
        else if(LEGEND_INSIDE_X.equals(key))
        {
            double defaultInsideX = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideX(styleKey);
            this.legendInsideX = pref.getDouble(LEGEND_INSIDE_X, defaultInsideX);
        }
        else if(LEGEND_INSIDE_Y.equals(key))
        {
            double defaultInsideY = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideY(styleKey);
            this.legendInsideY = pref.getDouble(LEGEND_INSIDE_Y, defaultInsideY);
        }
        else if(LEGEND_OUTSIDE_POSITION.equals(key))
        {
            this.outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, LEGEND_OUTSIDE_POSITION, RectangleEdge.RIGHT);
        }
        else if(LEGEND_MARGIN_TOP.equals(key))
        {
            this.marginTop = pref.getDouble(LEGEND_MARGIN_TOP, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;      
        }
        else if(LEGEND_MARGIN_BOTTOM.equals(key))
        {
            this.marginBottom = pref.getDouble(LEGEND_MARGIN_BOTTOM, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;     
        }
        else if(LEGEND_MARGIN_LEFT.equals(key))
        {
            this.marginLeft = pref.getDouble(LEGEND_MARGIN_LEFT, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;
        }
        else if(LEGEND_MARGIN_RIGHT.equals(key))
        {
            this.marginRight = pref.getDouble(LEGEND_MARGIN_RIGHT, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;
        }
        else if(LEGEND_FRAME_PAINT.equals(key))
        {
            this.framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, LEGEND_FRAME_PAINT, Color.black);
        }
        else if(LEGEND_FRAME_STROKE.equals(key))
        {
            this.frameStroke = SerializationUtilities.getStroke(pref, LEGEND_FRAME_STROKE, new BasicStroke(1.f));

        }
    };
}
