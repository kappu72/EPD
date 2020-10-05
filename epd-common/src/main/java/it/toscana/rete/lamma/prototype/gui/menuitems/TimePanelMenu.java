package it.toscana.rete.lamma.prototype.gui.menuitems;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.menuitems.event.IMapMenuAction;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import it.toscana.rete.lamma.prototype.gui.MetocPanelCommon;
import it.toscana.rete.lamma.prototype.gui.WMSTimePanelCommon;

import javax.swing.*;


public class TimePanelMenu extends JMenuItem implements IMapMenuAction {

    private static final long serialVersionUID = 1L;

    private int routeWaypointIndex;
    private int routeIndex;
    private RouteManagerCommon routeManager;
    private WMSTimePanelCommon wmsTimePanel;
    private MetocPanelCommon metocPanelCommon;

    public TimePanelMenu(String text) {
        super();
        setText(text);
    }

    @Override
    public void doAction() {
        // Recupera la rotta, il punto ed il tempo dal punto infine settalo nel selettore ore
        if(wmsTimePanel != null &&  wmsTimePanel.isVisible())
            wmsTimePanel.selectTime(routeManager.getRoute(routeIndex).getWpEta(routeWaypointIndex));
        if(metocPanelCommon != null && metocPanelCommon.isVisible())
            metocPanelCommon.selectTime(routeManager.getRoute(routeIndex).getWpEta(routeWaypointIndex));
    }

    public void setRouteWaypointIndex(int routeWaypointIndex) {
        this.routeWaypointIndex = routeWaypointIndex;
    }

    public void setRouteManager(RouteManagerCommon routeManager) {
        this.routeManager = routeManager;
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public void setWmsTimePanel(WMSTimePanelCommon wmsTimePanel) {
        this.wmsTimePanel = wmsTimePanel;
    }

    public void setMetocPanelCommon(MetocPanelCommon metocPanelCommon) {
        this.metocPanelCommon = metocPanelCommon;
    }
}