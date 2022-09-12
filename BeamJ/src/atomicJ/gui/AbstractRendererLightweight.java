package atomicJ.gui;

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
 * ---------------------
 * AbstractRenderer.java
 * ---------------------
 * (C) Copyright 2002-2011, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Nicolas Brodu;
 *
 * Changes:
 * --------
 * 22-Aug-2002 : Version 1, draws code out of AbstractXYItemRenderer to share
 *               with AbstractCategoryItemRenderer (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Nov-2002 : Moved to the com.jrefinery.chart.renderer package (DG);
 * 21-Nov-2002 : Added a paint table for the renderer to use (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 29-Apr-2003 : Added valueLabelFont and valueLabelPaint attributes, based on
 *               code from Arnaud Lelievre (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 15-Sep-2003 : Fixed serialization (NB);
 * 17-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Moved PlotRenderingInfo into RendererState to allow for
 *               multiple threads using a single renderer (DG);
 * 20-Oct-2003 : Added missing setOutlinePaint() method (DG);
 * 23-Oct-2003 : Split item label attributes into 'positive' and 'negative'
 *               values (DG);
 * 26-Nov-2003 : Added methods to get the positive and negative item label
 *               positions (DG);
 * 01-Mar-2004 : Modified readObject() method to prevent null pointer exceptions
 *               after deserialization (DG);
 * 19-Jul-2004 : Fixed bug in getItemLabelFont(int, int) method (DG);
 * 04-Oct-2004 : Updated equals() method, eliminated use of NumberUtils,
 *               renamed BooleanUtils --> BooleanUtilities, ShapeUtils -->
 *               ShapeUtilities (DG);
 * 15-Mar-2005 : Fixed serialization of baseFillPaint (DG);
 * 16-May-2005 : Base outline stroke should never be null (DG);
 * 01-Jun-2005 : Added hasListener() method for unit testing (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Minor API doc update (DG);
 * 19-Feb-2007 : Fixes for clone() method (DG);
 * 28-Feb-2007 : Use cached event to signal changes (DG);
 * 19-Apr-2007 : Deprecated seriesVisible and seriesVisibleInLegend flags (DG);
 * 20-Apr-2007 : Deprecated paint, fillPaint, outlinePaint, stroke,
 *               outlineStroke, shape, itemLabelsVisible, itemLabelFont,
 *               itemLabelPaint, positiveItemLabelPosition,
 *               negativeItemLabelPosition and createEntities override
 *               fields (DG);
 * 13-Jun-2007 : Added new autoPopulate flags for core series attributes (DG);
 * 23-Oct-2007 : Updated lookup methods to better handle overridden
 *               methods (DG);
 * 04-Dec-2007 : Modified hashCode() implementation (DG);
 * 29-Apr-2008 : Minor API doc update (DG);
 * 17-Jun-2008 : Added legendShape, legendTextFont and legendTextPaint
 *               attributes (DG);
 * 18-Aug-2008 : Added clearSeriesPaints() and clearSeriesStrokes() (DG);
 * 28-Jan-2009 : Equals method doesn't test Shape equality correctly (DG);
 * 27-Mar-2009 : Added dataBoundsIncludesVisibleSeriesOnly attribute, and
 *               updated renderer events for series visibility changes (DG);
 * 01-Apr-2009 : Factored up the defaultEntityRadius field from the
 *               AbstractXYItemRenderer class (DG);
 * 28-Apr-2009 : Added flag to allow a renderer to treat the legend shape as
 *               a line (DG);
 *
 */

import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.util.ObjectUtilities;

public abstract class AbstractRendererLightweight <E extends AbstractRendererData> implements Cloneable
{
    private E rendererData;
    private EventListenerList listenerList;

    //renderer data must be immutable
    public AbstractRendererLightweight(E rendererData) 
    {
        if(!rendererData.isImmutable())
        {
            throw new IllegalArgumentException("Renderer data must be immutable");
        }

        this.rendererData = rendererData;
        this.listenerList = new EventListenerList();
    }

    protected E getData()
    {
        return rendererData;
    }

    public abstract void setData(E data);

    protected abstract AbstractRendererDataMutable getDataForModification();

    protected void replaceData(E rendererDataNew)
    {
        this.rendererData.deregisterRendererIfNecessary(this);
        this.rendererData = rendererDataNew;
        rendererDataNew.registerRendererIfNecessary(this);

        notifyListeners(new RendererChangeEvent(this, true));
    }

    public boolean getBaseSeriesVisible() {
        return rendererData.getBaseSeriesVisible();
    }

    public void setBaseSeriesVisible(boolean visible)
    {
        getDataForModification().setBaseSeriesVisible(visible);
    }

    public void setBaseSeriesVisible(boolean visible, boolean notify)
    {
        getDataForModification().setBaseSeriesVisible(visible, notify);
    }

    public boolean getBaseSeriesVisibleInLegend() {
        return rendererData.getBaseSeriesVisibleInLegend();
    }

    public void setBaseSeriesVisibleInLegend(boolean visible)
    {
        setBaseSeriesVisibleInLegend(visible, true);
    }

    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify)
    {
        getDataForModification().setBaseSeriesVisibleInLegend(visible, notify);
    }

    // PAINT

    public Paint getBasePaint() {
        return rendererData.getBasePaint();
    }

    public void setBasePaint(Paint paint)
    {
        getDataForModification().setBasePaint(paint);
    }

    //// FILL PAINT //////////////////////////////////////////////////////////


    public Paint getBaseFillPaint() {
        return rendererData.getBaseFillPaint();
    }

    public void setBaseFillPaint(Paint paint)
    {
        getDataForModification().setBaseFillPaint(paint);
    }

    // OUTLINE PAINT //////////////////////////////////////////////////////////

    public Paint getBaseOutlinePaint() {
        return rendererData.getBaseOutlinePaint();
    }

    public void setBaseOutlinePaint(Paint paint)
    {
        getDataForModification().setBaseOutlinePaint(paint);
    }

    // STROKE

    public Stroke getBaseStroke() {
        return rendererData.getBaseStroke();
    }

    public void setBaseStroke(Stroke stroke)
    {
        getDataForModification().setBaseStroke(stroke);
    }


    // OUTLINE STROKE

    public Stroke getBaseOutlineStroke() {
        return rendererData.getBaseOutlineStroke();
    }

    public void setBaseOutlineStroke(Stroke stroke)
    {
        setBaseOutlineStroke(stroke, true);
    }
    public void setBaseOutlineStroke(Stroke stroke, boolean notify)
    {
        getDataForModification().setBaseOutlineStroke(stroke, notify);
    }

    // SHAPE

    public Shape getBaseShape() {
        return rendererData.getBaseShape();
    }

    public void setBaseShape(Shape shape)
    {
        setBaseShape(shape, true);
    }

    public void setBaseShape(Shape shape, boolean notify)
    {
        getDataForModification().setBaseShape(shape, notify);
    }

    // ITEM LABEL VISIBILITY...

    public boolean getBaseItemLabelsVisible() {      
        return rendererData.getBaseItemLabelsVisible();
    }

    public void setBaseItemLabelsVisible(boolean visible)
    {
        getDataForModification().setBaseItemLabelsVisible(visible);
    }

    //// ITEM LABEL FONT //////////////////////////////////////////////////////

    public Font getBaseItemLabelFont() {
        return rendererData.getBaseItemLabelFont();
    }

    public void setBaseItemLabelFont(Font font)
    {
        getDataForModification().setBaseItemLabelFont(font);
    }

    //// ITEM LABEL PAINT  ////////////////////////////////////////////////////

    public Paint getBaseItemLabelPaint() {
        return rendererData.getBaseItemLabelPaint();
    }

    public void setBaseItemLabelPaint(Paint paint)
    {
        getDataForModification().setBaseItemLabelPaint(paint);
    }

    // POSITIVE ITEM LABEL POSITION...

    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return rendererData.getBasePositiveItemLabelPosition();
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position)
    {
        setBasePositiveItemLabelPosition(position, true);
    }
    public void setBasePositiveItemLabelPosition(ItemLabelPosition position, boolean notify)
    {
        getDataForModification().setBasePositiveItemLabelPosition(position, notify);
    }

    // NEGATIVE ITEM LABEL POSITION...

    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return rendererData.getBaseNegativeItemLabelPosition();
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position)
    {
        setBaseNegativeItemLabelPosition(position, true);
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position, boolean notify)
    {
        getDataForModification().setBaseNegativeItemLabelPosition(position, notify);
    }


    public double getItemLabelAnchorOffset() {
        return rendererData.getItemLabelAnchorOffset();
    }

    public boolean getBaseCreateEntities() {
        return rendererData.getBaseCreateEntities();
    }

    public int getDefaultEntityRadius() {
        return rendererData.getDefaultEntityRadius();
    }

    public Shape lookupLegendShape() {
        Shape result = rendererData.getBaseLegendShape();

        if (result == null) {
            result = rendererData.getBaseShape();
        }

        return result;
    }

    public Shape getBaseLegendShape() {
        return rendererData.getBaseLegendShape();
    }

    protected boolean getTreatLegendShapeAsLine() {
        return rendererData.getTreatLegendShapeAsLine();
    }

    public Font getBaseLegendTextFont() {
        return rendererData.getBaseLegendTextFont();
    }

    public Paint getBaseLegendTextPaint() {
        return rendererData.getBaseLegendTextPaint();
    }

    public boolean getDataBoundsIncludesVisibleSeriesOnly() {
        return rendererData.getDataBoundsIncludesVisibleSeriesOnly();
    }

    public void dataChanged()
    {
        notifyListeners(new RendererChangeEvent(this));
    }

    public void dataChanged(boolean seriesVisibilityChanged)
    {
        notifyListeners(new RendererChangeEvent(this, seriesVisibilityChanged));
    }

    protected void fireChangeEvent() {

        // the commented out code would be better, but only if
        // RendererChangeEvent is immutable, which it isn't.  See if there is
        // a way to fix this...

        //if (this.event == null) {
        //    this.event = new RendererChangeEvent(this);
        //}
        //notifyListeners(this.event);

        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Registers an object to receive notification of changes to the renderer.
     *
     * @param listener  the listener (<code>null</code> not permitted).
     *
     * @see #removeChangeListener(RendererChangeListener)
     */
    public void addChangeListener(RendererChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null 'listener' argument.");
        }
        this.listenerList.add(RendererChangeListener.class, listener);
    }

    /**
     * Deregisters an object so that it no longer receives
     * notification of changes to the renderer.
     *
     * @param listener  the object (<code>null</code> not permitted).
     *
     * @see #addChangeListener(RendererChangeListener)
     */
    public void removeChangeListener(RendererChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null 'listener' argument.");
        }
        this.listenerList.remove(RendererChangeListener.class, listener);
    }

    /**
     * Returns <code>true</code> if the specified object is registered with
     * the dataset as a listener.  Most applications won't need to call this
     * method, it exists mainly for use by unit testing code.
     *
     * @param listener  the listener.
     *
     * @return A boolean.
     */
    public boolean hasListener(EventListener listener) {
        List<Object> list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }

    public void notifyListeners(RendererChangeEvent event) {
        Object[] ls = this.listenerList.getListenerList();
        for (int i = ls.length - 2; i >= 0; i -= 2) {
            if (ls[i] == RendererChangeListener.class) {
                ((RendererChangeListener) ls[i + 1]).rendererChanged(event);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractRendererLightweight)) {
            return false;
        }

        AbstractRendererLightweight<?> that = (AbstractRendererLightweight<?>)obj;
        if (!ObjectUtilities.equal(this.rendererData, that.rendererData)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode(result, this.rendererData);

        return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractRendererLightweight<?> clone = (AbstractRendererLightweight<?>) super.clone();


        clone.listenerList = new EventListenerList();
        return clone;
    }
}

