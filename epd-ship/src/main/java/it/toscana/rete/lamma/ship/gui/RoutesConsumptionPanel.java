package it.toscana.rete.lamma.ship.gui;

import com.bbn.openmap.gui.WindowSupport;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.route.RouteMetocDialog;
import dk.dma.epd.common.prototype.gui.route.RoutePropertiesDialogCommon;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.ship.EPDShip;
import dk.dma.epd.ship.gui.component_panels.DockableComponentPanel;
import it.toscana.rete.lamma.prototype.gui.RoutesConsumptionPanelCommon;
import it.toscana.rete.lamma.prototype.gui.route.RouteFuelConsumptionPropertiesDialogCommon;


public class RoutesConsumptionPanel extends RoutesConsumptionPanelCommon implements DockableComponentPanel {
    @Override
    public String getDockableComponentName() {
        return "Routes Sorting";
    }

    @Override
    public boolean includeInDefaultLayout() {
        return true;
    }

    @Override
    public boolean includeInPanelsMenu() {
        return true;
    }

    @Override
    public void openFuelConsumption(int routeIndex) {
        if (routeIndex >= 0) {
            RouteFuelConsumptionPropertiesDialogCommon routeFCDialog = new RouteFuelConsumptionPropertiesDialogCommon(
                    EPDShip.getInstance().getMainFrame(),
                    EPDShip.getInstance().getMainFrame().getChartPanel(),
                    routeIndex);
            routeFCDialog.setVisible(true);
        }
    }

    @Override
    public void openRouteProperties(int routeIndex) {
        RoutePropertiesDialogCommon routePropertiesDialog =
                new RoutePropertiesDialogCommon(
                        EPD.getInstance().getMainFrame(),
                        EPDShip.getInstance().getMainFrame().getChartPanel(),
                        routeIndex);
        routePropertiesDialog.setVisible(true);


    }

    @Override
    public void openRouteMetocProperties(int routeIndex) {
        RouteMetocDialog routeMetocDialog = new RouteMetocDialog(
                EPD.getInstance().getMainFrame(),
                EPD.getInstance().getRouteManager(),
                routeIndex);
        routeMetocDialog.setVisible(true);
        EPD.getInstance().getRouteManager().notifyListeners(RoutesUpdateEvent.METOC_SETTINGS_CHANGED);
    }

    @Override
    public void setWindowSupport(WindowSupport ws) {
        super.setWindowSupport(ws);
    }

}
