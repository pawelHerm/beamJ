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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import org.jfree.chart.HashUtilities;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ShapeUtilities;


public abstract class AbstractRendererDataMutable implements AbstractRendererData, Cloneable {

    private boolean baseSeriesVisible;
    private boolean baseSeriesVisibleInLegend;
    private Paint basePaint;
    private Paint baseFillPaint;
    private Paint baseOutlinePaint;
    private Stroke baseStroke;
    private Stroke baseOutlineStroke;
    private Shape baseShape;
    private boolean baseItemLabelsVisible;
    private Font baseItemLabelFont;
    private Paint baseItemLabelPaint;
    private ItemLabelPosition basePositiveItemLabelPosition;
    private ItemLabelPosition baseNegativeItemLabelPosition;
    private double itemLabelAnchorOffset = 2.0;
    private boolean baseCreateEntities;
    private Shape baseLegendShape;
    private boolean treatLegendShapeAsLine;
    private Font baseLegendTextFont;
    private Paint baseLegendTextPaint;
    private boolean dataBoundsIncludesVisibleSeriesOnly = true;
    private int defaultEntityRadius;
    private AbstractRendererLightweight<? extends AbstractRendererData> renderer;

    public AbstractRendererDataMutable() {
        this.baseSeriesVisible = true;
        this.baseSeriesVisibleInLegend = true;
        this.basePaint = DEFAULT_PAINT;
        this.baseFillPaint = Color.white;
        this.baseOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.baseStroke = DEFAULT_STROKE;
        this.baseOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.baseShape = DEFAULT_SHAPE;
        this.baseItemLabelsVisible = false;
        this.baseItemLabelFont = DEFAULT_BASE_ITEM_LABEL_FONT;
        this.baseItemLabelPaint = Color.black;
        this.basePositiveItemLabelPosition = DEFAULT_BASE_POSITIVE_ITEM_LABEL_POSITION;
        this.baseNegativeItemLabelPosition = DEFAULT_BASE_NEGATIVE_ITEM_LABEL_POSITION;
        this.baseCreateEntities = true;
        this.defaultEntityRadius = 3;
        this.baseLegendShape = null;
        this.treatLegendShapeAsLine = false;
        this.baseLegendTextFont = null;
        this.baseLegendTextPaint = null;
        this.renderer = null;
    }

    public AbstractRendererDataMutable(AbstractRendererData data) {
        this.baseSeriesVisible = data.getBaseSeriesVisible();
        this.baseSeriesVisibleInLegend = data.getBaseSeriesVisibleInLegend();
        this.basePaint = data.getBasePaint();
        this.baseFillPaint = data.getBaseFillPaint();
        this.baseOutlinePaint = data.getBaseOutlinePaint();
        this.baseStroke = data.getBaseStroke();
        this.baseOutlineStroke = data.getBaseOutlineStroke();
        this.baseShape = data.getBaseShape();
        this.baseItemLabelsVisible = data.getBaseItemLabelsVisible();
        this.baseItemLabelFont = data.getBaseItemLabelFont();
        this.baseItemLabelPaint = data.getBaseItemLabelPaint();
        this.basePositiveItemLabelPosition = data.getBasePositiveItemLabelPosition();
        this.baseNegativeItemLabelPosition = data.getBaseNegativeItemLabelPosition();
        this.baseCreateEntities = data.getBaseCreateEntities();
        this.defaultEntityRadius = data.getDefaultEntityRadius();
        this.baseLegendShape = data.getBaseLegendShape();
        this.treatLegendShapeAsLine = data.getTreatLegendShapeAsLine();
        this.baseLegendTextFont = data.getBaseLegendTextFont();
        this.baseLegendTextPaint = data.getBaseLegendTextPaint();
        this.renderer = null;
    }

    @Override
    public boolean isImmutable()
    {
        return false;
    }

    @Override
    public boolean getBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }

    public void setBaseSeriesVisible(boolean visible) {
        setBaseSeriesVisible(visible, true);
    }

    public void setBaseSeriesVisible(boolean visible, boolean notify) {
        this.baseSeriesVisible = visible;
        if (notify) {           
            notifyRendererOfDataChange(true);
        }
    }

    @Override
    public boolean getBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }

    public void setBaseSeriesVisibleInLegend(boolean visible) {
        setBaseSeriesVisibleInLegend(visible, true);
    }

    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify) {
        this.baseSeriesVisibleInLegend = visible;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBasePaint() {
        return this.basePaint;
    }

    public void setBasePaint(Paint paint) {
        // defer argument checking...
        setBasePaint(paint, true);
    }

    public void setBasePaint(Paint paint, boolean notify) {
        this.basePaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseFillPaint() {
        return this.baseFillPaint;
    }

    public void setBaseFillPaint(Paint paint) {
        // defer argument checking...
        setBaseFillPaint(paint, true);
    }

    public void setBaseFillPaint(Paint paint, boolean notify) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseFillPaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseOutlinePaint() {
        return this.baseOutlinePaint;
    }

    public void setBaseOutlinePaint(Paint paint) {
        // defer argument checking...
        setBaseOutlinePaint(paint, true);
    }

    public void setBaseOutlinePaint(Paint paint, boolean notify) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseOutlinePaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Stroke getBaseStroke() {
        return this.baseStroke;
    }

    public void setBaseStroke(Stroke stroke) {
        // defer argument checking...
        setBaseStroke(stroke, true);
    }

    public void setBaseStroke(Stroke stroke, boolean notify) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseStroke = stroke;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Stroke getBaseOutlineStroke() {
        return this.baseOutlineStroke;
    }

    public void setBaseOutlineStroke(Stroke stroke) {
        setBaseOutlineStroke(stroke, true);
    }

    public void setBaseOutlineStroke(Stroke stroke, boolean notify) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseOutlineStroke = stroke;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }


    @Override
    public Shape getBaseShape() {
        return this.baseShape;
    }

    public void setBaseShape(Shape shape) {
        // defer argument checking...
        setBaseShape(shape, true);
    }

    public void setBaseShape(Shape shape, boolean notify) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        this.baseShape = shape;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public boolean getBaseItemLabelsVisible() {
        return this.baseItemLabelsVisible;
    }

    public void setBaseItemLabelsVisible(boolean visible) {
        setBaseItemLabelsVisible(BooleanUtilities.valueOf(visible));
    }

    public void setBaseItemLabelsVisible(Boolean visible, boolean notify) {
        this.baseItemLabelsVisible = visible;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Font getBaseItemLabelFont() {
        return this.baseItemLabelFont;
    }

    public void setBaseItemLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        setBaseItemLabelFont(font, true);
    }

    public void setBaseItemLabelFont(Font font, boolean notify) {
        this.baseItemLabelFont = font;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Paint getBaseItemLabelPaint() {
        return this.baseItemLabelPaint;
    }

    public void setBaseItemLabelPaint(Paint paint) {
        // defer argument checking...
        setBaseItemLabelPaint(paint, true);
    }

    public void setBaseItemLabelPaint(Paint paint, boolean notify) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseItemLabelPaint = paint;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }


    @Override
    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return this.basePositiveItemLabelPosition;
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position) {
        // defer argument checking...
        setBasePositiveItemLabelPosition(position, true);
    }

    public void setBasePositiveItemLabelPosition(ItemLabelPosition position,
            boolean notify) {
        if (position == null) {
            throw new IllegalArgumentException("Null 'position' argument.");
        }
        this.basePositiveItemLabelPosition = position;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return this.baseNegativeItemLabelPosition;
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position) {
        setBaseNegativeItemLabelPosition(position, true);
    }

    public void setBaseNegativeItemLabelPosition(ItemLabelPosition position,
            boolean notify) {
        if (position == null) {
            throw new IllegalArgumentException("Null 'position' argument.");
        }
        this.baseNegativeItemLabelPosition = position;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public double getItemLabelAnchorOffset() {
        return this.itemLabelAnchorOffset;
    }

    public void setItemLabelAnchorOffset(double offset) {
        this.itemLabelAnchorOffset = offset;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getBaseCreateEntities() {
        return this.baseCreateEntities;
    }

    public void setBaseCreateEntities(boolean create) {
        setBaseCreateEntities(create, true);
    }

    public void setBaseCreateEntities(boolean create, boolean notify) {
        this.baseCreateEntities = create;
        if (notify) {
            notifyRendererOfDataChange();
        }
    }

    @Override
    public int getDefaultEntityRadius() {
        return this.defaultEntityRadius;
    }

    public void setDefaultEntityRadius(int radius) {
        this.defaultEntityRadius = radius;
    }

    @Override
    public Shape getBaseLegendShape() {
        return this.baseLegendShape;
    }

    public void setBaseLegendShape(Shape shape) {
        this.baseLegendShape = shape;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getTreatLegendShapeAsLine() {
        return this.treatLegendShapeAsLine;
    }

    public void setTreatLegendShapeAsLine(boolean treatAsLine) {
        if (this.treatLegendShapeAsLine != treatAsLine) {
            this.treatLegendShapeAsLine = treatAsLine;
            notifyRendererOfDataChange();
        }
    }

    @Override
    public Font getBaseLegendTextFont() {
        return this.baseLegendTextFont;
    }

    public void setBaseLegendTextFont(Font font) {
        this.baseLegendTextFont = font;
        notifyRendererOfDataChange();
    }

    @Override
    public Paint getBaseLegendTextPaint() {
        return this.baseLegendTextPaint;
    }

    public void setBaseLegendTextPaint(Paint paint) {
        this.baseLegendTextPaint = paint;
        notifyRendererOfDataChange();
    }

    @Override
    public boolean getDataBoundsIncludesVisibleSeriesOnly() {
        return this.dataBoundsIncludesVisibleSeriesOnly;
    }

    public void setDataBoundsIncludesVisibleSeriesOnly(boolean visibleOnly) {
        this.dataBoundsIncludesVisibleSeriesOnly = visibleOnly;
        notifyRendererOfDataChange(true);
    }


    @Override
    public void registerRendererIfNecessary(AbstractRendererLightweight<?> renderer){
        this.renderer = renderer;
    }


    @Override
    public void deregisterRendererIfNecessary(AbstractRendererLightweight<?> renderer)
    {
        if(this.renderer == renderer)
        {
            this.renderer = null;
        }
    }

    protected void notifyRendererOfDataChange() 
    {
        if(this.renderer != null)
        {
            this.renderer.dataChanged();
        }
    }

    protected void notifyRendererOfDataChange(boolean seriesVisibilityChanged) 
    {
        if(this.renderer != null)
        {
            this.renderer.dataChanged(seriesVisibilityChanged);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractRendererDataMutable)) {
            return false;
        }
        AbstractRendererDataMutable that = (AbstractRendererDataMutable) obj;
        if (this.dataBoundsIncludesVisibleSeriesOnly
                != that.dataBoundsIncludesVisibleSeriesOnly) {
            return false;
        }
        if (this.treatLegendShapeAsLine != that.treatLegendShapeAsLine) {
            return false;
        }
        if (this.defaultEntityRadius != that.defaultEntityRadius) {
            return false;
        }


        if (this.baseSeriesVisible != that.baseSeriesVisible) {
            return false;
        }

        if (this.baseSeriesVisibleInLegend != that.baseSeriesVisibleInLegend) {
            return false;
        }

        if (!PaintUtilities.equal(this.basePaint, that.basePaint)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseFillPaint, that.baseFillPaint)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseOutlinePaint,
                that.baseOutlinePaint)) {
            return false;
        }

        if (!ObjectUtilities.equal(this.baseStroke, that.baseStroke)) {
            return false;
        }

        if (!ObjectUtilities.equal(
                this.baseOutlineStroke, that.baseOutlineStroke)
                ) {
            return false;
        }

        if (!ShapeUtilities.equal(this.baseShape, that.baseShape)) {
            return false;
        }


        if (this.baseItemLabelsVisible !=
                that.baseItemLabelsVisible) {
            return false;
        }

        if (!ObjectUtilities.equal(this.baseItemLabelFont,
                that.baseItemLabelFont)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseItemLabelPaint,
                that.baseItemLabelPaint)) {
            return false;
        }

        if (!ObjectUtilities.equal(this.basePositiveItemLabelPosition,
                that.basePositiveItemLabelPosition)) {
            return false;
        }

        if (!ObjectUtilities.equal(this.baseNegativeItemLabelPosition,
                that.baseNegativeItemLabelPosition)) {
            return false;
        }
        if (this.itemLabelAnchorOffset != that.itemLabelAnchorOffset) {
            return false;
        }


        if (this.baseCreateEntities != that.baseCreateEntities) {
            return false;
        }

        if (!ShapeUtilities.equal(this.baseLegendShape,
                that.baseLegendShape)) {
            return false;
        }

        if (!ObjectUtilities.equal(this.baseLegendTextFont,
                that.baseLegendTextFont)) {
            return false;
        }

        if (!PaintUtilities.equal(this.baseLegendTextPaint,
                that.baseLegendTextPaint)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hashcode for the renderer.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode(result, this.baseSeriesVisible);
        result = HashUtilities.hashCode(result, this.baseSeriesVisibleInLegend);
        result = HashUtilities.hashCode(result, this.basePaint);
        result = HashUtilities.hashCode(result, this.baseFillPaint);
        result = HashUtilities.hashCode(result, this.baseOutlinePaint);
        result = HashUtilities.hashCode(result, this.baseStroke);
        result = HashUtilities.hashCode(result, this.baseOutlineStroke);

        return result;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractRendererDataMutable clone = (AbstractRendererDataMutable) super.clone();

        if (this.baseShape != null) {
            clone.baseShape = ShapeUtilities.clone(this.baseShape);
        }

        clone.renderer = null;
        return clone;
    }
}

