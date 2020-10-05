package it.toscana.rete.lamma.prototype.metocservices;

import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;

import ucar.nc2.dt.grid.GridDataset;


public class LocalMetocService extends MetocService {

/**
 * This manage the files to be opened
 * @param metocSettings
 * @return
 */
public GridDataset openMetoc(RouteMetocSettings metocSettings) {

    if (metocDataset == null || !metocDataset.getLocation().equals(metocSettings.getLocalMetocFile())) {
        clearDatasets();
        return this.openMetoc(metocSettings.getLocalMetocFile());

    }
    return metocDataset;
}



}