package it.toscana.rete.lamma.ship.prototype;

import javax.swing.JButton;

import com.bbn.openmap.MapHandlerChild;

import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.dma.epd.ship.gui.route.RouteManagerPanel;


public class FuelService extends MapHandlerChild {

	protected RouteManagerPanel routeManagerPanel;
	private JButton fuelConsumptionBtn = new JButton("Fuel Consumption");
	/**
     * {@inheritDoc}
     */
    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (routeManagerPanel == null && obj instanceof RouteManagerPanel) {
        	routeManagerPanel = (RouteManagerPanel) obj;
 
        	initRouteManagerPanel(routeManagerPanel);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findAndUndo(Object obj) {
        if (routeManagerPanel == obj) {
        	routeManagerPanel = null;
        }
        super.findAndUndo(obj);
    }
    /**
     * Init RouteManagerPannel adding the fuel-consumption
     * @param panel
     */
    private void initRouteManagerPanel (RouteManagerPanel panel) {
  
    	
    	
    }
	
}
