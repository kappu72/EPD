package it.toscana.rete.lamma.prototype.gui.menuitems;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.math.Vector2D;
import dk.dma.epd.common.prototype.gui.menuitems.event.IMapMenuAction;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.dma.epd.common.util.Calculator;
import it.toscana.rete.lamma.prototype.gui.MetocPanelCommon;
import it.toscana.rete.lamma.prototype.gui.WMSTimePanelCommon;
import dk.dma.epd.common.util.Calculator;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.LinkedList;


public class TimePanelMenu extends JMenuItem implements IMapMenuAction {

    private static final long serialVersionUID = 1L;

    private int routeWaypointIndex;
    private int routeIndex;
    private RouteManagerCommon routeManager;
    private WMSTimePanelCommon wmsTimePanel;
    private MetocPanelCommon metocPanelCommon;
    private RouteLeg leg;
    private Point point;
    private MapBean mapBean;

    public TimePanelMenu(String text) {
        super();
        setText(text);
    }

    @Override
    public void doAction() {
        if(routeWaypointIndex != -1) {
        // Recupera la rotta, il punto ed il tempo dal punto infine settalo nel selettore ore
            setTimePanels(routeManager.getRoute(routeIndex).getWpEta(routeWaypointIndex));
        }else if(point != null && leg != null && mapBean != null){
            //Calcualte the time in the middle
            Position rP = calcolatePointOnLeg();
            if (rP != null) {
                double brg = Calculator.range(leg.getStartWp().getPos(), rP, leg.getHeading());
                long timems = Math.round(brg * 3600.0 / leg.getSpeed() * 1000.0);
                Date startEta = routeManager.getRoute(routeIndex).getEtas().get(findWaypointIdx());
                Date newDate = new Date(startEta.getTime() + timems);
                setTimePanels(newDate);
            }

        }

    }

    private void setTimePanels (Date d) {
        if(wmsTimePanel != null &&  wmsTimePanel.isVisible())
            wmsTimePanel.selectTime(d);
        if(metocPanelCommon != null && metocPanelCommon.isVisible())
            metocPanelCommon.selectTime(d);
    }

    private int findWaypointIdx() {
            LinkedList<RouteWaypoint> list = routeManager.getRoute(routeIndex).getWaypoints();
            for (int i =0 ; i<list.size(); i++ ) {
                if(list.get(i) == leg.getStartWp()) return i;
            }
            return 0;
    }

    private Position calcolatePointOnLeg() {
        Position startWaypoint = leg.getStartWp().getPos();
        Position endWaypoint = leg.getEndWp().getPos();
        Projection projection = mapBean.getProjection();
        LatLonPoint newPoint = projection.inverse(point);

        Vector2D routeLegVector = new Vector2D(startWaypoint.getLongitude(),
                startWaypoint.getLatitude(),
                endWaypoint.getLongitude(),
                endWaypoint.getLatitude());

        Vector2D newVector = new Vector2D(startWaypoint.getLongitude(),
                startWaypoint.getLatitude(),
                newPoint.getLongitude(),
                newPoint.getLatitude());

        Vector2D projectedVector = routeLegVector.projection(newVector);

       return  Position.create(projectedVector.getY2(), projectedVector.getX2());
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

    public void setLeg(RouteLeg leg) {
        this.leg = leg;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setMapBean(MapBean mapBean) {
        this.mapBean = mapBean;
    }
}