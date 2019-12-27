package it.toscana.rete.lamma.prototype.metocservices;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import dk.dma.epd.common.prototype.model.route.RouteLoader;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;

import dk.dma.epd.common.prototype.settings.NavSettings;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.ma2.Array;


public class LammaMetocServiceTest {

    
    /**
     * Test method {@link LammaMetocService#openMetoc(String)}
     * 
     * @throws RouteLoadException
     * @throws URISyntaxException
     * @throws ShoreServiceException
     * @throws IOException
     */
    @Test
    public final void TestOpenMetoc() throws IOException {
       
		
		
		
        // int pnt
        for(int i = 0; i < 100; i++){
            double rest = 15.0%1.;
		
		
		
            int up = (int) Math.ceil(15./1.);
            int down = (int) Math.floor(15./1.);
            System.out.println(i + " " + rest + " " + up + " " + down);
        }
        // LammaMetocService metocService = new LammaMetocService();
        // metocService.getDatasetList(null);
        // metocService.openMetoc("http://172.16.1.55/det.5km/2019112606.det.5km.nc");
        
        // metocService.metocDataset.close();;
        
    }

}