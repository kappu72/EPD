package it.toscana.rete.lamma.ship.gui;

import dk.dma.epd.common.prototype.layers.EPDLayerCommon;
import dk.dma.epd.ship.gui.component_panels.DockableComponentPanel;
import it.toscana.rete.lamma.prototype.gui.MetocPanelCommon;
import it.toscana.rete.lamma.ship.layers.MouseEventLayer;

public class MetocPointPanel extends MetocPanelCommon implements DockableComponentPanel {
    @Override
    public String getDockableComponentName() {
        return "Metoc Panel";
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
    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if(obj instanceof MouseEventLayer) {
            layer = (EPDLayerCommon) obj;
        }
    }
}
