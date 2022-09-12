
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by Pawe� Hermanowicz
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

package atomicJ.gui.rois.line;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.PathSegment;
import atomicJ.gui.PathShiftIterator;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.AnnotationHotSpotAnchor;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.ProfilePolyLine;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.utilities.GeometryUtilities;

public class ROIPolyLine extends ROICurve implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    private final float hotSpotRadius = 5.f;

    private Point2D startPoint;
    private Point2D lastFixedPoint;
    private Point2D endPoint;

    private Path2D distanceShape;
    private Path2D fixedPartOfShape;

    private int segmentCount = 0;

    private List<Shape> reshapeHotSpots = new ArrayList<>();

    public ROIPolyLine(Point2D startPoint, Integer key, ProfileStyle style) 
    {
        this(startPoint, key, key.toString(), style); 
    }

    public ROIPolyLine(Point2D startPoint, Integer key, String label, ProfileStyle style) 
    {	
        super(key, label, style);

        GeneralPath shape = new GeneralPath();
        shape.moveTo(startPoint.getX(), startPoint.getY());

        this.distanceShape = shape;
        this.fixedPartOfShape = shape;   
        this.startPoint = startPoint;
        this.lastFixedPoint = startPoint;
        this.endPoint = startPoint;
    }

    public ROIPolyLine(ROIPolyLine that)
    {
        this(that, that.getStyle());
    }

    public ROIPolyLine(ROIPolyLine that, ProfileStyle style)
    {
        super(that, style);

        this.distanceShape = new GeneralPath(that.distanceShape);
        this.fixedPartOfShape = new GeneralPath(that.fixedPartOfShape); 
        this.startPoint = new Point2D.Double(that.startPoint.getX(), that.startPoint.getY());
        this.lastFixedPoint = new Point2D.Double(that.startPoint.getX(), that.startPoint.getY());
        this.endPoint = new Point2D.Double(that.endPoint.getX(), that.endPoint.getY());
        this.segmentCount = that.segmentCount;
    }

    protected ROIPolyLine(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, ProfileStyle style, Integer key, String label)
    {
        super(key, label, style);

        this.distanceShape = new GeneralPath(fixedPartOfShape);
        this.fixedPartOfShape = new GeneralPath(fixedPartOfShape); 
        this.startPoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        this.lastFixedPoint = new Point2D.Double(lastFixedPoint.getX(), lastFixedPoint.getY());
        this.endPoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
        this.segmentCount = segmentCount;
    }

    @Override
    public boolean isComplex()
    {
        return true;
    }

    @Override
    public ROIPolyLine copy()
    {
        return new ROIPolyLine(this);
    }

    @Override
    public ROIPolyLine copy(ProfileStyle style)
    {
        return new ROIPolyLine(this, style);
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }

        return MouseInputModeStandard.ROI_POLY_LINE_SPLIT;
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorCore core = null;

        int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

        if(caughtReshapeSpot >= 0)
        {
            core = new AnnotationHotSpotAnchor(caughtReshapeSpot);
        }
        else if(isFinished() && isClicked(dataRectangle))
        {
            core = BasicAnnotationAnchor.CENTER;
        }

        AnnotationAnchorSigned caughtAnchor = (core != null) ? new AnnotationAnchorSourceSigned(core, getKey()) : null;
        return caughtAnchor;     
    }

    public int getCaughtReshapeSpot(Point2D java2DPoint)
    {
        int index = -1;

        int n = reshapeHotSpots.size();

        for(int i = 0; i<n; i++)
        {
            Shape hotspot = reshapeHotSpots.get(i);

            if(hotspot.contains(java2DPoint))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned anchor, Point2D startPoint, Point2D endPoint)
    {
        if(anchor == null)
        {
            return null;
        }   

        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();
        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(tx, ty);

            distanceShape = new GeneralPath(distanceShape.createTransformedShape(transform));
            fixedPartOfShape = new GeneralPath(distanceShape); 

            this.startPoint = DistanceShapeFactors.getFirstPoint(distanceShape);
            this.endPoint = DistanceShapeFactors.getLastPoint(distanceShape);
            this.lastFixedPoint = endPoint;

            fireAnnotationChanged();
        }
        else if(coreAnchor instanceof AnnotationHotSpotAnchor)
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            int index = ((AnnotationHotSpotAnchor)coreAnchor).getIndex();
            Path2D roiShapeOld = distanceShape;

            distanceShape = new GeneralPath();
            distanceShape.append(new PathShiftIterator(roiShapeOld.getPathIterator(null), index, tx, ty), true);
            fixedPartOfShape = new GeneralPath(distanceShape); 

            this.startPoint = DistanceShapeFactors.getFirstPoint(distanceShape);
            this.endPoint = DistanceShapeFactors.getLastPoint(distanceShape);
            this.lastFixedPoint = endPoint;

            fireAnnotationChanged();
        }

        AnnotationAnchorSigned caughtAnchorNew = (coreAnchor != null && ObjectUtilities.equal(anchor.getKey(), getKey())) ? new AnnotationAnchorSourceSigned(coreAnchor, getKey()) : null;
        return caughtAnchorNew;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {        
        if(modifierKeys.contains(ModifierKey.CONTROL) && reshapeHotSpots.size() > 2)
        {           
            int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

            if(caughtReshapeSpot >= 0)
            {            
                this.distanceShape = GeometryUtilities.removePoint(distanceShape, caughtReshapeSpot);
                this.fixedPartOfShape = new GeneralPath(distanceShape); 

                fireAnnotationChanged();

                return true;
            }
        }
        else if(modifierKeys.contains(ModifierKey.SHIFT))
        {
            int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

            if(caughtReshapeSpot >= 0)
            {            
                this.distanceShape = GeometryUtilities.splitPoint(distanceShape, caughtReshapeSpot, false);
                this.fixedPartOfShape = new GeneralPath(distanceShape); 

                fireAnnotationChanged();

                return true;
            }
        }

        return false;
    }


    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.lastFixedPoint.getX(),  this.lastFixedPoint.getY(), x, y, modifiers);
        lineTemporailyTo(endNew.getX(), endNew.getY(), true);            
    }  

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.lastFixedPoint.getX(),  this.lastFixedPoint.getY(), x, y, modifiers);
        lineTo(endNew.getX(), endNew.getY(), true);
    }

    protected void lineTo(double x, double y, boolean notify)
    {
        fixedPartOfShape.lineTo(x, y);
        distanceShape = new GeneralPath(fixedPartOfShape);   
        segmentCount++;

        this.lastFixedPoint = new Point2D.Double(x, y);
        this.endPoint = new Point2D.Double(x, y);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    protected void lineTemporailyTo(double x, double y, boolean notify)
    {
        distanceShape = new GeneralPath(fixedPartOfShape);
        distanceShape.lineTo(x, y);

        this.endPoint = new Point2D.Double(x, y);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }


    public Path2D getModifiableShape()
    {
        return distanceShape;
    }

    protected int getSegmentCount()
    {
        return segmentCount;
    }

    @Override
    public double getLength()
    {
        double length = DistanceShapeFactors.getLength(distanceShape, 0);

        return length;
    }

    @Override
    public double getStartX()
    {
        return startPoint.getX();
    }

    @Override
    public double getStartY()
    {
        return startPoint.getY();
    }

    @Override
    public double getEndX()
    {
        return endPoint.getX();
    }

    @Override
    public double getEndY()
    {
        return endPoint.getY();
    }

    @Override
    public Point2D getStartPoint()
    {
        return startPoint;
    }

    protected Point2D getLastFixedPoint()
    {
        return lastFixedPoint;
    }

    @Override
    public Point2D getEndPoint()
    {
        return endPoint;
    }

    @Override
    public Path2D getDistanceShape()
    {
        return distanceShape;
    }

    @Override
    protected void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint)
    {
        this.reshapeHotSpots = new ArrayList<>();

        if(isVisible() && isHighlighted())
        {
            PathIterator iterator = distanceShape.getPathIterator(null);

            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            while(!iterator.isDone())
            {
                float[] coords = new float[6];

                int segmentType = iterator.currentSegment(coords);

                PathSegment pathSegment = PathSegment.instanceFor(segmentType);
                int pointCount =  pathSegment.getPointCount();

                boolean isVertical = (PlotOrientation.VERTICAL.equals(orientation));

                for(int i = 0; i<2*pointCount; i = i + 2)
                {
                    float j2DX = 0.0f;
                    float j2DY = 0.0f;
                    j2DX = (float) domainAxis.valueToJava2D(coords[i], dataArea, domainEdge);
                    j2DY = (float) rangeAxis.valueToJava2D(coords[i + 1], dataArea, rangeEdge);

                    Shape  startHotSpot;
                    if(isVertical)
                    {
                        startHotSpot = new Ellipse2D.Float(j2DX - hotSpotRadius, j2DY - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);
                    }
                    else
                    {
                        startHotSpot = new Ellipse2D.Float(j2DY - hotSpotRadius, j2DX - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);
                    }

                    g2.setPaint(getStrokePaint());
                    g2.fill(startHotSpot);      

                    reshapeHotSpots.add(startHotSpot);                    
                }

                iterator.next();              
            }
        }  
    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors() {
        return DistanceShapeFactors.getShapeFactors(distanceShape, 0.);
    }

    //    public List<ROIPolyLine> split(double[][] polylineVertices)
    //    {
    //        
    //    }
    //    


    @Override
    public ROICurveSerializationProxy getProxy()
    {
        return new ROIPolyLineSerializationProxy(fixedPartOfShape, startPoint, getLastFixedPoint(), endPoint, segmentCount, getCustomLabel(), isFinished());
    }

    private static class ROIPolyLineSerializationProxy implements ROICurveSerializationProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D fixedPartOfShape;
        private final Point2D startPoint;
        private final Point2D lastFixedPoint;
        private final Point2D endPoint;
        private final int segmentCount;
        private final String customLabel;
        private final boolean finished;

        private ROIPolyLineSerializationProxy(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, String customLabel, boolean finished)
        {
            this.fixedPartOfShape = fixedPartOfShape;
            this.startPoint = startPoint;
            this.lastFixedPoint = lastFixedPoint;
            this.endPoint = endPoint;
            this.segmentCount = segmentCount;
            this.customLabel = customLabel;
            this.finished = finished;
        }

        @Override
        public ROIPolyLine recreateOriginalObject(ProfileStyle profileStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            ROIPolyLine profile = new ROIPolyLine(fixedPartOfShape, startPoint, lastFixedPoint, endPoint, segmentCount, profileStyle, key, label);
            profile.setFinished(finished);

            return profile;
        }        
    }
}

