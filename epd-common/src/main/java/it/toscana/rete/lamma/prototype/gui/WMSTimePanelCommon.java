package it.toscana.rete.lamma.prototype.gui;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.bbn.openmap.gui.OMComponentPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import dk.dma.epd.common.prototype.layers.ais.AisLayerCommon;
import it.toscana.rete.lamma.prototype.metocservices.WMSClientService;
import it.toscana.rete.lamma.prototype.metocservices.WMSMetocLayers;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;
import org.geotools.ows.wms.xml.Dimension;
import org.geotools.util.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Panel to manage the time of Lamma wms metoc service
 * This will also used as time to extract data from OpenDap or local metoc service to
 * draw wave-rose chart.
 */
public class WMSTimePanelCommon extends OMComponentPanel implements PropertyChangeListener, ItemListener {
    private static final Logger LOG = LoggerFactory
            .getLogger(AisLayerCommon.class);
    private JPanel wmsTime;
    private JLabel panelLabel;

    private JRadioButton waveHeight;
    private JRadioButton wavePeriod;
    private JCheckBox waveDir;
    private WMSLayerTimeSelector hourSelector;
    private JCheckBox windSpeedDir;
    private JRadioButton windGust;
    private JCheckBox showLegend;
    private JCheckBox showLayer;
    private JRadioButton windSpeed;
    private JRadioButton currentSpeed;
    private JCheckBox windDir;
    private JCheckBox currentDir;
    private JLabel selectedLocalTime;
    private WMSRUNSelector wmsRUNSelector;
    private WMSClientService wmsService;
    private DateTimeParser dateTimeParser = new DateTimeParser();
    private LammaMetocWMSConfig wmsConfig;
    private Date time;
    private String run;
    private List<String> layers;
    private Dimension timeDimension;
    private static String pattern = "dd/MM/yyyy HH:mm";
    private static DateFormat df = new SimpleDateFormat(pattern);


    public WMSTimePanelCommon() {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(this.wmsTime, BorderLayout.CENTER);
        waveDir.addItemListener(this);
        windSpeedDir.addItemListener(this);
        wavePeriod.addItemListener(this);
        waveHeight.addItemListener(this);
        windGust.addItemListener(this);
        hourSelector.addItemListener(this);
        wmsRUNSelector.addItemListener(this);
        showLayer.addItemListener(this);
        showLegend.addItemListener(this);
        windSpeed.addItemListener(this);
        windDir.addItemListener(this);
        currentDir.addItemListener(this);
        currentSpeed.addItemListener(this);

        initValues();
    }

    // inizializza i valori dei componenti
    private void initValues() {
        waveDir.setSelected(true);
        waveHeight.setSelected(true);
    }

    // Create custom ui components
    private void createUIComponents() {

        hourSelector = new WMSLayerTimeSelector(new ArrayList<>());
        wmsRUNSelector = new WMSRUNSelector(new ArrayList<>());
    }

    // Update time selector time series and select first element if present
    private void updateTimes(Dimension time) {
        timeDimension = time;
        if (hourSelector != null) {
            if (time != null) {
                try {
                    Collection<Date> parsed = (Collection<Date>) dateTimeParser.parse(time.getExtent().getValue());

                    hourSelector.addTimes(new ArrayList<>(parsed));
                    if (parsed.size() > 0) {
                        hourSelector.setSelectedIndex(0);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                hourSelector.addTimes(new ArrayList<>());
                hourSelector.setSelectedIndex(-1);
            }
        }

    }

    // Return current wmst time selected
    private Date getTimeParam() {
        return (Date) hourSelector.getModel().getSelectedItem();
    }


    // Return current run time non più usata ci aspettiamo che la run sia un workspace
    private String getRun() {
        // 2020-06-22T00:00:00.000Z/2020-06-27T00:00:00.000Z/PT1H
        // ww3_medit_Direction_of_wind_waves_surface_20200621T000000000Z
        String run = "";
        if (timeDimension != null) {
            String[] range = timeDimension.getExtent().getValue().split("/");
            if (range.length > 0 && range[0].matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")) {
                run = range[0].replaceAll("[.:-]", "");
            }

        }
        return run;
    }

    // Return current wms layers selected
    private List<String> getLayersPram() {
        List<String> newLayers = new ArrayList<>();
        String ws = wmsRUNSelector.isEnabled() && wmsRUNSelector.getSelectedIndex() != -1 ? ((String) wmsRUNSelector.getSelectedItem()).concat(":") : "";
        if (waveHeight.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.MEAN_WAVE_HEIGHT.layerName()));
        } else if (wavePeriod.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.MEAN_WAVE_PERIOD.layerName()));
        } else if (windSpeed.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.WIND_SPEED.layerName()));
        } else if (currentSpeed.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.CURRENT_SPEED.layerName()));
        } else if (windGust.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.WIND_GUST.layerName()));
        }

        if (waveDir.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.MEAN_WAVE_DIR.layerName()));
        }
        if (windSpeedDir.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.WIND_SPEED_DIR.layerName()));
        }
        if (windDir.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.WIND_DIR.layerName()));
        }
        if (currentDir.isSelected()) {
            newLayers.add(ws.concat(WMSMetocLayers.CURRENT_DIR.layerName()));
        }

        return newLayers;
    }

    private void updateWmsConfig() {
        LammaMetocWMSConfig oldConfig = wmsConfig;
        wmsConfig = new LammaMetocWMSConfig(getTimeParam(), "unused", getLayersPram(), showLayer.isSelected(), showLegend.isSelected());
        if (!wmsConfig.equals(oldConfig)) {
            firePropertyChange("wmsConfigChanged", oldConfig, wmsConfig);
        } else {
            wmsConfig = oldConfig;
        }
    }

    public LammaMetocWMSConfig getWmsConfig() {
        return wmsConfig;
    }

    @Override
    public void findAndInit(Object obj) {
        if (obj instanceof WMSClientService) {
            wmsService = (WMSClientService) obj;
            if (wmsService.isInitialized()) {
                List<String> runs = wmsService.getWorkspaces();
                if (runs.size() > 0) {
                    wmsRUNSelector.setEnabled(true);
                    wmsRUNSelector.addRUNS(runs);
                } else {
                    wmsRUNSelector.setEnabled(false);
                    updateTimes(wmsService.getTimeDimension());
                }
            }
            wmsService.addPropertyChangeListener("wmsServer", this);
            wmsService.addPropertyChangeListener("wmsUrl", this);
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        if (obj instanceof WMSClientService) {
            wmsService.removePropertyChangeListener("wmsServer", this);
            wmsService.removePropertyChangeListener("wmsUrl", this);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object o = e.getSource();
        if (o == showLegend || o == showLayer || o == windSpeedDir || o == windGust
                || o == waveHeight || o == wavePeriod || o == waveDir || o == hourSelector
                || o == windDir || o == windSpeed || o == currentDir || o == currentSpeed) {
            updateWmsConfig();
        }
        if (o == hourSelector) {
            if (hourSelector.getSelectedIndex() != -1)
                selectedLocalTime.setText(df.format(hourSelector.getSelectedItem()));
            else selectedLocalTime.setText("");
        }
        if (o == wmsRUNSelector) {
            if (wmsRUNSelector.getSelectedIndex() != -1)
                updateTimes(wmsService.getTimeDimension((String) wmsRUNSelector.getSelectedItem()));
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        switch (property) {
            case "wmsServer":
                if (wmsService != null && wmsService.isInitialized()) {
                    List<String> runs = wmsService.getWorkspaces();
                    if (runs.size() > 0) {
                        wmsRUNSelector.setEnabled(true);
                        wmsRUNSelector.addRUNS(runs);
                    } else {
                        wmsRUNSelector.setEnabled(false);
                        updateTimes(wmsService.getTimeDimension());
                    }
                }
            case "wmsUrl": {
                showLayer.setSelected(false);
            }
            break;

            default:
                break;
        }
    }

    public void selectTime(Date wmsTime) {
        hourSelector.selectByDate(wmsTime);
    }

    public Date getSelectedTime() {
        return (Date) hourSelector.getSelectedItem();
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
        wmsTime = new JPanel();
        wmsTime.setLayout(new GridLayoutManager(3, 1, new Insets(0, 5, 0, 5), -1, -1));
        wmsTime.setAutoscrolls(false);
        wmsTime.setMaximumSize(new java.awt.Dimension(4000, 350));
        wmsTime.setMinimumSize(new java.awt.Dimension(230, 350));
        wmsTime.setOpaque(true);
        wmsTime.setPreferredSize(new java.awt.Dimension(230, 350));
        wmsTime.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        wmsTime.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new java.awt.Dimension(-1, 200), 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setMaximumSize(new java.awt.Dimension(100, 30));
        label1.setMinimumSize(new java.awt.Dimension(70, 30));
        label1.setPreferredSize(new java.awt.Dimension(70, 30));
        label1.setText("UTC Time");
        label1.setVerticalAlignment(0);
        label1.setVerticalTextPosition(0);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        hourSelector.setMaximumRowCount(20);
        hourSelector.setMaximumSize(new java.awt.Dimension(400, 30));
        hourSelector.setMinimumSize(new java.awt.Dimension(130, 20));
        hourSelector.setOpaque(true);
        hourSelector.setPreferredSize(new java.awt.Dimension(145, 20));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(hourSelector, gbc);
        showLegend = new JCheckBox();
        showLegend.setPreferredSize(new java.awt.Dimension(72, 30));
        showLegend.setText("Legend");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(showLegend, gbc);
        showLayer = new JCheckBox();
        showLayer.setSelected(false);
        showLayer.setText("Show");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(showLayer, gbc);
        final JLabel label2 = new JLabel();
        label2.setPreferredSize(new java.awt.Dimension(23, 30));
        label2.setText("L.T.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        selectedLocalTime = new JLabel();
        selectedLocalTime.setAlignmentX(0.5f);
        selectedLocalTime.setAutoscrolls(false);
        selectedLocalTime.setMaximumSize(new java.awt.Dimension(250, 16));
        selectedLocalTime.setPreferredSize(new java.awt.Dimension(145, 30));
        selectedLocalTime.setText("Not selected");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 4, 0, 0);
        panel1.add(selectedLocalTime, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("WORKSP.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel1.add(wmsRUNSelector, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        wmsTime.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new java.awt.Dimension(-1, 200), null, new java.awt.Dimension(-1, 300), 0, false));
        wavePeriod = new JRadioButton();
        wavePeriod.setActionCommand("meanWavePeriod");
        wavePeriod.setText("Mean Wave Period");
        wavePeriod.putClientProperty("hideActionText", Boolean.FALSE);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(wavePeriod, gbc);
        waveDir = new JCheckBox();
        waveDir.setActionCommand("meanWaveDirectionChanged");
        waveDir.setSelected(false);
        waveDir.setText("Mean Wave Direction");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(waveDir, gbc);
        waveHeight = new JRadioButton();
        waveHeight.setActionCommand("meanWaveHeight");
        waveHeight.setSelected(false);
        waveHeight.setText("Significant Wave Height");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(waveHeight, gbc);
        windGust = new JRadioButton();
        windGust.setActionCommand("windGust");
        windGust.setLabel("Wind Gust");
        windGust.setText("Wind Gust");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(windGust, gbc);
        windSpeed = new JRadioButton();
        windSpeed.setActionCommand("windSpeed");
        windSpeed.setText("10m Wind Speed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(windSpeed, gbc);
        currentSpeed = new JRadioButton();
        currentSpeed.setActionCommand("currenteSpeed");
        currentSpeed.setText("Surface Currente Speed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(currentSpeed, gbc);
        windDir = new JCheckBox();
        windDir.setActionCommand("windDir");
        windDir.setText("10m Wind Direction");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(windDir, gbc);
        currentDir = new JCheckBox();
        currentDir.setActionCommand("currentDirection");
        currentDir.setText("Surface Current Direction");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(currentDir, gbc);
        windSpeedDir = new JCheckBox();
        windSpeedDir.setActionCommand("windSpeedDir");
        windSpeedDir.setText("10m Wind Barbs");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(windSpeedDir, gbc);
        panelLabel = new JLabel();
        Font panelLabelFont = this.$$$getFont$$$(null, -1, -1, panelLabel.getFont());
        if (panelLabelFont != null) panelLabel.setFont(panelLabelFont);
        panelLabel.setHorizontalAlignment(0);
        panelLabel.setHorizontalTextPosition(0);
        panelLabel.setText("Lamma WMS Metoc");
        wmsTime.add(panelLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(wavePeriod);
        buttonGroup.add(waveHeight);
        buttonGroup.add(windGust);
        buttonGroup.add(windSpeed);
        buttonGroup.add(currentSpeed);
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
        return wmsTime;
    }

}