package it.toscana.rete.lamma.prototype.layers;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import dk.dma.epd.common.prototype.event.WMSEvent;
import dk.dma.epd.common.prototype.event.WMSEventListener;
import dk.dma.epd.common.prototype.layers.EPDLayerCommon;


import dk.dma.epd.common.prototype.settings.MapSettings;
import it.toscana.rete.lamma.prototype.gui.WMSTimePanelCommon;
import it.toscana.rete.lamma.prototype.model.LammaMetocWMSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Layer handling all Time WMS data from Lamma wms and displaying of it
 *
 *https://geoportale.lamma.rete.toscana.it/geoserver_mare/ows?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&LAYERS=WW3_MEDIT%3Aww3_medit_Direction_of_wind_waves_surface_20200121T000000000Z,WW3_MEDIT%3Aww3_medit_Sig_height_of_wind_waves_and_swell_surface_20200121T000000000Z&INTERPOLATIONS=,bilinear&TILED=true&STYLES=&SRS=EPSG:4326
 *
 */
public class WMSMetocLayer extends EPDLayerCommon implements WMSEventListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(dk.dma.epd.common.prototype.layers.wms.WMSLayer.class);

    private static final int PROJ_SCALE_THRESHOLD = 6000000;

    volatile boolean shouldRun = true;
    private StreamingTiledWmsTimeService wmsService;
    private int height = -1;
    private int width = -1;
    private float lastScale = -1F;
    MapSettings mapSettings;
    private WMSTimePanelCommon wmsTimePanel;
    private Projection lastProj;
    private LammaMetocWMSConfig wmsConfig;

    /**
     * Constructor that starts the WMS layer
     * @param mapSettings
     */
    public WMSMetocLayer(MapSettings mapSettings) {
        LOG.info("WMS Metoc Layer initialized");
        wmsService = new StreamingTiledWmsTimeService(mapSettings.getLammaWMSservice(), 4);
        wmsService.addWMSEventListener(this);

        this.mapSettings = mapSettings;


    }

    /**
     * Stop the thread
     */
    public void stop() {
        wmsService.stop();
    }
    /**
     * Returns a reference to the WMS service
     *
     * @return a reference to the WMS service
     */
    public AbstractWMSTimeService getWmsService() {
        return wmsService;
    }

    /**
     * Draw the WMS onto the map if the layer is still visible
     *
     * @param tiles
     *            of elements to be drawn
     */
    public void drawWMS(OMGraphicList tiles) {
        graphics.clear();
        if (this.wmsConfig != null && this.wmsConfig.getShow()) {
            graphics.addAll(tiles);
            doPrepare();
        }
    }
    /**
     * Manage the change in scale and size and queues a wms requests
     */
    private void renderWMS() {
        Projection proj = this.getProjection();
        if(proj != null && this.wmsConfig != null && this.wmsConfig.getShow()  && this.wmsConfig.isValid()) {
            /** CLEAN OLD GRAPHICS ON SCALE CHANGE*/
            if (proj.getScale() != lastScale) {
                lastScale = proj.getScale();
                this.drawWMS(new OMGraphicList());
            }
            width = proj.getWidth();
            height = proj.getHeight();
            if (width > 0 && height > 0 ) {
                wmsService.queue(proj); // Request new tiles
            } else{
                this.drawWMS(new OMGraphicList());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void projectionChanged(ProjectionEvent e) {
        super.projectionChanged(e);
        if (e.getProjection() != null) { // If the projection is valid we request the tiles else we clean
            renderWMS();
        }

    }
    /**
     * Called by the WMS service upon a WMS change
     *
     * @param evt
     *            the WMS event
     */
    @Override
    public void changeEventReceived(WMSEvent evt) {
        final Projection proj = this.getProjection();
        OMGraphicList result = wmsService.getWmsList(proj);
        drawWMS(result);
    }

    /**
     * Force redraw if the visibility has been changed instead of waiting for thread
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && this.getProjection() != null) {
            this.drawWMS(new OMGraphicList());
            wmsService.queue(this.getProjection());
        }
    }

    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if(obj instanceof WMSTimePanelCommon) {
            wmsTimePanel = (WMSTimePanelCommon) obj;
            wmsTimePanel.addPropertyChangeListener("wmsConfigChanged", this);
            wmsConfig = wmsTimePanel.getWmsConfig();
            wmsService.setWmsParams(wmsConfig);
            renderWMS();
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);
        if(obj instanceof WMSTimePanelCommon) {
            wmsTimePanel.removePropertyChangeListener("wmsConfigChanged", this);
            wmsTimePanel = null;
            this.stop();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() == wmsTimePanel && evt.getPropertyName() == "wmsConfigChanged") {

                graphics.clear();
                if(wmsService.getWmsServiceURL() != mapSettings.getLammaWMSservice()) {
                    wmsService.clearAllCache();
                    wmsService.setWmsServiceURL(mapSettings.getLammaWMSservice());
                    wmsService.setWMSString(mapSettings.getLammaWMSservice());
                }

                wmsConfig = (LammaMetocWMSConfig) evt.getNewValue();
                wmsService.setWmsParams(wmsConfig);
                // wmsService.cache.clear();

                wmsService.queue(this.getProjection());
        }
    }

}




