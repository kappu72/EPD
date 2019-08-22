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
package dk.dma.epd.ship.route;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.model.route.ActiveRoute;
import dk.dma.epd.common.prototype.model.route.ActiveRoute.ActiveWpSelectionResult;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteSuggestionData;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.dma.epd.common.prototype.sensor.pnt.IPntDataListener;
import dk.dma.epd.common.prototype.sensor.pnt.PntData;
import dk.dma.epd.common.prototype.sensor.pnt.PntHandler;
import dk.dma.epd.common.prototype.sensor.pnt.PntTime;
import dk.dma.epd.ship.EPDShip;
import dk.dma.epd.ship.gui.component_panels.ShowDockableDialog;
import dk.dma.epd.ship.gui.component_panels.ShowDockableDialog.dock_type;

/**
 * Manager for handling a collection of routes and active route
 */
@ThreadSafe
public class RouteManager extends RouteManagerCommon implements
        IPntDataListener {

    private static final long serialVersionUID = -9019124285849351709L;
    private static final String ROUTES_FILE = EPD.getInstance().getHomePath()
            .resolve(".routes").toString();
    private static final Logger LOG = LoggerFactory
            .getLogger(RouteManager.class);

    private volatile PntHandler pntHandler;

    @GuardedBy("routeSuggestions")
    private List<RouteSuggestionData> routeSuggestions = new LinkedList<>();

    /**
     * Constructor
     */
    public RouteManager() {
        super();
    }

    /**
     * Called when receiving a position update
     * 
     * @param pntData
     *            the updated position
     */
    @Override
    public void pntDataUpdate(PntData pntData) {
        if (!isRouteActive()) {
            return;
        }
        if (pntData.isBadPosition()) {
            return;
        }

        ActiveWpSelectionResult endRes;
        ActiveWpSelectionResult res;
        synchronized (this) {
            activeRoute.update(pntData);
            endRes = activeRoute.chooseActiveWp();
            res = endRes;
            // Keep chosing active waypoint until not changed any more
            while (res == ActiveWpSelectionResult.CHANGED) {
                res = activeRoute.chooseActiveWp();
            }
        }

        // If last change ended route, this will be result
        if (res == ActiveWpSelectionResult.ROUTE_FINISHED) {
            endRes = ActiveWpSelectionResult.ROUTE_FINISHED;
        }

        if (endRes == ActiveWpSelectionResult.CHANGED) {
            notifyListeners(RoutesUpdateEvent.ACTIVE_ROUTE_UPDATE);
        } else if (endRes == ActiveWpSelectionResult.ROUTE_FINISHED) {
            synchronized (this) {
                activeRoute = null;
                activeRouteIndex = -1;
            }
            notifyListeners(RoutesUpdateEvent.ACTIVE_ROUTE_FINISHED);
        }
    }

    /**************************************/
    /** Active Route operations **/
    /**************************************/

    /**
     * Activates the route with the given index
     * 
     * @param index
     *            the index of the route to activate
     */
    public void activateRoute(int index) {
        synchronized (this) {
            if (index < 0 || index >= routes.size()) {
                LOG.error("Could not activate route with index: " + index);
                return;
            }

            if (isRouteActive()) {
                // Deactivate route
                deactivateRoute();
            }

            Route route = routes.get(index);
            route.setVisible(true);
            // Set active route index
            activeRouteIndex = index;

            // Create new
            activeRoute = new ActiveRoute(route, pntHandler.getCurrentData());

            // Set the minimum WP circle radius
            activeRoute.setWpCircleMin(EPDShip.getInstance().getSettings()
                    .getNavSettings().getMinWpRadius());
            // Set relaxed WP change
            activeRoute.setRelaxedWpChange(EPDShip.getInstance().getSettings()
                    .getNavSettings().isRelaxedWpChange());
            // Inject the current position
            activeRoute.update(pntHandler.getCurrentData());
            // Set start time to now
            activeRoute.setStarttime(PntTime.getDate());
        }

        // Is the GUI created and active yet

        if (EPDShip.getInstance().getMainFrame() != null) {

            // If the dock isn't visible should it show it?
            if (!EPDShip.getInstance().getMainFrame().getDockableComponents()
                    .isDockVisible("Active Waypoint")) {

                // Show it display the message?
                if (EPDShip.getInstance().getSettings().getGuiSettings()
                        .isShowDockMessage()) {
                    new ShowDockableDialog(
                            EPDShip.getInstance().getMainFrame(),
                            dock_type.ROUTE);
                } else {

                    if (EPDShip.getInstance().getSettings().getGuiSettings()
                            .isAlwaysOpenDock()) {
                        EPDShip.getInstance().getMainFrame()
                                .getDockableComponents()
                                .openDock("Active Waypoint");
                        EPDShip.getInstance().getMainFrame().getJMenuBar()
                                .refreshDockableMenu();
                    }

                    // It shouldn't display message but take a default action

                }

            }
        }
        // Notify listeners
        notifyListeners(RoutesUpdateEvent.ROUTE_ACTIVATED);
    }

    /**
     * Deactivates the currently active route
     */
    public void deactivateRoute() {
        synchronized (this) {
            activeRoute = null;
            activeRouteIndex = -1;
        }

        notifyListeners(RoutesUpdateEvent.ROUTE_DEACTIVATED);
    }

    /**************************************/
    /** Life cycle operations **/
    /**************************************/

    /**
     * Loads and instantiates a {@code RouteManager} from the default routes
     * file.
     * 
     * @return the new route manager
     */
    public static RouteManager loadRouteManager() {
        RouteManager manager = new RouteManager();

        try (FileInputStream fileIn = new FileInputStream(ROUTES_FILE);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);) {
            RouteStore routeStore = (RouteStore) objectIn.readObject();
            manager.setRoutes(routeStore.getRoutes());
            manager.activeRoute = routeStore.getActiveRoute();
            manager.activeRouteIndex = routeStore.getActiveRouteIndex();

            // manager.setTempActiveRouteIndex(routeStore.getActiveRouteIndex());
            // activeRouteIndex = routeStore.getActiveRouteIndex();

            if (routeStore.getActiveRouteIndex() > -1) {
                manager.deactivateRoute();
            }

        } catch (FileNotFoundException e) {
            // Not an error
        } catch (Exception e) {
            LOG.error("Failed to load routes file: " + e.getMessage());
            e.printStackTrace();
            // Delete possible corrupted or old file
            new File(ROUTES_FILE).delete();
        }

        return manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void saveToFile() {
        RouteStore routeStore = new RouteStore(this);
        try (FileOutputStream fileOut = new FileOutputStream(ROUTES_FILE);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);) {
            objectOut.writeObject(routeStore);
        } catch (IOException e) {
            LOG.error("Failed to save routes file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (pntHandler == null && obj instanceof PntHandler) {
            pntHandler = (PntHandler) obj;
            pntHandler.addListener(this);

            // Found pnt handler, will activate route now to find best WP match
            // of current position
//            if (tempActiveRouteIndex > -1) {

//                activateRoute(tempActiveRouteIndex);

            }
        }
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findAndUndo(Object obj) {
        if (pntHandler == obj) {
            pntHandler.removeListener(this);
        }
        super.findAndUndo(obj);
    }

    @Override
    public void notifyListeners(RoutesUpdateEvent e) {
        super.notifyListeners(e);

        if (EPDShip.getInstance().getVoctHandler() != null) {
            EPDShip.getInstance().getVoctManager().saveToFile();

        }

    }


}
