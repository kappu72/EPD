package it.toscana.rete.lamma.prototype.gui;

import com.bbn.openmap.gui.OMComponentPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.layers.EPDLayerCommon;
import dk.dma.epd.common.prototype.model.route.IRoutesUpdateListener;

import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.dma.epd.common.prototype.settings.EnavSettings;

import it.toscana.rete.lamma.prototype.gui.chart.WaveSpectrumChart;
import it.toscana.rete.lamma.prototype.metocservices.*;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;

import org.geotools.util.DateTimeParser;
import ucar.nc2.time.CalendarDateRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Map;

import java.util.stream.Collectors;


public class MetocPanelCommon extends OMComponentPanel implements PropertyChangeListener, ItemListener, IRoutesUpdateListener, ActionListener {

    private final EnavSettings enavSettings;
    private JPanel panel1;
    private WaveSpectrumChart waveSpectrumChart;
    private JCheckBox metocActivecb;
    private PointMetocProviderSelector pointMetocProviderSelector1;
    private WMSLayerTimeSelector timeSelector;
    private JButton setValBtn;
    protected EPDLayerCommon layer;

    private static String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static DateFormat df;
    private RouteManagerCommon routeManager;
    private LocalMetocService locService;
    private LammaMetocService lammaService;
    private DateTimeParser dateTimeParser = new DateTimeParser();
    private MetocPointForecast mpf;

    /**
     * TODO:: Configurare il settaggio della data dal metopoint nel munu
     */
    public MetocPanelCommon() {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(this.panel1, BorderLayout.CENTER);
        df = new SimpleDateFormat(pattern);
        df.setTimeZone(DateTimeParser.UTC_TZ);
        metocActivecb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (layer != null) {
                    layer.setVisible(metocActivecb.isSelected());
                }

            }
        });
        pointMetocProviderSelector1.addItemListener(this);
        timeSelector.addItemListener(this);
        enavSettings = EPD.getInstance().getSettings().getEnavSettings();
        initValues();
        setValBtn.addActionListener(this);

    }

    // inizializza i valori dei componenti
    private void initValues() {
        updateFileSelector();
        enableMetoc();
    }

    // Create custom ui components
    private void createUIComponents() {
        waveSpectrumChart = new WaveSpectrumChart();
        pointMetocProviderSelector1 = new PointMetocProviderSelector();
        timeSelector = new WMSLayerTimeSelector();
    }

    /**
     * Map click event handler
     *
     * @param point The clicked map point in map coordinates
     */
    public void onMapClick(Point2D point) {
        PointMetocProvider mp = (PointMetocProvider) pointMetocProviderSelector1.getSelectedItem();
        MetocService metocService = getService(mp);
        try {
            metocService.openMetoc(mp.getSettings());
            metocService.openGrids();
            mpf = metocService.readMetocPointForecast(point.getY(), point.getX(), (Date) timeSelector.getSelectedItem(), true);
            waveSpectrumChart.drawMetocPointForecast(mpf);
            if (mpf != null) {
                layer.prepare().add(new PointMetocGraphic(mpf, mp.getSettings(), enavSettings));
                layer.prepare();
            }

        } catch (ShoreServiceException e) {
            e.printStackTrace();
            resetValues();
        }

    }

    public void drawMetoc(MetocPointForecast mpf) {
        this.mpf = mpf;
        waveSpectrumChart.drawMetocPointForecast(mpf);
    }

    /**
     * @param mp Point Metoc Provider
     * @return The Metoc Service
     */
    public MetocService getService(PointMetocProvider mp) {
        if (mp == null) return null;
        if (mp.getType() == MetocProviders.LAMMA.label())
            return lammaService;
        return locService;

    }

    /**
     * Clear all fields value
     */
    private void resetValues() {
        if (waveSpectrumChart != null)
            waveSpectrumChart.drawMetocPointForecast(null);
        if (layer != null)
            layer.prepare().clear();

    }

    /**
     * Activate and deactivate panel functionality
     */
    public void enableMetoc() {
        if (metocActivecb != null) {
            boolean isEnabled = timeSelector != null && timeSelector.getSelectedItem() != null && pointMetocProviderSelector1 != null && pointMetocProviderSelector1.getSelectedItem() != null;
            metocActivecb.setEnabled(isEnabled);
            metocActivecb.setSelected(isEnabled && metocActivecb.isSelected());
        }
    }

    private void initTimes() {

        PointMetocProvider mp = (PointMetocProvider) pointMetocProviderSelector1.getSelectedItem();
        MetocService metocService = getService(mp);
        if (mp != null && metocService != null) {
            try {
                metocService.openMetoc(mp.getSettings());
                CalendarDateRange dateRange = metocService.getGcs().getCalendarDateRange();
                if (timeSelector != null) {
                    if (dateRange != null) {
                        try {
                            Collection<Date> parsed = (Collection<Date>)
                                    dateTimeParser.parse(df.format(dateRange.getStart().toDate())
                                            .concat("/")
                                            .concat(df.format(dateRange.getEnd().toDate())
                                                    .concat("/PT1H")));

                            timeSelector.addTimes(new ArrayList<>(parsed));
                            if (parsed.size() > 0) {
                                timeSelector.setSelectedIndex(0);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        timeSelector.addTimes(new ArrayList<>());
                        timeSelector.setSelectedIndex(-1);
                    }
                }
            } catch (ShoreServiceException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isActive() {
        return metocActivecb.isSelected() && metocActivecb.isEnabled();
    }

    /**
     * @return a list of available metoc providers scanning routes metoc settings
     */
    private java.util.List<PointMetocProvider> getProviders() {
        if (routeManager != null)
            return routeManager.getRoutes().stream()
                    .map(r -> r.getRouteMetocSettings())
                    .filter(s -> s != null)
                    .filter(s -> (s.getProvider().equals(MetocProviders.LOCAL.label()) && s.getLocalMetocFile() != null))
                    .map(s -> new PointMetocProvider(s))
                    .filter(distinctByKey(p -> p.getFile()))
                    .filter(p -> (new File(p.getFile())).exists())
                    .distinct()
                    .collect(Collectors.toList());
        return new ArrayList<PointMetocProvider>();
    }

    // Utility function to filter object in a strem bya a methid
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Update the metoc provider selector
     */
    private void updateFileSelector() {
        if (pointMetocProviderSelector1 != null) {
            pointMetocProviderSelector1.addProviders(getProviders());
            if (pointMetocProviderSelector1.getModel().getSize() > 0)
                pointMetocProviderSelector1.setSelectedIndex(0);
            enableMetoc();
        }
    }

    public void selectTime(Date wmsTime) {
        timeSelector.selectByDate(wmsTime);
    }

    public Date getSelectedTime() {
        return (Date) timeSelector.getSelectedItem();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object o = e.getSource();
        if (o == pointMetocProviderSelector1) {
            initTimes();
            resetValues();
            enableMetoc();
        }
        if (o == timeSelector) {
            resetValues();
            enableMetoc();
        }


    }


    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if (obj instanceof RouteManagerCommon) {
            routeManager = (RouteManagerCommon) obj;
            routeManager.addListener(this);
            updateFileSelector();
        } else if (obj instanceof LocalMetocService) {
            locService = (LocalMetocService) obj;
            initTimes();
        } else if (obj instanceof LammaMetocService) {
            lammaService = (LammaMetocService) obj;
        }

    }

    @Override
    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);
        if (obj instanceof RouteManagerCommon) {
            routeManager = null;
            enableMetoc();
        } else if (obj instanceof LocalMetocService) {
            locService = null;
        } else if (obj instanceof LammaMetocService) {
            lammaService = null;
        }

    }

    @Override
    public void routesChanged(RoutesUpdateEvent e) {
        if (e.is(RoutesUpdateEvent.METOC_SETTINGS_CHANGED) || e.is(RoutesUpdateEvent.ROUTE_METOC_CHANGED) ||
                e.is(RoutesUpdateEvent.ROUTE_ADDED)) {

            pointMetocProviderSelector1.addProviders(getProviders());
            if (pointMetocProviderSelector1.getModel().getSize() > 0)
                pointMetocProviderSelector1.setSelectedIndex(0);
            enableMetoc();
            resetValues();
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setMinimumSize(new Dimension(370, 540));
        panel1.setPreferredSize(new Dimension(370, 540));
        panel1.setRequestFocusEnabled(false);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;84px):grow(0.5),left:4dlu:noGrow,fill:max(d;184px):grow(1.5),fill:d:grow(0.5)", "center:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
        panel2.setMinimumSize(new Dimension(360, 70));
        panel2.setPreferredSize(new Dimension(360, 70));
        panel2.setRequestFocusEnabled(false);
        panel1.add(panel2, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Metoc File");
        CellConstraints cc = new CellConstraints();
        panel2.add(label1, cc.xy(3, 1));
        metocActivecb = new JCheckBox();
        metocActivecb.setText("Active");
        panel2.add(metocActivecb, cc.xy(6, 1));
        panel2.add(pointMetocProviderSelector1, cc.xy(5, 1));
        panel2.add(timeSelector, cc.xy(5, 3));
        final JLabel label2 = new JLabel();
        label2.setText("Time");
        panel2.add(label2, cc.xy(3, 3));
        setValBtn = new JButton();
        setValBtn.setLabel("Values");
        setValBtn.setMaximumSize(new Dimension(250, 30));
        setValBtn.setPreferredSize(new Dimension(-1, 30));
        setValBtn.setText("Values");
        panel2.add(setValBtn, new CellConstraints(6, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 5, 0, 5)));
        waveSpectrumChart.setAutoscrolls(true);
        waveSpectrumChart.setBackground(new Color(-12828863));
        waveSpectrumChart.setFocusCycleRoot(true);
        waveSpectrumChart.setMinimumSize(new Dimension(400, 460));
        waveSpectrumChart.setName("");
        waveSpectrumChart.setOpaque(true);
        waveSpectrumChart.setPreferredSize(new Dimension(-1, -1));
        waveSpectrumChart.setToolTipText("Puppamelo");
        panel1.add(waveSpectrumChart, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setValBtn) {
            if (mpf != null) {
                SpectrumDialogCommon dialog = new SpectrumDialogCommon(mpf, this);
                dialog.pack();
                dialog.setVisible(true);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
