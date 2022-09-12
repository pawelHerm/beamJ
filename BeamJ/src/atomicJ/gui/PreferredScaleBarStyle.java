package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_FONT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_OFFSET;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_PAINT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_POSITION;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LABEL_VISIBLE;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_LENGTH_AUTOMATIC;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_POSITION_X;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_POSITION_Y;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_STROKE;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_STROKE_PAINT;
import static atomicJ.gui.PreferenceKeys.SCALEBAR_VISIBLE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredScaleBarStyle implements PreferenceChangeListener
{
    private boolean visible;

    private float labelOffset;
    private float lengthwisePosition;

    private boolean labelVisible;   
    private Stroke stroke;
    private Paint strokePaint;      
    private Paint labelPaint;   
    private Font labelFont;

    private double x;
    private double y;

    private boolean lengthAutomatic;
    private double length;

    private final Preferences pref;

    public PreferredScaleBarStyle(Preferences pref)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.visible = pref.getBoolean(SCALEBAR_VISIBLE, false);
        this.lengthAutomatic = pref.getBoolean(SCALEBAR_LENGTH_AUTOMATIC, true);

        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_STROKE_PAINT, Color.black);
        this.stroke = SerializationUtilities.getStroke(pref, SCALEBAR_STROKE, new BasicStroke(3.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));

        this.x = pref.getDouble(SCALEBAR_POSITION_X, 0.8);
        this.y = pref.getDouble(SCALEBAR_POSITION_Y, 0.1);

        this.labelVisible = pref.getBoolean(SCALEBAR_LABEL_VISIBLE, true);
        this.labelOffset = pref.getFloat(SCALEBAR_LABEL_OFFSET, 0.f);
        this.lengthwisePosition = pref.getFloat(SCALEBAR_LABEL_POSITION, 0.5f);
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public double getPositionX()
    {
        return x;
    }

    public double getPositionY()
    {
        return y;
    }

    public double getLength()
    {
        return length;
    }

    public boolean isLengthAutomatic()
    {
        return lengthAutomatic;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }   

    public Paint getLabelPaint()
    {       
        return labelPaint;
    }

    public Paint getStrokePaint()
    {
        return strokePaint;
    }

    public Stroke getStroke()
    {
        return stroke;
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public float getLabelLengthwisePosition()
    {
        return lengthwisePosition;
    }

    public float getLabelOffset()
    {
        return labelOffset;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(SCALEBAR_VISIBLE.equals(key))
        {
            this.visible = pref.getBoolean(SCALEBAR_VISIBLE, false);
        }
        else if(SCALEBAR_LENGTH_AUTOMATIC.equals(key))
        {
            this.lengthAutomatic = pref.getBoolean(SCALEBAR_LENGTH_AUTOMATIC, true);
        }
        else if(SCALEBAR_STROKE_PAINT.equals(key))
        {
            this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_STROKE_PAINT, Color.black);
        }
        else if(SCALEBAR_STROKE.equals(key))
        {
            this.stroke = SerializationUtilities.getStroke(pref, SCALEBAR_STROKE, new BasicStroke(3.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        }
        else if(SCALEBAR_LABEL_PAINT.equals(key))
        {
            this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_PAINT, Color.black);
        }
        else if(SCALEBAR_LABEL_FONT.equals(key))
        {
            this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));
        }
        else if(SCALEBAR_POSITION_X.equals(key))
        {
            this.x = pref.getDouble(SCALEBAR_POSITION_X, 0.8);
        }
        else if(SCALEBAR_POSITION_Y.equals(key))
        {
            this.y = pref.getDouble(SCALEBAR_POSITION_Y, 0.1);
        }
        else if(SCALEBAR_LABEL_VISIBLE.equals(key))
        {
            this.labelVisible = pref.getBoolean(SCALEBAR_LABEL_VISIBLE, true);
        }
        else if(SCALEBAR_LABEL_OFFSET.equals(key))
        {
            this.labelOffset = pref.getFloat(SCALEBAR_LABEL_OFFSET, 0.f);
        }
        else if(SCALEBAR_LABEL_POSITION.equals(key))
        {
            this.lengthwisePosition = pref.getFloat(SCALEBAR_LABEL_POSITION, 0.5f);
        }
    }
}
