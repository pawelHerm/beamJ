
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

package atomicJ.gui;


//THIS CLASS IS A MODIFICATION OF A CLASS XYTITLEANNOTATION
//WRITTEN BY DAVID GILBERT AND OTHERS
//BELOW THE ORIGITIONAL  COPYRIGHT STATEMENT
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ----------------------
 * XYTitleAnnotation.java
 * ----------------------
 * (C) Copyright 2007-2011, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Andrew Mickish;
 *                   Peter Kolb (patch 2809117);
 *
 * Changes:
 * --------
 * 02-Feb-2007 : Version 1 (DG);
 * 30-Apr-2007 : Fixed equals() method (DG);
 * 26-Feb-2008 : Fixed NullPointerException when drawing chart with a null
 *               ChartRenderingInfo - see patch 1901599 by Andrew Mickish (DG);
 * 03-Sep-2008 : Moved from experimental to main (DG);
 * 24-Jun-2009 : Fire change events (see patch 2809117 by PK) (DG);
 *
 */


import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockParams;
import org.jfree.chart.block.EntityBlockResult;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.XYCoordinateType;
import org.jfree.data.Range;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * An annotation that allows any {@link Title} to be placed at a location on
 * an {@link XYPlot}.
 *
 * @since 1.0.11
 */
public class RoamingTitleAnnotation extends AbstractXYAnnotation
implements Cloneable, PublicCloneable, Serializable, TitleChangeListener {

    /** For serialization. */
    private static final long serialVersionUID = -4364694501921559958L;

    /** The coordinate type. */
    private final XYCoordinateType coordinateType;

    /** The x-coordinate (in data space). */
    private double x;

    /** The y-coordinate (in data space). */
    private double y;

    /** The maximum width. */
    private double maxWidth;

    /** The maximum height. */
    private double maxHeight;

    /** The title. */
    private Title title;

    private final RoamingTitle roamingTitle;

    /**
     * The title anchor point.
     */
    private final RectangleAnchor anchor;

    /**
     * Creates a new annotation to be displayed at the specified (x, y)
     * location.
     *
     * @param x  the x-coordinate (in data space).
     * @param y  the y-coordinate (in data space).
     * @param roamingTitle  the title (<code>null</code> not permitted).
     */
    public RoamingTitleAnnotation(double x, double y, RoamingTitle roamingTitle) {
        this(x, y, roamingTitle, RectangleAnchor.CENTER);
    }

    /**
     * Creates a new annotation to be displayed at the specified (x, y)
     * location.
     *
     * @param x  the x-coordinate (in data space).
     * @param y  the y-coordinate (in data space).
     * @param roamingTitle  the title (<code>null</code> not permitted).
     * @param anchor  the title anchor (<code>null</code> not permitted).
     */
    public RoamingTitleAnnotation(double x, double y, RoamingTitle roamingTitle,
            RectangleAnchor anchor) {
        super();
        if (roamingTitle == null) {
            throw new IllegalArgumentException("Null 'title' argument.");
        }
        if (anchor == null) {
            throw new IllegalArgumentException("Null 'anchor' argument.");
        }
        this.coordinateType = XYCoordinateType.RELATIVE;
        this.x = x;
        this.y = y;
        this.maxWidth = 0.0;
        this.maxHeight = 0.0;
        this.roamingTitle = roamingTitle;
        this.title = roamingTitle.getOutsideTitle();
        this.anchor = anchor;
        title.addChangeListener(this);
    }

    /**
     * Returns the coordinate type (set in the constructor).
     *
     * @return The coordinate type (never <code>null</code>).
     */
    public XYCoordinateType getCoordinateType() {
        return this.coordinateType;
    }

    /**
     * Returns the x-coordinate for the annotation.
     *
     * @return The x-coordinate.
     */
    public double getX() {
        return this.x;
    }

    public void setX(double x)
    {
        setX(x, true);
    }

    public void setX(double x, boolean notify)
    {
        this.x = x;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }


    public double getY() {
        return this.y;
    }

    public void setY(double y)
    {
        setY(y, true);
    }

    public void setY(double y, boolean notify)
    {
        this.y = y;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public void setPosition(double x, double y)
    {
        setPosition(x, y, true);
    }

    public void setPosition(double x, double y, boolean notify)
    {
        this.x = x;
        this.y = y;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public Title getTitle() {
        return this.title;
    }

    /**
     * Returns the title anchor for the annotation.
     *
     * @return The title anchor.
     */
    public RectangleAnchor getTitleAnchor() {
        return this.anchor;
    }

    /**
     * Returns the maximum width.
     *
     * @return The maximum width.
     */
    public double getMaxWidth() {
        return this.maxWidth;
    }

    /**
     * Sets the maximum width and sends an
     * {@link AnnotationChangeEvent} to all registered listeners.
     *
     * @param max  the maximum width (0.0 or less means no maximum).
     */
    public void setMaxWidth(double max) {
        this.maxWidth = max;
        fireAnnotationChanged();
    }

    /**
     * Returns the maximum height.
     *
     * @return The maximum height.
     */
    public double getMaxHeight() {
        return this.maxHeight;
    }

    /**
     * Sets the maximum height and sends an
     * {@link AnnotationChangeEvent} to all registered listeners.
     *
     * @param max  the maximum height.
     */
    public void setMaxHeight(double max) {
        this.maxHeight = max;
        fireAnnotationChanged();
    }

    /**
     * Draws the annotation.  This method is called by the drawing code in the
     * {@link XYPlot} class, you don't normally need to call this method
     * directly.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  if supplied, this info object will be populated with
     *              entity information.
     */
    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis,
            int rendererIndex,
            PlotRenderingInfo info) 
    {

        PlotOrientation orientation = plot.getOrientation();
        AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
        AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(domainAxisLocation, orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(rangeAxisLocation, orientation);
        Range xRange = domainAxis.getRange();
        Range yRange = rangeAxis.getRange();
        double anchorX = 0.0;
        double anchorY = 0.0;
        if (this.coordinateType == XYCoordinateType.RELATIVE) {
            anchorX = xRange.getLowerBound() + (this.x * xRange.getLength());
            anchorY = yRange.getLowerBound() + (this.y * yRange.getLength());
        }
        else {
            anchorX = domainAxis.valueToJava2D(this.x, dataArea, domainEdge);
            anchorY = rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
        }

        float j2DX = (float) domainAxis.valueToJava2D(anchorX, dataArea,domainEdge);
        float j2DY = (float) rangeAxis.valueToJava2D(anchorY, dataArea, rangeEdge);
        float xx = 0.0f;
        float yy = 0.0f;

        if (orientation == PlotOrientation.HORIZONTAL) {
            xx = j2DY;
            yy = j2DX;
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            xx = j2DX;
            yy = j2DY;
        }

        double maxW = dataArea.getWidth();
        double maxH = dataArea.getHeight();
        if (this.coordinateType == XYCoordinateType.RELATIVE) {
            if (this.maxWidth > 0.0) {
                maxW = maxW * this.maxWidth;
            }
            if (this.maxHeight > 0.0) {
                maxH = maxH * this.maxHeight;
            }
        }
        if (this.coordinateType == XYCoordinateType.DATA) {
            maxW = this.maxWidth;
            maxH = this.maxHeight;
        }
        RectangleConstraint rc = new RectangleConstraint(new Range(0, maxW), new Range(0, maxH));

        Size2D size = this.title.arrange(g2, rc);
        Rectangle2D titleRect = new Rectangle2D.Double(0, 0, size.width, size.height);
        Point2D anchorPoint = RectangleAnchor.coordinates(titleRect, this.anchor);
        xx = xx - (float) anchorPoint.getX();
        yy = yy - (float) anchorPoint.getY();
        titleRect.setRect(xx, yy, titleRect.getWidth(), titleRect.getHeight());
        BlockParams blockParameters = new BlockParams();
        if (info != null) 
        {
            if (info.getOwner().getEntityCollection() != null) 
            {
                blockParameters.setGenerateEntities(true);
            }
        }
        Object result = this.title.draw(g2, titleRect, blockParameters);

        if (info != null) 
        {
            EntityCollection entities = info.getOwner().getEntityCollection();
            if (entities == null) 
            {
                return;
            }
            RectangleInsets insets = title.getMargin();

            Rectangle2D area = new Rectangle2D.Float(xx, yy,(float) size.width, (float) size.height);
            insets.trim(area);

            RoamingTitleEntity entity = new RoamingTitleEntity(area, this.roamingTitle) ;
            entities.add(entity);

            if (result instanceof EntityBlockResult) 
            {
                EntityBlockResult ebr = (EntityBlockResult) result;
                EntityCollection blockEntities = ebr.getEntityCollection();

                for(Object e : blockEntities.getEntities())
                {                            
                    if(e instanceof LegendItemEntity)
                    {
                        LegendItemEntity legendItemEntity = (LegendItemEntity)e;
                        RoamingLegendItemEntity roamingLegendItemEntity = new RoamingLegendItemEntity(legendItemEntity, roamingTitle);
                        entities.add(roamingLegendItemEntity);
                    }
                    else if(e instanceof AxisEntity)
                    {
                        entities.add((AxisEntity) e);
                    }
                }
            }
        }
    }

    //    public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
    //            CategoryAxis domainAxis, ValueAxis rangeAxis,
    //            int rendererIndex, int categoryCount,
    //            PlotRenderingInfo info) 
    //    {
    //
    //        PlotOrientation orientation = plot.getOrientation();
    //        AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
    //        AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
    //        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(domainAxisLocation, orientation);
    //        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(rangeAxisLocation, orientation);
    //        Range xRange = new Range(0, categoryCount);
    //        Range yRange = rangeAxis.getRange();
    //        
    //        CategoryItemRenderer renderer = plot.getRenderer(rendererIndex);
    //        CategoryDataset dataset = plot.getDataset(rendererIndex);
    //        int seriesCount = dataset.get
    //        
    //        double anchorX = 0.0;
    //        double anchorY = 0.0;
    //        if (this.coordinateType == XYCoordinateType.RELATIVE) {
    //            anchorX = xRange.getLowerBound() + (this.x * xRange.getLength());
    //            anchorY = yRange.getLowerBound() + (this.y * yRange.getLength());
    //        }
    //        else {
    //            anchorX = categoryValueToJava2D(domainAxis, this.x, categoryCount, dataArea, domainEdge);
    //            anchorY = rangeAxis.valueToJava2D(this.y, dataArea, rangeEdge);
    //        }
    //
    //        float j2DX = (float) categoryValueToJava2D(domainAxis, anchorX, categoryCount,  dataArea,domainEdge);
    //        float j2DY = (float) rangeAxis.valueToJava2D(anchorY, dataArea, rangeEdge);
    //        float xx = 0.0f;
    //        float yy = 0.0f;
    //        
    //        if (orientation == PlotOrientation.HORIZONTAL) {
    //            xx = j2DY;
    //            yy = j2DX;
    //        }
    //        else if (orientation == PlotOrientation.VERTICAL) {
    //            xx = j2DX;
    //            yy = j2DY;
    //        }
    //
    //        double maxW = dataArea.getWidth();
    //        double maxH = dataArea.getHeight();
    //        if (this.coordinateType == XYCoordinateType.RELATIVE) {
    //            if (this.maxWidth > 0.0) {
    //                maxW = maxW * this.maxWidth;
    //            }
    //            if (this.maxHeight > 0.0) {
    //                maxH = maxH * this.maxHeight;
    //            }
    //        }
    //        if (this.coordinateType == XYCoordinateType.DATA) {
    //            maxW = this.maxWidth;
    //            maxH = this.maxHeight;
    //        }
    //        RectangleConstraint rc = new RectangleConstraint(new Range(0, maxW), new Range(0, maxH));
    //
    //        Size2D size = this.title.arrange(g2, rc);
    //        Rectangle2D titleRect = new Rectangle2D.Double(0, 0, size.width, size.height);
    //        Point2D anchorPoint = RectangleAnchor.coordinates(titleRect, this.anchor);
    //        xx = xx - (float) anchorPoint.getX();
    //        yy = yy - (float) anchorPoint.getY();
    //        titleRect.setRect(xx, yy, titleRect.getWidth(), titleRect.getHeight());
    //        BlockParams blockParameters = new BlockParams();
    //        if (info != null) 
    //        {
    //            if (info.getOwner().getEntityCollection() != null) 
    //            {
    //                blockParameters.setGenerateEntities(true);
    //            }
    //        }
    //        Object result = this.title.draw(g2, titleRect, blockParameters);
    //        
    //        if (info != null) 
    //        {
    //            EntityCollection entities = info.getOwner().getEntityCollection();
    //            if (entities == null) 
    //            {
    //                return;
    //            }
    //            RectangleInsets insets = title.getMargin();
    //            
    //            Rectangle2D area = new Rectangle2D.Float(xx, yy,(float) size.width, (float) size.height);
    //            insets.trim(area);
    //
    //            RoamingTitleEntity entity = new RoamingTitleEntity(area, this.roamingTitle) ;
    //            entities.add(entity);
    //
    //            if (result instanceof EntityBlockResult) 
    //            {
    //                EntityBlockResult ebr = (EntityBlockResult) result;
    //                EntityCollection blockEntities = ebr.getEntityCollection();
    //
    //                for(Object e : blockEntities.getEntities())
    //                {                            
    //                    if(e instanceof LegendItemEntity)
    //                    {
    //                        LegendItemEntity legendItemEntity = (LegendItemEntity)e;
    //                        RoamingLegendItemEntity roamingLegendItemEntity = new RoamingLegendItemEntity(legendItemEntity, roamingTitle);
    //                        entities.add(roamingLegendItemEntity);
    //                    }
    //                    else if(e instanceof AxisEntity)
    //                    {
    //                        entities.add((AxisEntity) e);
    //                    }
    //                }
    //            }
    //        }
    //    }


    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RoamingTitleAnnotation)) {
            return false;
        }
        RoamingTitleAnnotation that = (RoamingTitleAnnotation) obj;
        if (this.coordinateType != that.coordinateType) {
            return false;
        }
        if (this.x != that.x) {
            return false;
        }
        if (this.y != that.y) {
            return false;
        }
        if (this.maxWidth != that.maxWidth) {
            return false;
        }
        if (this.maxHeight != that.maxHeight) {
            return false;
        }
        if (!ObjectUtilities.equal(this.title, that.title)) {
            return false;
        }
        if (!this.anchor.equals(that.anchor)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode(result, this.anchor);
        result = HashUtilities.hashCode(result, this.coordinateType);
        result = HashUtilities.hashCode(result, this.x);
        result = HashUtilities.hashCode(result, this.y);
        result = HashUtilities.hashCode(result, this.maxWidth);
        result = HashUtilities.hashCode(result, this.maxHeight);
        result = HashUtilities.hashCode(result, this.title);
        return result;
    }

    /**
     * Returns a clone of the annotation.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the annotation can't be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException 
    {        
        RoamingTitleAnnotation clone = (RoamingTitleAnnotation)super.clone();

        clone.title = (Title) this.title.clone();

        return clone;
    }

    @Override
    public void titleChanged(TitleChangeEvent evt) 
    {
        fireAnnotationChanged();
    }


    //CHECK IF IT WORKS
    public double categoryValueToJava2D(CategoryAxis axis, double position, int categoryCount, int seriesIndex,
            int seriesCount, double itemMargin, Rectangle2D area, RectangleEdge edge)
    {
        int categoryIndex = (int)Math.rint(Math.floor(position));
        double fraction = position - categoryIndex;

        double start = axis.getCategoryStart(categoryIndex, categoryCount, area, edge);
        double end = axis.getCategoryEnd(categoryIndex, categoryCount, area, edge);
        double width = end - start;
        if (seriesCount == 1) 
        {
            return start + fraction*width;
        }
        else 
        {
            double gap = (width * itemMargin) / (seriesCount - 1);
            double ww = (width * (1 - itemMargin)) / seriesCount;
            return start + (seriesIndex * (ww + gap)) + fraction*ww;
        }
    }

}

