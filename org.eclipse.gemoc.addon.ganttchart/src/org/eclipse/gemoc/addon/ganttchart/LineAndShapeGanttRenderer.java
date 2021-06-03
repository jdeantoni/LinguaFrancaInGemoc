/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * Copyright 2008 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.gemoc.addon.ganttchart;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.ObjectUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.category.CategoryDataset;


/**
 * A {@link CategoryItemRenderer} that draws shapes for each data item, and
 * lines between data items (for use with the {@link CategoryPlot} class).
 */
public class LineAndShapeGanttRenderer extends AbstractCategoryItemRenderer
                                       implements Cloneable, PublicCloneable,
                                                  Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -197749519869226398L;

    /** A flag that controls whether or not lines are visible for ALL series. */
    private Boolean linesVisible;

    /**
     * A table of flags that control (per series) whether or not lines are
     * visible.
     */
    private BooleanList seriesLinesVisible;

    /**
     * A flag indicating whether or not lines are drawn between non-null
     * points.
     */
    private boolean baseLinesVisible;

    /**
     * A flag that controls whether or not shapes are visible for ALL series.
     */
    private Boolean shapesVisible;

    /**
     * A table of flags that control (per series) whether or not shapes are
     * visible.
     */
    private BooleanList seriesShapesVisible;

    /** The default value returned by the getShapeVisible() method. */
    private boolean baseShapesVisible;

    /** A flag that controls whether or not shapes are filled for ALL series. */
    private Boolean shapesFilled;

    /**
     * A table of flags that control (per series) whether or not shapes are
     * filled.
     */
    private BooleanList seriesShapesFilled;

    /** The default value returned by the getShapeFilled() method. */
    private boolean baseShapesFilled;

    /**
     * A flag that controls whether the fill paint is used for filling
     * shapes.
     */
    private boolean useFillPaint;

    /** A flag that controls whether outlines are drawn for shapes. */
    private boolean drawOutlines;

    /**
     * A flag that controls whether the outline paint is used for drawing shape
     * outlines - if not, the regular series paint is used.
     */
    private boolean useOutlinePaint;

    /**
     * Creates a renderer with both lines and shapes visible by default.
     */
    public LineAndShapeGanttRenderer() {
        this(true, true);
    }

    /**
     * Creates a new renderer with lines and/or shapes visible.
     *
     * @param lines  draw lines?
     * @param shapes  draw shapes?
     */
    public LineAndShapeGanttRenderer(boolean lines, boolean shapes) {
        super();
        this.linesVisible = null;
        this.seriesLinesVisible = new BooleanList();
        this.baseLinesVisible = lines;
        this.shapesVisible = null;
        this.seriesShapesVisible = new BooleanList();
        this.baseShapesVisible = shapes;
        this.shapesFilled = null;
        this.seriesShapesFilled = new BooleanList();
        this.baseShapesFilled = true;
        this.useFillPaint = false;
        this.drawOutlines = true;
        this.useOutlinePaint = false;
    }

    // LINES VISIBLE

    /**
     * Returns the flag used to control whether or not the line for an item is
     * visible.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public boolean getItemLineVisible(int series, int item) {
        Boolean flag = this.linesVisible;
        if (flag == null) {
            flag = getSeriesLinesVisible(series);
        }
        if (flag != null) {
            return flag.booleanValue();
        }
        else {
            return this.baseLinesVisible;
        }
    }

    /**
     * Returns a flag that controls whether or not lines are drawn for ALL
     * series.  If this flag is <code>null</code>, then the "per series"
     * settings will apply.
     *
     * @return A flag (possibly <code>null</code>).
     */
    public Boolean getLinesVisible() {
        return this.linesVisible;
    }

    /**
     * Sets a flag that controls whether or not lines are drawn between the
     * items in ALL series, and sends a {@link RendererChangeEvent} to all
     * registered listeners.  You need to set this to <code>null</code> if you
     * want the "per series" settings to apply.
     *
     * @param visible  the flag (<code>null</code> permitted).
     */
    public void setLinesVisible(Boolean visible) {
        this.linesVisible = visible;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Sets a flag that controls whether or not lines are drawn between the
     * items in ALL series, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param visible  the flag.
     */
    public void setLinesVisible(boolean visible) {
        setLinesVisible(Boolean.valueOf(visible));
    }

    /**
     * Returns the flag used to control whether or not the lines for a series
     * are visible.
     *
     * @param series  the series index (zero-based).
     *
     * @return The flag (possibly <code>null</code>).
     */
    public Boolean getSeriesLinesVisible(int series) {
        return this.seriesLinesVisible.getBoolean(series);
    }

    /**
     * Sets the 'lines visible' flag for a series.
     *
     * @param series  the series index (zero-based).
     * @param flag  the flag (<code>null</code> permitted).
     */
    public void setSeriesLinesVisible(int series, Boolean flag) {
        this.seriesLinesVisible.setBoolean(series, flag);
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Sets the 'lines visible' flag for a series.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag.
     */
    public void setSeriesLinesVisible(int series, boolean visible) {
        setSeriesLinesVisible(series, Boolean.valueOf(visible));
    }

    /**
     * Returns the base 'lines visible' attribute.
     *
     * @return The base flag.
     */
    public boolean getBaseLinesVisible() {
        return this.baseLinesVisible;
    }

    /**
     * Sets the base 'lines visible' flag.
     *
     * @param flag  the flag.
     */
    public void setBaseLinesVisible(boolean flag) {
        this.baseLinesVisible = flag;
        notifyListeners(new RendererChangeEvent(this));
    }

    // SHAPES VISIBLE

    /**
     * Returns the flag used to control whether or not the shape for an item is
     * visible.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public boolean getItemShapeVisible(int series, int item) {
        Boolean flag = this.shapesVisible;
        if (flag == null) {
            flag = getSeriesShapesVisible(series);
        }
        if (flag != null) {
            return flag.booleanValue();
        }
        else {
            return this.baseShapesVisible;
        }
    }

    /**
     * Returns the flag that controls whether the shapes are visible for the
     * items in ALL series.
     *
     * @return The flag (possibly <code>null</code>).
     */
    public Boolean getShapesVisible() {
        return this.shapesVisible;
    }

    /**
     * Sets the 'shapes visible' for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag (<code>null</code> permitted).
     */
    public void setShapesVisible(Boolean visible) {
        this.shapesVisible = visible;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Sets the 'shapes visible' for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     */
    public void setShapesVisible(boolean visible) {
        setShapesVisible(Boolean.valueOf(visible));
    }

    /**
     * Returns the flag used to control whether or not the shapes for a series
     * are visible.
     *
     * @param series  the series index (zero-based).
     *
     * @return A boolean.
     */
    public Boolean getSeriesShapesVisible(int series) {
        return this.seriesShapesVisible.getBoolean(series);
    }

    /**
     * Sets the 'shapes visible' flag for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag.
     */
    public void setSeriesShapesVisible(int series, boolean visible) {
        setSeriesShapesVisible(series, Boolean.valueOf(visible));
    }

    /**
     * Sets the 'shapes visible' flag for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param flag  the flag.
     */
    public void setSeriesShapesVisible(int series, Boolean flag) {
        this.seriesShapesVisible.setBoolean(series, flag);
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Returns the base 'shape visible' attribute.
     *
     * @return The base flag.
     */
    public boolean getBaseShapesVisible() {
        return this.baseShapesVisible;
    }

    /**
     * Sets the base 'shapes visible' flag.
     *
     * @param flag  the flag.
     */
    public void setBaseShapesVisible(boolean flag) {
        this.baseShapesVisible = flag;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Returns <code>true</code> if outlines should be drawn for shapes, and
     * <code>false</code> otherwise.
     *
     * @return A boolean.
     */
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }

    /**
     * Sets the flag that controls whether outlines are drawn for
     * shapes, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     * <P>
     * In some cases, shapes look better if they do NOT have an outline, but
     * this flag allows you to set your own preference.
     *
     * @param flag  the flag.
     */
    public void setDrawOutlines(boolean flag) {
        this.drawOutlines = flag;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Returns the flag that controls whether the outline paint is used for
     * shape outlines.  If not, the regular series paint is used.
     *
     * @return A boolean.
     */
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }

    /**
     * Sets the flag that controls whether the outline paint is used for shape
     * outlines.
     *
     * @param use  the flag.
     */
    public void setUseOutlinePaint(boolean use) {
        this.useOutlinePaint = use;
    }

    // SHAPES FILLED

    /**
     * Returns the flag used to control whether or not the shape for an item
     * is filled. The default implementation passes control to the
     * <code>getSeriesShapesFilled</code> method. You can override this method
     * if you require different behaviour.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public boolean getItemShapeFilled(int series, int item) {
        return getSeriesShapesFilled(series);
    }

    /**
     * Returns the flag used to control whether or not the shapes for a series
     * are filled.
     *
     * @param series  the series index (zero-based).
     *
     * @return A boolean.
     */
    public boolean getSeriesShapesFilled(int series) {

        // return the overall setting, if there is one...
        if (this.shapesFilled != null) {
            return this.shapesFilled.booleanValue();
        }

        // otherwise look up the paint table
        Boolean flag = this.seriesShapesFilled.getBoolean(series);
        if (flag != null) {
            return flag.booleanValue();
        }
        else {
            return this.baseShapesFilled;
        }

    }

    /**
     * Returns the flag that controls whether or not shapes are filled for
     * ALL series.
     *
     * @return A Boolean.
     */
    public Boolean getShapesFilled() {
        return this.shapesFilled;
    }

    /**
     * Sets the 'shapes filled' for ALL series.
     *
     * @param filled  the flag.
     */
    public void setShapesFilled(boolean filled) {
        if (filled) {
            setShapesFilled(Boolean.TRUE);
        }
        else {
            setShapesFilled(Boolean.FALSE);
        }
    }

    /**
     * Sets the 'shapes filled' for ALL series.
     *
     * @param filled  the flag (<code>null</code> permitted).
     */
    public void setShapesFilled(Boolean filled) {
        this.shapesFilled = filled;
    }

    /**
     * Sets the 'shapes filled' flag for a series.
     *
     * @param series  the series index (zero-based).
     * @param filled  the flag.
     */
    public void setSeriesShapesFilled(int series, Boolean filled) {
        this.seriesShapesFilled.setBoolean(series, filled);
    }

    /**
     * Sets the 'shapes filled' flag for a series.
     *
     * @param series  the series index (zero-based).
     * @param filled  the flag.
     */
    public void setSeriesShapesFilled(int series, boolean filled) {
        this.seriesShapesFilled.setBoolean(
            series, Boolean.valueOf(filled)
        );
    }

    /**
     * Returns the base 'shape filled' attribute.
     *
     * @return The base flag.
     */
    public boolean getBaseShapesFilled() {
        return this.baseShapesFilled;
    }

    /**
     * Sets the base 'shapes filled' flag.
     *
     * @param flag  the flag.
     */
    public void setBaseShapesFilled(boolean flag) {
        this.baseShapesFilled = flag;
    }

    /**
     * Returns <code>true</code> if the renderer should use the fill paint
     * setting to fill shapes, and <code>false</code> if it should just
     * use the regular paint.
     *
     * @return A boolean.
     */
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }

    /**
     * Sets the flag that controls whether the fill paint is used to fill
     * shapes, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param flag  the flag.
     */
    public void setUseFillPaint(boolean flag) {
        this.useFillPaint = flag;
        notifyListeners(new RendererChangeEvent(this));
    }

    /**
     * Returns a legend item for a series.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return The legend item.
     */
    public LegendItem getLegendItem(int datasetIndex, int series) {

        CategoryPlot cp = getPlot();
        if (cp == null) {
            return null;
        }

        if (isSeriesVisible(series) && isSeriesVisibleInLegend(series)) {
            CategoryDataset dataset;
            dataset = cp.getDataset(datasetIndex);
            String label = getLegendItemLabelGenerator().generateLabel(
                dataset, series
            );
            String toolTipText = null;
            if (getLegendItemToolTipGenerator() != null) {
                toolTipText = getLegendItemToolTipGenerator().generateLabel(
                    dataset, series
                );
            }
            String urlText = null;
            if (getLegendItemURLGenerator() != null) {
                urlText = getLegendItemURLGenerator().generateLabel(
                    dataset, series
                );
            }
            Shape shape = lookupLegendShape(series);
            Paint paint = lookupSeriesPaint(series);
            Paint fillPaint = paint;
            //(this.useFillPaint
            //    ? getItemFillPaint(series, 0) : paint);
            boolean shapeOutlineVisible = this.drawOutlines;
            Paint outlinePaint = (this.useOutlinePaint
                ? getItemOutlinePaint(series, 0) : paint);
            Stroke outlineStroke = lookupSeriesStroke(series);
            boolean lineVisible = getItemLineVisible(series, 0);
            boolean shapeVisible = getItemShapeVisible(series, 0);
            return new LegendItem(label, label, toolTipText,
                    urlText, shapeVisible, shape, getItemShapeFilled(series, 0),
                    fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke,
                    lineVisible, new Line2D.Double(-7.0, 0.0, 7.0, 0.0),
                    getItemStroke(series, 0), getItemPaint(series, 0));
        }
        return null;

    }

    /**
     * This renderer uses two passes to draw the data.
     *
     * @return The pass count (<code>2</code> for this renderer).
     */
    public int getPassCount() {
        return 2;
    }

    /**
     * Draw a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area in which the data is drawn.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
            int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(row, column)) {
            return;
        }

        // do nothing if both the line and shape are not visible
        if (!getItemLineVisible(row, column)
                && !getItemShapeVisible(row, column)) {
            return;
        }

        // nothing is drawn for null...
        Number v = dataset.getValue(row, column);
        if (v == null) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();

        // current data point...
        double xMargin = 7.0;
        double yMargin = 3.0;
        double x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                dataArea, plot.getDomainAxisEdge()) - xMargin;
        double value = v.doubleValue();
        double y1 = rangeAxis.valueToJava2D(value, dataArea, 
                plot.getRangeAxisEdge());

        Number previousValue = null;
        int previousColumn;
        for (int i = 1; ; i++) {
            previousColumn = column - i;
            if (previousColumn < 0) {
                break;
            }
            previousValue = dataset.getValue(row, previousColumn);
            if (previousColumn == 0 || previousValue != null) {
                break;
            }
        }

        if (pass == 0 && getItemLineVisible(row, column)) {
            if (column != 0) {
                if (previousValue != null) {
                    // previous data point...
                    double previous = previousValue.doubleValue();
                    double x0 = domainAxis.getCategoryMiddle(previousColumn,
                            getColumnCount(), dataArea,
                            plot.getDomainAxisEdge());
                    double y0 = rangeAxis.valueToJava2D(previous, dataArea,
                            plot.getRangeAxisEdge());

                    Line2D line1 = null;
                    Line2D line2 = null;
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        if (y1 > y0) {
                            line1 = new Line2D.Double(y0 + yMargin, x0,
                                    y0 + yMargin, x1);
                            line2 = new Line2D.Double(y0 + yMargin, x1,
                                    y1 - yMargin, x1);
                        } else if (y1 < y0) {
                            line1 = new Line2D.Double(y0 + yMargin, x0,
                                    y0 + yMargin, x1);
                            line2 = new Line2D.Double(y0 + yMargin, x1,
                                    y1 + yMargin, x1);
                        } else {
                            line1 = new Line2D.Double(y0 + yMargin, x0,
                                    y1 + yMargin, x1);
                        }
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        if (y1 > y0) {
                            line1 = new Line2D.Double(x0, y0 + yMargin,
                                    x1, y0 + yMargin);
                            line2 = new Line2D.Double(x1, y0 + yMargin,
                                    x1, y1 - yMargin);
                        } else if (y1 < y0) {
                            line1 = new Line2D.Double(x0, y0 + yMargin,
                                    x1, y0 + yMargin);
                            line2 = new Line2D.Double(x1, y0 + yMargin,
                                    x1, y1 + yMargin);
                        } else {
                            line1 = new Line2D.Double(x0, y0 + yMargin, x1,
                                y1 + yMargin);
                        }
                    }
                    g2.setPaint(getItemPaint(row, column));
                    g2.setStroke(getItemStroke(row, column));
                    g2.draw(line1);
                    if (line2 != null) {
                        g2.draw(line2);
                    }
                }
            }
        }

        if (pass == 1) {
            if (previousValue != null) {
                // previous data point...
                double previous = previousValue.doubleValue();
                double y0 = rangeAxis.valueToJava2D(previous, dataArea,
                        plot.getRangeAxisEdge());

                Shape shape = getItemShape(row, column);
                if (orientation == PlotOrientation.HORIZONTAL) {
                    if (y1 > y0) {
                        shape = ShapeUtils.createTranslatedShape(shape,
                                y1 - yMargin, x1);
                        shape = ShapeUtils.rotateShape(shape,
                                Math.toRadians(-90),
                                (float) y1 - (float) yMargin, (float) x1);
                    } else {
                        shape = ShapeUtils.createTranslatedShape(shape,
                                y1 + yMargin, x1);
                        if (y1 < y0) {
                            shape = ShapeUtils.rotateShape(shape,
                                    Math.toRadians(90),
                                    (float) y1 + (float) yMargin, (float) x1);
                        }
                    }
                } else if (orientation == PlotOrientation.VERTICAL) {
                    if (y1 > y0) {
                        shape = ShapeUtils.createTranslatedShape(shape, x1,
                                y1 - yMargin);
                        shape = ShapeUtils.rotateShape(shape,
                                Math.toRadians(-90),
                                (float) x1, (float) y1 - (float) yMargin);
                    } else {
                        shape = ShapeUtils.createTranslatedShape(shape, x1,
                                y1 + yMargin);
                        if (y1 < y0) {
                            shape = ShapeUtils.rotateShape(shape,
                                    Math.toRadians(90),
                                    (float) x1, (float) y1 + (float) yMargin);
                        }
                    }
                }

                if (getItemShapeVisible(row, column)) {
                    if (getItemShapeFilled(row, column)) {
                        if (this.useFillPaint) {
                            g2.setPaint(getItemFillPaint(row, column));
                        } else {
                            g2.setPaint(getItemPaint(row, column));
                        }
                        g2.fill(shape);
                    }
                    if (this.drawOutlines) {
                        if (this.useOutlinePaint) {
                            g2.setPaint(getItemOutlinePaint(row, column));
                        } else {
                            g2.setPaint(getItemPaint(row, column));
                        }
                        g2.setStroke(getItemOutlineStroke(row, column));
                        g2.draw(shape);
                    }
                }

                // draw the item label if there is one...
                if (isItemLabelVisible(row, column)) {
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        drawItemLabel(g2, orientation, dataset, row, column, y1,
                                x1, (value < 0.0));
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        drawItemLabel(g2, orientation, dataset, row, column, x1,
                                y1, (value < 0.0));
                    }
                }

                // add an item entity, if this information is being collected
                EntityCollection entities = state.getEntityCollection();
                if (entities != null) {
                    addItemEntity(entities, dataset, row, column, shape);
                }
            } else {
                Line2D line1 = null;
                Line2D line2 = null;
                if (orientation == PlotOrientation.HORIZONTAL) {
                    line1 = new Line2D.Double(y1, x1, y1 + yMargin, x1);
                    line2 = new Line2D.Double(y1 + yMargin, x1, y1 + yMargin,
                            x1 + xMargin);
                } else if (orientation == PlotOrientation.VERTICAL) {
                    line1 = new Line2D.Double(x1, y1, x1, y1 + yMargin);
                    line2 = new Line2D.Double(x1, y1 + yMargin, x1 + xMargin,
                            y1 + yMargin);
                }
                g2.setPaint(getItemPaint(row, column));
                g2.setStroke(getItemStroke(row, column));
                g2.draw(line1);
                g2.draw(line2);
            }
        }

    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LineAndShapeGanttRenderer)) {
            return false;
        }

        LineAndShapeGanttRenderer that = (LineAndShapeGanttRenderer) obj;
        if (this.baseLinesVisible != that.baseLinesVisible) {
            return false;
        }
        if (!ObjectUtils.equal(this.seriesLinesVisible,
                that.seriesLinesVisible)) {
            return false;
        }
        if (!ObjectUtils.equal(this.linesVisible, that.linesVisible)) {
            return false;
        }
        if (this.baseShapesVisible != that.baseShapesVisible) {
            return false;
        }
        if (!ObjectUtils.equal(this.seriesShapesVisible,
                that.seriesShapesVisible)) {
            return false;
        }
        if (!ObjectUtils.equal(this.shapesVisible, that.shapesVisible)) {
            return false;
        }
        if (!ObjectUtils.equal(this.shapesFilled, that.shapesFilled)) {
            return false;
        }
        if (!ObjectUtils.equal(
            this.seriesShapesFilled, that.seriesShapesFilled)
        ) {
            return false;
        }
        if (this.baseShapesFilled != that.baseShapesFilled) {
            return false;
        }
        if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  should not happen.
     */
    public Object clone() throws CloneNotSupportedException {
        LineAndShapeGanttRenderer clone =
                (LineAndShapeGanttRenderer) super.clone();
        clone.seriesLinesVisible
            = (BooleanList) this.seriesLinesVisible.clone();
        clone.seriesShapesVisible
            = (BooleanList) this.seriesLinesVisible.clone();
        clone.seriesShapesFilled
            = (BooleanList) this.seriesShapesFilled.clone();
        return clone;
    }

}
