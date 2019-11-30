package it.toscana.rete.lamma.prototype.metocservices;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import dk.dma.epd.common.prototype.model.route.RouteLoader;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;

import dk.dma.epd.common.prototype.settings.NavSettings;
import ucar.nc2.dt.grid.GridDataset;

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
        metocService.routeMetoc(route, gribRun);
    }

}