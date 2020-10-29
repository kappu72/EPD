package it.toscana.rete.lamma.prototype.gui.chart;


import dk.dma.epd.common.text.Formatter;
import edu.emory.mathcs.backport.java.util.Arrays;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;
import it.toscana.rete.lamma.prototype.model.Wave;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYZToolTipGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.DefaultXYZDataset;

import javax.swing.*;
import java.awt.*;

import java.awt.geom.AffineTransform;
import java.text.*;
import java.util.ArrayList;
import java.util.TimeZone;


public class WaveSpectrumChart extends JPanel {

    private final SimpleDateFormat df;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private DefaultXYZDataset dataset;
    private int width = 370;
    private int height = 460;
    private WavePlot plot;
    private static String pattern = "dd/MM/yyyy HH:mm";


    public WaveSpectrumChart() {
        super();
        df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        setLayout(new BorderLayout());
        createChart();
    }


    private void createChart() {
        // Create Empty dataset
        dataset = new DefaultXYZDataset();
        //sampleValues();
        setSpectrum(null, new ArrayList<Wave>());
        //Create the plot
        plot = new WavePlot();
        chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        ChartFactory.getChartTheme().apply(chart);

        plot.setDataset(dataset);
        // plot configuration
        plot.setRadiusMinorGridlinesVisible(false); // Remove minor grid lines in radial grid
        plot.setAngleTickUnit(new NumberTickUnit(22.5));
        plot.setBackgroundPaint(Color.white);
        plot.setAngleGridlinePaint(Color.GRAY);
        plot.setRadiusGridlinePaint(Color.GRAY);

        NumberAxis periodAxis = new NumberAxis();

        periodAxis.setLabel("T[sec]");
        periodAxis.setTickUnit(new NumberTickUnit(1));
        periodAxis.setRange(0, 14);
       /* periodAxis.setNumberFormatOverride(new NumberFormat() {
            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;

            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public Number parse(String source, ParsePosition parsePosition) {
                return null;
            }
        });*/
        periodAxis.setAxisLineVisible(false);
        periodAxis.setTickMarksVisible(false);
        periodAxis.setTickLabelInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        plot.setAxis(periodAxis);

        // Configuring the renderer
        WaveChartPolarRenderer renderer = new WaveChartPolarRenderer();
        // Tooltip label generator
        StandardXYZToolTipGenerator tooltip = new StandardXYZToolTipGenerator("{0}: ({1}°, {2}s, {3}m)",
                NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance());
        renderer.setBaseToolTipGenerator(tooltip);

        renderer.setFillComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesFilled(i, true); // wind
        }
        // settings series center item
        Shape[] shapes = DefaultDrawingSupplier.createStandardSeriesShapes();
        renderer.setSeriesShape(0, shapes[1]); // Wind
        renderer.setSeriesShape(1, shapes[9]); // Swell 5
        renderer.setSeriesShape(2, shapes[5]); // Swell 4
        renderer.setSeriesShape(3, shapes[7]); // Swell 3
        renderer.setSeriesShape(4, shapes[2]); // Swell 2
        renderer.setSeriesShape(5, shapes[0]); // Swell 1


        // vanno configurati i colori
        // renderer.setSeriesPaint(0, Color.BLUE);


        plot.setRenderer(renderer);


        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(-1, -1));
        chartPanel.setMouseZoomable(true);
        add(chartPanel, BorderLayout.CENTER);

    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        chartPanel.setPreferredSize(preferredSize);
    }

    /**
     * Create wave spectrum sample values for debugging purpose
     */
    private void sampleValues() {
        Wave wind = new Wave(0.8, 182, 6);
        java.util.ArrayList<Wave> swell = new ArrayList<Wave>(
                Arrays.asList( new Wave[] {
                new Wave(7.2, 162, 7.2),
                new Wave(4.7, 40, 9),
                new Wave(2.4, 300, 7),
                new Wave(1, 260, 6.5),
                new Wave(.4, 360, 10)}));
        setSpectrum(wind, swell);

    }

    /**
     * Set the spectrum values
     * Draw the swell in reverse order
     * @param wind
     * @param swell
     */
    public void setSpectrum(Wave wind, java.util.ArrayList<Wave> swell) {
        if(swell == null) {
            swell = new ArrayList<>();
        }
        if (wind != null) {
            dataset.addSeries("Wind Sea", new double[][]{{wind.getDirection()}, {wind.getPeriod()}, {wind.getHeight()}});
        } else {
            dataset.addSeries("Wind Sea", new double[][]{{}, {}, {}});
        }
        for (int i = 4; i >=0 ; i--) {
            Comparable key = "Swell " + (i + 1);
            try {
                Wave w = swell.get(i);
                dataset.addSeries(key, new double[][]{{w.getDirection()}, {w.getPeriod()}, {w.getHeight()}});
            } catch (IndexOutOfBoundsException e) {
                dataset.addSeries(key, new double[][]{{}, {}, {}});
            }
        }
    }
    private void clearSubtitles() {
        LegendTitle legend = (LegendTitle) chart.getSubtitles().get(0);
        chart.clearSubtitles();
        chart.addLegend(legend);
    }
    /**
     * Draw the chart
     * @param mP Metoc Point forecast
     */
    public void drawMetocPointForecast(MetocPointForecast mP) {
        plot.clearCornerTextItems();
        clearSubtitles();
        if (mP != null) {
            setSpectrum(mP.getWindWave(), (ArrayList<Wave>) mP.getSwellWave());

            chart.addSubtitle(new TextTitle(Formatter.latToPrintable(mP.getLat())
                    .concat("-")
                    .concat(Formatter.lonToPrintable(mP.getLon()))));
            chart.addSubtitle(new TextTitle("L.T. "
                    .concat(df.format(mP.getTime()))));
            if(mP.getWindWave() == null && mP.getSwellWave().size() == 0) {
                chart.addSubtitle(new TextTitle("Partitions N/A"));
            }
            plot.addCornerTextItem("10m Wind: "
                    .concat(Formatter.formatWindSpeed(mP.getWindSpeed().getForecast()))
                    .concat(" ")
                    .concat(Formatter.formatDegrees(mP.getWindDirection().getForecast(), 0)));
            plot.addCornerTextItem("Current: "
                    .concat(Formatter.formatCurrentSpeed(mP.getCurrentSpeed().getForecast()))
                    .concat(" ")
                    .concat(Formatter.formatDegrees(mP.getCurrentDirection().getForecast(), 0))
                    );
            plot.addCornerTextItem("T. Wave: "
                    .concat(Formatter.formatWave(mP.getMeanWave())));
        }else {
            chart.addSubtitle(new TextTitle("Metoc not available"));
            setSpectrum(null, new ArrayList<Wave>());
        }

    }

}
