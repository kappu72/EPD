package it.toscana.rete.lamma.prototype.metocservices;

import com.bbn.openmap.MapHandlerChild;
import dk.dma.epd.common.prototype.settings.MapSettings;
import org.geotools.ows.ServiceException;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.xml.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

public class WMSClientService extends MapHandlerChild {

    private WebMapServer lamma;
    private MapSettings settings;
    private String BASE = "ww3_medit_Direction_of_wind_waves_surface_";
    private WMSCapabilities capabilities;
    private Layer[] layers;
    private WMSMetocLayers METOC_LAYERS;

    public boolean isInitialized() {
        return initialized;
    }

    private boolean initialized = false;

    private static final Logger LOG = LoggerFactory.getLogger(WMSClientService.class);
    public WMSClientService(MapSettings settings) {
        this.settings = settings;
        createService();
    }
    private  void  createService() {
        URL url = null;
        String wmsURL = this.settings.getLammaWMSservice();
        try {
            url = new URL(wmsURL); // + "?SERVICE=WMS&VERSION=1.1.1&REQUEST=GETCapabilities"
        } catch (MalformedURLException e) {
            LOG.error("Malformed Url " + wmsURL);
        }
        if(!wmsURL.isEmpty()) {
            URL finalUrl = url;
            new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    WebMapServer wms = null;

                    try {
                        wms = new WebMapServer(finalUrl);
                    } catch (IOException e) {
                        initialized = false;
                        LOG.error(e.getMessage());
                    } catch (ServiceException e) {
                        initialized = false;
                        LOG.error(e.getMessage());
                    } catch (SAXException e) {
                        initialized = false;
                        LOG.error(e.getMessage());
                    }
                    lamma = wms;
                    capabilities= wms.getCapabilities();
                    getLayers();
                    initialized = true;
                    firePropertyChange("wms", null, lamma);
                }

            }
        }).start();
        }

    }
    private WebMapServer getService() {
        return this.lamma;
    }
    public WMSCapabilities getCapabilities () {
        if(this.capabilities == null && this.lamma != null) {
            capabilities = lamma.getCapabilities();
        }
        return capabilities;
    }

    public Layer[] getLayers () {
        if(this.layers == null) {
            this.layers = WMSUtils.getNamedLayers(this.getCapabilities());
        }
        return this.layers;

    }
    // Vanno estratti i tempi per uno singolo layer, tanto dovrebbero essere tutti uguali
    // va pensato un componente che prende questa stringa inizio fine e genera il resto
    public  Dimension getTimeDimension() {
            List<Dimension> dim = Arrays.stream(this.getLayers())
                    .filter(layer -> layer.getName().toLowerCase().contains(WMSMetocLayers.MEAN_WAVE_DIR.layerName().toLowerCase()))
                    .sorted(new Comparator<Layer>() {
                        @Override
                        public int compare(Layer o1, Layer o2) {
                            return o1.getName().compareTo(o2.getName()) * -1;
                        }
                    })
                    .map(layer -> layer.getDimension("time"))
                    .collect(Collectors.toList());
            if(dim.size() > 0) {
                return dim.get(0);
            }
            return null;
    }

    public String getLayerRun() {
        Optional<Map<String, Dimension>> dim = Arrays.stream(this.getLayers())
                .filter(layer -> layer.getName().toLowerCase().contains(WMSMetocLayers.MEAN_WAVE_DIR.layerName().toLowerCase()))
                .sorted(new Comparator<Layer>() {
                    @Override
                    public int compare(Layer o1, Layer o2) {
                        return o1.getName().compareTo(o2.getName()) * -1;
                    }
                })
                .map(layer -> layer.getDimensions())
                .findFirst();
        return "";
    };
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener in_pcl) {
        super.addPropertyChangeListener(propertyName, in_pcl);
    }

    // TODO: implementare il reset quanto cambia la url inoltre


}




