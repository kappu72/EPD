package it.toscana.rete.lamma.prorotype.gui.menuitems;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.menuitems.RouteMenuItem;
import it.toscana.rete.lamma.prorotype.gui.route.RouteFuelConsumptionPropertiesDialogCommon;
import dk.dma.epd.common.prototype.gui.views.ChartPanelCommon;



/**
 * Opens the route fuel consumption properties dialog
 */
public class RouteFuelConsumptionProperties extends RouteMenuItem {
    
    private static final long serialVersionUID = 1L;

    ChartPanelCommon chartPanel;    

    /**
     * Constructor
     * @param text
     */
    public RouteFuelConsumptionProperties(String text) {
        super();
        setText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doAction() {
    	RouteFuelConsumptionPropertiesDialogCommon routeFCPropertiesDialog = 
                new RouteFuelConsumptionPropertiesDialogCommon(
                        EPD.getInstance().getMainFrame(), 
                        chartPanel,
                        routeIndex);
    	routeFCPropertiesDialog.setVisible(true);
    }

    /**
     * Sets the current chart panel
     * @param chartPanel
     */
    public void setChartPanel(ChartPanelCommon chartPanel) {
        this.chartPanel = chartPanel;
    }
}
