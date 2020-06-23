package it.toscana.rete.lamma.prototype.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import com.bbn.openmap.gui.OMComponentPanel;

import javax.swing.*;
import javax.swing.text.DateFormatter;

import dk.dma.epd.common.prototype.layers.ais.AisLayerCommon;
import it.toscana.rete.lamma.prototype.metocservices.WMSClientService;
import it.toscana.rete.lamma.prototype.metocservices.WMSMetocLayers;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.xml.Dimension;
import org.geotools.util.DateTimeParser;
import org.jdesktop.swingx.JXDatePicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Panel to manage the time of Lamma  idea.properties  idea.properties wms metoc service
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
    private WMSClientService wmsService;
    private DateTimeParser dateTimeParser = new DateTimeParser();
    private LammaMetocWMSConfig wmsConfig;
    private Date time;
    private String run;
    private List<String> layers;
    private Dimension timeDimension;



    public WMSTimePanelCommon() {
        add(this.wmsTime);
        waveDir.addItemListener(this);
        wavePeriod.addItemListener(this);
        waveHeight.addItemListener(this);
        hourSelector.addItemListener(this);
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
    }
    // Update time selector time series and select first element if present
    private void updateTimes(Dimension time) {
        timeDimension = time;
        if(hourSelector!=null) {
            if(time != null){
                try {
                    Collection<Date> parsed = (Collection<Date>) dateTimeParser.parse(time.getExtent().getValue());
                    hourSelector.addTimes(new ArrayList<>(parsed));
                    if(parsed.size() > 0) {
                       hourSelector.setSelectedIndex(0);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else {
                hourSelector.addTimes(new ArrayList<>());
                hourSelector.setSelectedIndex(-1);
            }
        }

    }
    // Return current wmst time selected
    private Date getTimeParam() {
        return (Date) hourSelector.getModel().getSelectedItem();
    }
    // Return current run time
    private String getRun() {
        // 2020-06-22T00:00:00.000Z/2020-06-27T00:00:00.000Z/PT1H
        // ww3_medit_Direction_of_wind_waves_surface_20200621T000000000Z
        String run = "";
        if(timeDimension != null) {
            String[] range = timeDimension.getExtent().getValue().split("/");
            if(range.length > 0 && range[0].matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z")){
                run = range[0].replaceAll("[.:-]", "");
            }

        }
        return run;
    }
    // Return current wms layers selected
    private List<String> getLayersPram () {
        List<String> newLayers = new ArrayList<>();
        if(waveHeight.isSelected()) {
            newLayers.add(WMSMetocLayers.MEAN_WAVE_HEIGHT.name());
        }else if(wavePeriod.isSelected()) {
            newLayers.add(WMSMetocLayers.MEAN_WAVE_PERIOD.name());
        }
        if(waveDir.isSelected()) {
            newLayers.add(WMSMetocLayers.MEAN_WAVE_DIR.name());
        }
        return newLayers;
    }

    private void updateWmsConfig() {
        LammaMetocWMSConfig oldConfig = wmsConfig;
        wmsConfig = new LammaMetocWMSConfig(getTimeParam(), getRun(), getLayersPram());
        if (!wmsConfig.equals(oldConfig)) {
            firePropertyChange("wmsConfigChanged", oldConfig, wmsConfig);
        }else {
            wmsConfig = oldConfig;
        }
    }

    public LammaMetocWMSConfig getWmsConfig() {
        return wmsConfig;
    }

    @Override
    public void findAndInit(Object obj) {
        if(obj instanceof WMSClientService) {
            wmsService = (WMSClientService) obj;
            if(wmsService.isInitialized()) {
                updateTimes(wmsService.getTimeDimension());
            }
            wmsService.addPropertyChangeListener("wms", this);
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        if(obj instanceof WMSClientService) {
            wmsService.removePropertyChangeListener("wms", this);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object o = e.getSource();
        if(o == waveHeight || o == wavePeriod || o == waveDir || o == hourSelector) {
            updateWmsConfig();
        }

    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        switch (property) {
            case "wms":
                if( wmsService!= null && wmsService.isInitialized() ) {
                    updateTimes(wmsService.getTimeDimension());
                }
                break;

            default: break;
        }
    }


}
