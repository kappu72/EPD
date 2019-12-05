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


public class LocalMetocServiceTest {

    private Route _loadRoute(String filename) throws URISyntaxException, RouteLoadException {
        URL url = ClassLoader.getSystemResource(filename);
        Assert.assertNotNull(url);
        File file = new File(url.toURI());
        Assert.assertNotNull(file);
        NavSettings navSettings = new NavSettings();
        Route route = RouteLoader.pertinaciousLoad(file, navSettings);
        Assert.assertNotNull(route);
        return route;
    }

    private RouteMetocSettings getMetocSettings() throws URISyntaxException {
        RouteMetocSettings routeMetocSettings = new RouteMetocSettings();
        routeMetocSettings.setProvider(MetocProviders.LOCAL.label());
        // URL url =
        // ClassLoader.getSystemResource("WW3_Mediterraneo_2019080200-part.grb");
        routeMetocSettings.setLocalMetocFile(Paths.get("/Users/kappu/lamma/data/2019103100.test.5km.nc").toString());
        return routeMetocSettings;
    }

    /**
     * Test method {@link LocalMetocService#routeMetoc}
     * 
     * @throws RouteLoadException
     * @throws URISyntaxException
     * @throws ShoreServiceException
     * @throws IOException
     */
    @Test
    public final void TestMetocExtraction()
            throws URISyntaxException, RouteLoadException, ShoreServiceException, IOException {
        System.out.println("Loading Med-Route file");
        // int pnt
        Route route = _loadRoute("Med-route.txt");
        RouteMetocSettings ms = getMetocSettings();
        ms.setInterval(60);
        route.setRouteMetocSettings(ms);
        LocalMetocService metocService = new LocalMetocService();
        metocService.openMetoc(ms.getLocalMetocFile());
        
        GridDataset gds = metocService.getMetocDataset();
        gds.getDataVariables().stream().forEach(var -> System.out.println(var.getFullName()));
        
        // C'è il problema di gestire la data, ma non ho bisogno di trasformare il richiesta metoc.
        // posso semplicemente filtrare quelli che non voglio se non li voglio e stop oppure
        Date gribRun = metocService.getMetocDataset().getCalendarDateStart().toDate();

        route.setStarttime(new Date(gribRun.getTime() + (3600 * 1000)));
        route.setSpeed(20);
        route.calcValues(true);
       // metocService.routeMetoc(route, gribRun);
    }

    /**
     * Test method {@link LocalMetocService#routeMetoc}
     * 
     * @throws RouteLoadException
     * @throws URISyntaxException
     * @throws ShoreServiceException
     * @throws IOException
     */
    @Test
    public final void TestWWWPartREading()
            throws URISyntaxException, RouteLoadException, ShoreServiceException, IOException {
        System.out.println("Loading Med-Route file");
        // int pnt
        Route route = _loadRoute("Med-route.txt");
        RouteMetocSettings ms = getMetocSettings();
        ms.setInterval(60);
        route.setRouteMetocSettings(ms);
        ms.setLocalMetocFile(Paths.get("/Users/kappu/lamma/data/WW3_Med005MPI_2019111206-part-4h.grb").toString());
        LocalMetocService metocService = new LocalMetocService();
        
        metocService.openMetoc(ms.getLocalMetocFile());
        
        GridDataset gds = metocService.getMetocDataset();
        gds.getDataVariables().stream().forEach(var -> {
            System.out.println(var.getShortName());
            System.out.println(var.getFullName());
        });
        
        // C'è il problema di gestire la data, ma non ho bisogno di trasformare il richiesta metoc.
        // posso semplicemente filtrare quelli che non voglio se non li voglio e stop oppure
        // Date gribRun = metocService.getMetocDataset().getCalendarDateStart().toDate();
        // GeoGrid swell_waves = metocService.getMetocDataset().findGridByShortName("Significant_height_of_swell_waves_OSEQD");
        // GridCoordSystem gcs = swell_waves.getCoordinateSystem(); 
        // Array  data =  swell_waves.readDataSlice(1, -1,-1,-1);
        // for(int i = 0; i < 320; i++ ) {
        //     for(int n = 0; n < 850; n++ ) {
        //         System.out.println(" " + i + " " + n);
        //         Array  data =  swell_waves.readDataSlice(1, 0, i, n);
        //         System.out.println(" " + i + " " + n + " " + data.toString());
        //         if(Double.isFinite(data.getDouble(0))) { 
        //             System.out.println(data.getDouble(i));
        //         }
        //     }
        // }
        
    }

}