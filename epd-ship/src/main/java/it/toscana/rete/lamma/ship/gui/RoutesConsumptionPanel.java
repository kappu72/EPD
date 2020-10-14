package it.toscana.rete.lamma.ship.gui;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.ship.EPDShip;
import dk.dma.epd.ship.gui.component_panels.DockableComponentPanel;
import it.toscana.rete.lamma.prototype.gui.RoutesConsumptionPanelCommon;
import it.toscana.rete.lamma.prototype.gui.route.RouteFuelConsumptionPropertiesDialogCommon;


public class RoutesConsumptionPanel extends RoutesConsumptionPanelCommon implements DockableComponentPanel {
    @Override
    public String getDockableComponentName() {
        return "Consumptions";
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
    public void openFuelConsumtion() {
        System.out.println("Eccomi");
        int i = this.rcTable.getSelectedRow();
        String routeName = (String) rcTable.getModel().getValueAt(i,0);
        int routeIdx = getRouteIdexByName(routeName);
        if (routeIdx >= 0) {
            RouteFuelConsumptionPropertiesDialogCommon routeFCDialog = new RouteFuelConsumptionPropertiesDialogCommon(
                    EPDShip.getInstance().getMainFrame(),
                    EPDShip.getInstance().getMainFrame().getChartPanel(),
                    routeIdx);
            routeFCDialog.setVisible(true);
        }
    }
}
