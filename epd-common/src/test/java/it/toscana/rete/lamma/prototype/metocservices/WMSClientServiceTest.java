package it.toscana.rete.lamma.prototype.metocservices;

import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import dk.dma.epd.common.prototype.settings.MapSettings;
import org.geotools.ows.wms.WebMapServer;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class WMSClientServiceTest {
    private CountDownLatch lock = new CountDownLatch(1);
    
    /**
     * Test method {@link WMSClientService#WMSClientService(MapSettings)}
     * 
     * @throws RouteLoadException
     * @throws URISyntaxException
     * @throws ShoreServiceException
     * @throws IOException
     */
    @Test
    public final void TestCreateService() throws IOException, InterruptedException {
       
        MapSettings s = new MapSettings();
        s.setLammaWMSservice("https://geoportale.lamma.rete.toscana.it/geoserver_mare/ows");
        WMSClientService service = new WMSClientService(s);
        service.addPropertyChangeListener("wms", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getNewValue() instanceof WebMapServer) {
                    service.getTimeDimension();
                    lock.countDown();

                }
            }

        });
        lock.await(20000, TimeUnit.MILLISECONDS);
        
    }

}