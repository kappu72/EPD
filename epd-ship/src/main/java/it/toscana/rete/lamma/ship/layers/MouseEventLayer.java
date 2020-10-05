package it.toscana.rete.lamma.ship.layers;


import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.layers.common.WpCircle;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;
import dk.dma.epd.common.prototype.settings.EnavSettings;
import dk.dma.epd.ship.layers.GeneralLayer;
import dk.dma.epd.ship.route.RouteManager;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import it.toscana.rete.lamma.prototype.gui.PointMetocGraphic;
import it.toscana.rete.lamma.ship.gui.MetocPointPanel;
import it.toscana.rete.lamma.ship.gui.WMSTimePanel;
import it.toscana.rete.lamma.prototype.gui.WMSTimePanelCommon;
import it.toscana.rete.lamma.prototype.metocservices.LocalMetocService;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class MouseEventLayer extends GeneralLayer {

    private EnavSettings enavSettings;
    private MetocPointPanel metocPointPanel;

    public MouseEventLayer() {
        super();
        enavSettings = EPD.getInstance().getSettings().getEnavSettings();
    }

    @Override
    public boolean mouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && metocPointPanel != null && metocPointPanel.isActive()) {
            graphics.clear();
            MetocPointForecast mp = null;
            Point2D point = mapBean.getProjection().inverse(evt.getPoint());
            graphics.add(new WpCircle(point.getY(), point.getX(), 0,0,10,10));
            prepare();
            metocPointPanel.onMapClick(point);
            return true;
        }

        return false;
    }
    public void addPointMetoc(MetocForecastPoint mp, RouteMetocSettings settings) {
        graphics.add(new PointMetocGraphic(mp, settings, enavSettings));
        prepare();
    }
    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (obj instanceof MetocPointPanel) {
            metocPointPanel =  (MetocPointPanel) obj;

        }
    }
    @Override
    public void findAndUndo(Object o) {
        super.findAndUndo(o);
        if (o instanceof MetocPointPanel) {
            metocPointPanel =  null;
        }
    }
}
