package it.toscana.rete.lamma.prototype.metocservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;
import it.toscana.rete.lamma.prototype.event.OpenDapCatalogEvent;
import it.toscana.rete.lamma.prototype.listener.OpenDapCatalogListener;
import thredds.client.catalog.Catalog;
import thredds.client.catalog.Dataset;
import thredds.client.catalog.ServiceType;
import thredds.client.catalog.builder.CatalogBuilder;

import ucar.nc2.dt.grid.GridDataset;

public class LammaMetocService extends MetocService {
    private final String BASE_CATALOG = "172.16.1.55/det.5km/";
    private final String LAMMA_CATALOG = "http://" + BASE_CATALOG + "catalog.xml";
    private CatalogBuilder catBuilder = new CatalogBuilder();
    private List<Dataset> ds;
    private Catalog cat;
    private boolean requestActive = false;

    @Override
    public GridDataset openMetoc(String path) {
        try {
            metocDataset = GridDataset.open(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return metocDataset;
    }

    /**
     * This manage the files to be opened
     * 
     * @param metocSettings
     * @return
     */
    @Override
    public GridDataset openMetoc(RouteMetocSettings metocSettings) {

        if (metocDataset == null || !metocDataset.getLocation().equals(metocSettings.getLocalMetocFile())) {
            clearDatasets();
            Dataset d = ds.stream().filter(data -> data.getName().equals(metocSettings.getLammaMetocFile())).findAny()
                    .orElse(null);
            URI u = d.getAccess(ServiceType.OPENDAP).getStandardUri();
            try {
                return this.openMetoc(u.toURL().toString());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return metocDataset;
    }

    /**
     * Async method to get dataset from lamma opendap service
     * 
     * @param listener
     */
    public void getDatasetList(OpenDapCatalogListener listener) {
       
        new Thread(new Runnable() {
            public void run() {
                
                cat = cat == null ? catBuilder.buildFromLocation(LAMMA_CATALOG, null) : cat;
                synchronized (this) { 
                    if ( cat!= null ) {
                        ds = (List<Dataset>) cat.getAllDatasets();
                    }
                }
                
                
                listener.onCatalogChanged(new OpenDapCatalogEvent(this, ds));
        } 
    }).start(); 
    
    }
}