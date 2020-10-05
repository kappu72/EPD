package it.toscana.rete.lamma.ship.gui;

import dk.dma.epd.ship.gui.component_panels.DockableComponentPanel;
import it.toscana.rete.lamma.prototype.gui.WMSTimePanelCommon;

public class WMSTimePanel extends WMSTimePanelCommon implements DockableComponentPanel {

    @Override
    public String getDockableComponentName() {
        
        return "WMS Time";
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