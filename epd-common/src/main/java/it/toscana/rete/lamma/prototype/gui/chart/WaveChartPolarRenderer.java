package it.toscana.rete.lamma.prototype.gui.chart;

import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class WaveChartPolarRenderer extends DefaultPolarItemRenderer {
    /**
     * Wave height color palette
     */
    public static List<Color> colors = new ArrayList<Color>(Arrays.asList(
            new Color(1, 255, 132),
            new Color(1, 255, 255),
            new Color(1, 221, 221),
            new Color(99, 99, 255),
            new Color(4, 1, 255),
            new Color(132, 0, 148),
            new Color(182, 25, 157),
            new Color(212, 100, 195),
            new Color(206, 3, 0),
            new Color(255, 4, 0),
            new Color(255, 195, 0),
            new Color(255, 240, 90),
            new Color(210, 210, 210),
            new Color(185, 185, 185),
            new Color(160, 160, 160),
            new Color(90, 90, 90)
    ));
    /**
     * Wave height classes
     */
    public static double[] lookupBounds = {0.1, 0.3, 0.5, 0.8, 1.25, 1.6, 2, 2.5, 3,4,5,6,7,8,9};

    /**
     * Wave height classes
     */
    public static double[] tickAngle = {22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5};

    public WaveChartPolarRenderer() {
        super();
    }

    /**
     * Plots the data for a given series.
     * It add a circle with direction as x coordinate
     * period as y coordinate and circle radius as
     * height value
     * Draw the center as the configured shape for each series
     * and add an arrow toward the center of the polar chart
     *
     * @param g2          the drawing surface.
     * @param dataArea    the data area.
     * @param info        collects plot rendering info.
     * @param plot        the plot.
     * @param dataset     the dataset.
     * @param seriesIndex the series index.
     */
    @Override
    public void drawSeries(Graphics2D g2, Rectangle2D dataArea,
                           PlotRenderingInfo info, PolarPlot plot, XYDataset dataset,
                           int seriesIndex) {

        final int numPoints = dataset.getItemCount(seriesIndex);
        if (numPoints == 0 ) {
            return;
        }
        // Devo creare un cerchi e non un polygon


        GeneralPath poly = null;
        ValueAxis axis = plot.getAxisForDataset(plot.indexOf(dataset));
        // Le serie sono monopuntuali
        double theta = dataset.getXValue(seriesIndex, 0);
        double radius = dataset.getYValue(seriesIndex, 0);
        double height = Double.NaN;

        if (dataset instanceof XYZDataset) {
            XYZDataset xyzData = (XYZDataset) dataset;
            height = xyzData.getZValue(seriesIndex, 0);

        }
        if (!Double.isNaN(height) && height > 0.1) {
            Double sHeight = lookupHeightSize(height);
            Point p = plot.translateToJava2D(theta, radius, axis, dataArea);
            Shape shape = getCircleShape(p, sHeight + 5);
            Shape center = ShapeUtils.createTranslatedShape(
                    getItemShape(seriesIndex, 2), p.getX(),  p.getY());
            Point pc = moveToCenter(p, theta, 5);
            Shape arrow = ShapeUtils.rotateShape(getArrow(pc),Math.toRadians(FuelConsumptionCalculator.reverseAngle(theta)),(float) pc.getX(), (float) pc.getY());

            g2.setPaint(lookupHeightColor(height)); // Colora da prendere la palet
            g2.setStroke(lookupSeriesStroke(seriesIndex));
            if (isSeriesFilled(seriesIndex)) {
                Composite savedComposite = g2.getComposite();
                g2.setComposite(getFillComposite());
                g2.fill(shape);
                g2.setPaint(Color.BLACK);
                g2.draw(shape);
                g2.setComposite(savedComposite);
                g2.draw(center);
                g2.draw(arrow);

                if (getDrawOutlineWhenFilled()) {
                    // draw the outline of the filled polygon
                    g2.setPaint(lookupSeriesOutlinePaint(seriesIndex));
                }
            } else {
                // just the lines, no filling
                g2.draw(shape);
            }
            // draw the item shapes
            if (getShapesVisible()) {
                // setup for collecting optional entity info...
                EntityCollection entities = null;
                if (info != null) {
                    entities = info.getOwner().getEntityCollection();
                }

                // add an entity for the item, but only if it falls within the
                // data area...
                if (entities != null && ShapeUtils.isPointInRect(dataArea, p.getX(),
                        p.getY())) {
                    addEntity(entities, shape, dataset, seriesIndex, 0, p.getX(), p.getY());
                }
            }

        }
    }

    /**
     * Create a circle
     * @param center The circle center
     * @param r Circle radius
     * @return Circle as Shape
     */
    public static Shape getCircleShape(Point center, double r) {
        return new Ellipse2D.Double(center.getX() - r / 2,
                center.getY() - r / 2, r, r);
    }

    /**
     * Creata an arrow with the origin in the given point and north oriented
     * @param p Point of the arrow origin
     * @return
     */
    public Shape getArrow(Point p) {

        GeneralPath poly  = new GeneralPath();
        poly.moveTo(p.getX(), p.getY());
        poly.lineTo(p.getX(), p.getY() - 20);
        poly.lineTo(p.getX() - 3, p.getY() - 17);
        poly.moveTo(p.getX(), p.getY() - 20);
        poly.lineTo(p.getX() + 3, p.getY() - 17);
        return poly;
    }

    /**
     *  Translete a point along a passed direction of the passed amount of pixels
     * @param p Point to translate
     * @param theta direction
     * @param pixels number of pixels to be translated
     * @return
     */
    public Point moveToCenter(Point p, double theta, double pixels) {
        double y = p.getY() + pixels * Math.cos(Math.toRadians(theta));
        double x = p.getX() - pixels * Math.sin(Math.toRadians(theta));
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    /**
     * Adapt the legend item
     * @param series
     * @return
     */
    public LegendItem getLegendItem(int series, double height) {
        Color c;
        if(!Double.isNaN(height) && height > 0.1) {
            c = lookupHeightColor(height);
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 204);
        }else {
             c = new Color(0000, true);
        }
        LegendItem legendItem = super.getLegendItem(series);
        if (legendItem != null) {
            legendItem.setLineVisible(false);

            legendItem.setFillPaint(c);
            legendItem.setShape(scaleShape(legendItem.getShape(), 2));
        }
        return legendItem;
    }

    /**
     * Scale the wave height
     * @param h Wave height
     * @return Scaled wave height
     */
    private double lookupHeightSize(double h) {
        if(h<lookupBounds[0])
            return 5;
        else
            for(int i = 1; i < lookupBounds.length; i++){
                if( h >= lookupBounds[i-1] && h < lookupBounds[i]){
                    return i * 5;
                }
        }
        return (lookupBounds.length - 1) * 5;
    }

    /**
     *
     * @param h wave height
     * @return Wave height color
     */
    public static Color lookupHeightColor(double h) {
        if(h<lookupBounds[0])
            return colors.get(0);
        else
            for(int i = 1; i < lookupBounds.length; i++){
                if( h >= lookupBounds[i-1] && h < lookupBounds[i]){
                    return colors.get(i);
                }
            }
        return colors.get(lookupBounds.length);

    }

    /**
     * Draw the angular gridlines - the spokes.
     *
     * @param g2  the drawing surface.
     * @param plot  the plot ({@code null} not permitted).
     * @param ticks  the ticks ({@code null} not permitted).
     * @param dataArea  the data area.
     */
    @Override
    public void drawAngularGridLines(Graphics2D g2, PolarPlot plot,
                                     List ticks, Rectangle2D dataArea) {

        g2.setFont(plot.getAngleLabelFont());
        g2.setStroke(plot.getAngleGridlineStroke());
        g2.setPaint(plot.getAngleGridlinePaint());

        ValueAxis axis = plot.getAxis();
        double centerValue, outerValue;
        if (axis.isInverted()) {
            outerValue = axis.getLowerBound();
            centerValue = axis.getUpperBound();
        } else {
            outerValue = axis.getUpperBound();
            centerValue = axis.getLowerBound();
        }
        Point center = plot.translateToJava2D(0, centerValue, axis, dataArea);
        Iterator iterator = ticks.iterator();
        while (iterator.hasNext()) {
            NumberTick tick = (NumberTick) iterator.next();
            double tickVal = tick.getNumber().doubleValue();
            Point p = plot.translateToJava2D(tickVal, outerValue, axis,
                    dataArea);
            g2.setPaint(plot.getAngleGridlinePaint());
            g2.drawLine(center.x, center.y, p.x, p.y);
            if (plot.isAngleLabelsVisible() && isAngleLabelVisible(tickVal)) {
                int x = p.x;
                int y = p.y;
                g2.setPaint(plot.getAngleLabelPaint());
                TextUtils.drawAlignedString(tick.getText(), g2, x, y,
                        tick.getTextAnchor());
            }
        }
    }



        /**
         * Draw the radial gridlines - the rings.
         *
         * @param g2  the drawing surface ({@code null} not permitted).
         * @param plot  the plot ({@code null} not permitted).
         * @param radialAxis  the radial axis ({@code null} not permitted).
         * @param ticks  the ticks ({@code null} not permitted).
         * @param dataArea  the data area.
         */
        @Override
        public void drawRadialGridLines(Graphics2D g2, PolarPlot plot,
                ValueAxis radialAxis, List ticks, Rectangle2D dataArea) {

            Args.nullNotPermitted(radialAxis, "radialAxis");
            g2.setFont(radialAxis.getTickLabelFont());
            g2.setPaint(plot.getRadiusGridlinePaint());
            g2.setStroke(plot.getRadiusGridlineStroke());

            double centerValue;
            if (radialAxis.isInverted()) {
                centerValue = radialAxis.getUpperBound();
            } else {
                centerValue = radialAxis.getLowerBound();
            }
            Point center = plot.translateToJava2D(0, centerValue, radialAxis, dataArea);

            Iterator iterator = ticks.iterator();
            while (iterator.hasNext()) {
                NumberTick tick = (NumberTick) iterator.next();
                double angleDegrees = plot.isCounterClockwise()
                        ? plot.getAngleOffset() : -plot.getAngleOffset();
                Point p = plot.translateToJava2D(angleDegrees,  tick.getNumber().doubleValue(), radialAxis, dataArea);
                int r = p.x - center.x;
                int upperLeftX = center.x - r;
                int upperLeftY = center.y - r;
                int d = 2 * r;
                Ellipse2D ring = new Ellipse2D.Double(upperLeftX, upperLeftY, d, d);
                g2.setPaint(plot.getRadiusGridlinePaint());
                g2.draw(ring);
            }
        }

    private boolean isAngleLabelVisible(double tickVal) {
        for(int i = 0; i< tickAngle.length; i++) {
            if(tickAngle[i] == tickVal)
                return false;
        }
        return true;
    }
    public static Shape scaleShape(Shape base, double scaleFactor) {
        if (base == null) {
            return null;
        }
        final AffineTransform scale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        final Shape result = scale.createTransformedShape(base);
        return result;
    }
}
