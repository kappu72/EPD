/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.epd.common.prototype.layers.route;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.coords.LatLonPoint;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.math.Vector2D;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.metoc.MetocGraphic;
import dk.dma.epd.common.prototype.gui.metoc.MetocInfoPanel;
import dk.dma.epd.common.prototype.gui.metoc.MetocPointGraphic;
import dk.dma.epd.common.prototype.gui.util.InfoPanel;
import dk.dma.epd.common.prototype.gui.views.ChartPanelCommon;
import dk.dma.epd.common.prototype.layers.EPDLayerCommon;
import dk.dma.epd.common.prototype.model.route.IRoutesUpdateListener;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;

/**
 * Base class for EPDShip and EPDShore route layers
 */
public abstract class RouteLayerCommon extends EPDLayerCommon implements IRoutesUpdateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RouteLayerCommon.class);

    private MetocInfoPanel metocInfoPanel = new MetocInfoPanel();
    private WaypointInfoPanel routeInfoPanel = new WaypointInfoPanel();

    protected RouteManagerCommon routeManager;
    protected WaypointCircle selectedWp;
    protected OMGraphicList metocGraphics = new OMGraphicList();
    protected boolean arrowsVisible;
    protected MetocGraphic routeMetoc;

    private OMCircle dummyCircle = new OMCircle();
    private ChartPanelCommon chartPanel;

    /**
     * Constructor
     */
    public RouteLayerCommon() {
        super();

        // Register the info panels
        registerInfoPanel(metocInfoPanel, MetocPointGraphic.class);
        registerInfoPanel(routeInfoPanel, WaypointCircle.class, RouteLegGraphic.class);

        // Register the classes the will trigger the map menu
        registerMapMenuClasses(WaypointCircle.class, RouteLegGraphic.class);
    }

    /**
     * Calculate distance between displayed METOC-points projected onto the screen
     * 
     * @param metocGraphic
     *            METOC-graphics containing METOC-points
     * @return The smallest distance between displayed METOC-points projected onto the screen
     */
    public double calculateMetocDistance(MetocGraphic metocGraphic) {
        List<OMGraphic> forecasts = metocGraphic.getTargets();
        double minDist = 0;
        for (int i = 0; i < forecasts.size(); i++) {
            if (i < forecasts.size() - 2) {
                MetocPointGraphic metocForecastPoint = (MetocPointGraphic) forecasts.get(i);
                MetocPointGraphic metocForecastPointNext = (MetocPointGraphic) forecasts.get(i + 1);
                double lat = metocForecastPoint.getLat();
                double lon = metocForecastPoint.getLon();

                double latnext = metocForecastPointNext.getLat();
                double lonnext = metocForecastPointNext.getLon();

                Point2D current = getProjection().forward(lat, lon);
                Point2D next = getProjection().forward(latnext, lonnext);

                Vector2D vector = new Vector2D(current.getX(), current.getY(), next.getX(), next.getY());

                double newDist = vector.norm();

                if (i == 0) {
                    minDist = newDist;
                }

                if (minDist > newDist) {
                    minDist = newDist;
                }
            }
        }
        return minDist;
    }

    /**
     * Calculate distance between each METOC-point projected onto the screen
     * 
     * @param route
     *            The route which contains metoc data (check for this before!)
     * @return The smallest distance between METOC-points projected onto the screen
     */
    public double calculateMetocDistance(Route route) {
        MetocForecast routeMetoc = route.getMetocForecast();
        List<MetocForecastPoint> forecasts = routeMetoc.getForecasts();
        double minDist = 0;
        for (int i = 0; i < forecasts.size(); i++) {
            if (i < forecasts.size() - 2) {
                MetocForecastPoint metocForecastPoint = forecasts.get(i);
                MetocForecastPoint metocForecastPointNext = forecasts.get(i + 1);
                double lat = metocForecastPoint.getLat();
                double lon = metocForecastPoint.getLon();

                double latnext = metocForecastPointNext.getLat();
                double lonnext = metocForecastPointNext.getLon();

                Point2D current = getProjection().forward(lat, lon);
                Point2D next = getProjection().forward(latnext, lonnext);

                Vector2D vector = new Vector2D(current.getX(), current.getY(), next.getX(), next.getY());

                double newDist = vector.norm();

                if (i == 0) {
                    minDist = newDist;
                }

                if (minDist > newDist) {
                    minDist = newDist;
                }
            }
        }
        return minDist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean initInfoPanel(InfoPanel infoPanel, OMGraphic newClosest, MouseEvent evt, Point containerPoint) {
        if (newClosest instanceof MetocPointGraphic) {
            MetocPointGraphic pointGraphic = (MetocPointGraphic) newClosest;
            MetocForecastPoint pointForecast = pointGraphic.getMetocPoint();
            metocInfoPanel.showText(pointForecast, pointGraphic.getMetocGraphic().getRoute().getRouteMetocSettings());
            return true;

        } else if (newClosest instanceof WaypointCircle) {
            WaypointCircle waypointCircle = (WaypointCircle) closest;
            routeInfoPanel.showWpInfo(waypointCircle.getRoute(), waypointCircle.getWpIndex());
            return true;
        } else if (newClosest instanceof RouteLegGraphic) {
            
            RouteLegGraphic routeLeg = (RouteLegGraphic) newClosest;
            
            if (routeLeg.getRouteGraphic() == null){
//                System.out.println("Route Graphic is null?");
            }
            
            Point2D worldLocation = chartPanel.getMap().getProjection().inverse(evt.getPoint());
            routeInfoPanel.showLegInfo((RouteLegGraphic) newClosest, worldLocation);
            closest = dummyCircle;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initMapMenu(OMGraphic clickedGraphics, MouseEvent evt) {

        if (clickedGraphics instanceof WaypointCircle) {
            WaypointCircle wpc = (WaypointCircle) clickedGraphics;
            getMapMenu().routeWaypointMenu(wpc.getRouteIndex(), wpc.getWpIndex());

        } else if (clickedGraphics instanceof RouteLegGraphic) {
            RouteLegGraphic rlg = (RouteLegGraphic) clickedGraphics;
            getMapMenu().routeLegMenu(rlg.getRouteIndex(), rlg.getRouteLeg(), evt.getPoint());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mouseDragged(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return false;
        }

        if (selectedWp == null) {
            selectedWp = (WaypointCircle) getSelectedGraphic(e, WaypointCircle.class);
        }

        if (selectedWp != null) {
            hideInfoPanels();

            // Handle non-active route case
            if (routeManager.getActiveRouteIndex() != selectedWp.getRouteIndex()) {
                Route r = selectedWp.getRoute();
                if(r.hasFuelConsumption()) {
                    int dialogresult = JOptionPane.showConfirmDialog(EPD.getInstance().getMainFrame(),
                        "Editing this root will reset metoc and fuel consumption \nDo you wish to make to edit?", "Route Editing",
                        JOptionPane.YES_OPTION);
                    if (dialogresult == JOptionPane.YES_OPTION) {
                        moveWP(e);
                    }
                }else {
                    moveWP(e);
                }
                return true;
            } else {
                // Attempting to drag an active route, make a route copy and drag that one.
                // NB: This case will only ever be reached for EPDShip
                int dialogresult = JOptionPane.showConfirmDialog(EPD.getInstance().getMainFrame(),
                        "You are trying to edit an active route \nDo you wish to make a copy to edit?", "Route Editing",
                        JOptionPane.YES_OPTION);
                if (dialogresult == JOptionPane.YES_OPTION) {
                    Route route = routeManager.getRoute(routeManager.getActiveRouteIndex()).copy();
                    route.setName(route.getName() + " copy");
                    routeManager.addRoute(route);
                }
                selectedWp = null;
                return true;
            }
        }

        return false;
    }

    private void moveWP (MouseEvent e) {
        RouteWaypoint routeWaypoint = selectedWp.getRoute().getWaypoints().get(selectedWp.getWpIndex());
                LatLonPoint pos = mapBean.getProjection().inverse(e.getPoint());
                routeWaypoint.setPos(Position.create(pos.getLatitude(), pos.getLongitude()));

                // Invalidate the STCC approval flag
                if (selectedWp.getRoute().isStccApproved()) {
                    selectedWp.getRoute().setStccApproved(false);
                    try {
                        selectedWp.getRoute().setName(selectedWp.getRoute().getName().split(":")[1].trim());
                    } catch (Exception e2) {
                        LOG.debug("Failed to remove STCC Approved part of name");
                    }
                }
                // Force recalc of metoc
                selectedWp.getRoute().calcValues(true);
                routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_WAYPOINT_MOVED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mouseReleased(MouseEvent e) {
        if (selectedWp != null) {
            selectedWp = null;
            routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_MSI_UPDATE);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized OMGraphicList prepare() {
        float showArrowScale = EPD.getInstance().getSettings().getNavSettings().getShowArrowScale();
        for (OMGraphic omgraphic : graphics) {
            if (omgraphic instanceof RouteGraphic) {
                ((RouteGraphic) omgraphic).showArrowHeads(getProjection().getScale() < showArrowScale);
            }
        }

        List<OMGraphic> metocList = metocGraphics.getTargets();
        for (OMGraphic omGraphic : metocList) {
            MetocGraphic metocGraphic = (MetocGraphic) omGraphic;
            Route route = metocGraphic.getRoute();
            if (routeManager.showMetocForRoute(route)) {
                double minDist = calculateMetocDistance(route);
                int step = Math.max((int) (5.0 / minDist), 1);
                metocGraphic.setStep(step);

                // temporary fix for drawing metoc information
                // All scales will draw all metoc.
                metocGraphic.setStep(1);

                metocGraphic.paintMetoc();
            }
        }

        graphics.project(getProjection());
        return graphics;
    }

    /**
     * Called when a new bean is added to the bean context
     * 
     * @param obj
     *            the bean being added
     */
    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (obj instanceof RouteManagerCommon) {
            routeManager = (RouteManagerCommon) obj;
            routeManager.addListener(this);
        } else if (obj instanceof ChartPanelCommon) {
            chartPanel = (ChartPanelCommon) obj;
        }
    }

    /**
     * Called when a bean is removed from the bean context
     * 
     * @param obj
     *            the bean being removed
     */
    @Override
    public void findAndUndo(Object obj) {
        if (obj == routeManager) {
            routeManager.removeListener(this);
        }
        super.findAndUndo(obj);
    }

}
