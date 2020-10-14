package it.toscana.rete.lamma.ship.gui;

import dk.dma.epd.ship.gui.component_panels.DockableComponentPanel;
import it.toscana.rete.lamma.prototype.gui.RoutesConsumptionPanelCommon;



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

}
