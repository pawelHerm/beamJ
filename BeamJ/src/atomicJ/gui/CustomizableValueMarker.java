package atomicJ.gui;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import atomicJ.gui.profile.KnobSpecification;

public class CustomizableValueMarker extends ValueMarker
{
    private static final long serialVersionUID = 1L;

    private final PreferredMarkerStyle preferredStyle;
    private boolean visible = true;

    private final String name;
    private Object key;

    private List<Double> knobPositions = new ArrayList<>();
    private final List<Shape> knobHotSpots = new ArrayList<>();

    private int knobWidth = 12;
    private int knobHeight = 25;

    public CustomizableValueMarker(String name, double value, PreferredMarkerStyle preferredStyle, Object key) 
    {
        super(value);

        this.name = name;
        this.key = key;
        this.preferredStyle = preferredStyle;

        // Stroke defaultStroke = StackMapCrossSectionDialog.FORCE_LEVEL_MARKER.equals(key) ? DEFAULT_DASHED_STROKE : DEFAULT_SOLID_STROKE;

        setPreferredStyle(preferredStyle);
    }

    public String getName()
    {
        return name;
    }

    private void setPreferredStyle(PreferredMarkerStyle preferredStyle)
    {
        float alpha = preferredStyle.getAlpha();
        Paint paint = preferredStyle.getPaint();
        Stroke stroke = preferredStyle.getStroke();

        setAlpha(alpha);
        setPaint(paint);
        setStroke(stroke);
    }

    public Object getKey()
    {
        return key;
    }

    public void setKey(Object key)
    {
        this.key = key;
    }


    public int getKnobWidth()
    {
        return knobWidth;
    }

    public void setKnobWidth(int knobWidthNew)
    {
        this.knobWidth = knobWidthNew;        
        notifyListeners(new MarkerChangeEvent(this));
    }

    public int getKnobHeight()
    {
        return knobHeight;
    }

    public void setKnobHeight(int knobHeightNew)
    {
        this.knobHeight = knobHeightNew;       
        notifyListeners(new MarkerChangeEvent(this));
    }

    public boolean isVisible()
    {
        return visible;
    }

    //The field visible should be respected be renderers
    public void setVisible(boolean visible)
    {
        this.visible = visible;

        notifyListeners(new MarkerChangeEvent(this));
    }

    public Preferences getPreferences()
    {
        return preferredStyle.getPreferences();
    }

    public void addKnob(double d)
    {            
        this.knobPositions.add(Double.valueOf(d));
        notifyListeners(new MarkerChangeEvent(this));     
    }

    public void moveKnob(int knobIndex, double d)
    {
        int n = knobPositions.size();

        if(knobIndex >= 0 && knobIndex <n)
        {
            this.knobPositions.set(knobIndex, d);       
            notifyListeners(new MarkerChangeEvent(this));
        }   
    }

    public void removeKnob(int knobIndex)
    {
        int n = knobPositions.size();

        if(knobIndex >= 0 && knobIndex <n)
        {
            this.knobPositions.remove(knobIndex);
            this.knobHotSpots.remove(knobIndex);
            notifyListeners(new MarkerChangeEvent(this));
        }
    }

    public List<Double> getKnobPositions()
    {
        return new ArrayList<>(knobPositions);
    }

    public void setKnobPositions(List<Double> knobPositionsNew)
    {       
        this.knobPositions = new ArrayList<>(knobPositionsNew);

        notifyListeners(new MarkerChangeEvent(this));
    }

    public KnobSpecification getCaughtKnob(Point2D java2DPoint)
    {
        KnobSpecification knobSpecification = null;

        int n = knobHotSpots.size();

        for(int i = 0; i<n; i++)
        {
            Shape hotspot = knobHotSpots.get(i);

            if(hotspot.contains(java2DPoint))
            {                
                knobSpecification = new KnobSpecification(key, i, knobPositions.get(i));
                break;
            }
        }

        return knobSpecification;
    }

    public void drawKnobs(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, boolean domain)
    {
        knobHotSpots.clear();

        for(Double knobPosition : knobPositions)
        {
            drawKnob(g2, plot, dataArea, domainAxis, rangeAxis, knobPosition, domain);
        }
    }

    private void drawKnob(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,
            double knobPosition, boolean domain) 
    {
        if(isVisible())
        {
            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            double dataX = domain ? getValue() : knobPosition;

            double dataY = domain ? knobPosition : getValue();

            double controlX = domainAxis.valueToJava2D(dataX, dataArea, domainEdge);
            double controlY = rangeAxis.valueToJava2D(dataY, dataArea, rangeEdge);

            boolean isVertical = (orientation == PlotOrientation.VERTICAL);

            double X0 = isVertical ? controlX : controlY;
            double Y0 = isVertical ? controlY : controlX;

            double X1 = isVertical ? X0 - 0.5*knobWidth: X0 - knobHeight;
            double Y1 = isVertical ? Y0 + knobHeight : Y0 - 0.5*knobWidth;

            double X2 = isVertical ? X0 + 0.5*knobWidth : X0 - knobHeight;
            double Y2 = isVertical ? Y0 + knobHeight: Y0 + 0.5*knobWidth;

            GeneralPath s = new GeneralPath();
            s.moveTo(X0, Y0);
            s.lineTo(X1, Y1);
            s.lineTo(X2, Y2);
            s.closePath();

            Shape shape;

            if(domain)
            {
                AffineTransform transformNew = new AffineTransform();

                transformNew.rotate(-Math.PI/2, X0, Y0);    
                shape = transformNew.createTransformedShape(s);
            }
            else
            {
                shape = s;
            }  

            g2.setPaint(getPaint());
            g2.fill(shape);

            knobHotSpots.add(shape);
        }
    }

}
