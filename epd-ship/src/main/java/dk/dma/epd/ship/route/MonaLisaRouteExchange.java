/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.epd.ship.route;


import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bbn.openmap.MapHandlerChild;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.Heading;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.ship.EPDShip;
import dk.dma.epd.ship.ais.AisHandler;
import dk.dma.epd.ship.gps.GpsHandler;
import dk.dma.epd.ship.route.sspa.CurrentShipDataType;
import dk.dma.epd.ship.route.sspa.PositionType;
import dk.dma.epd.ship.route.sspa.RouteType;
import dk.dma.epd.ship.route.sspa.RouterequestType;
import dk.dma.epd.ship.route.sspa.RouteresponseType;
import dk.dma.epd.ship.route.sspa.WaypointType;
import dk.dma.epd.ship.route.sspa.WaypointsType;
import dk.dma.epd.ship.status.ComponentStatus;
import dk.dma.epd.ship.status.IStatusComponent;
import dk.dma.epd.ship.status.ShoreServiceStatus;

//import dk.frv.enav.ins.route.monalisa.se.sspa.optiroute.Routerequest;

/**
 * Shore service component providing the functional link to shore.
 */
public class MonaLisaRouteExchange extends MapHandlerChild implements
        IStatusComponent, Runnable {

    // private static final Logger LOG = Logger
    // .getLogger(MonaLisaRouteExchange.class);

    private AisHandler aisHandler;
    private GpsHandler gpsHandler;
    private ShoreServiceStatus status = new ShoreServiceStatus();

    private Route route;

    public void setRoute(Route route) {
        this.route = route;
    }

    public MonaLisaRouteExchange() {

    }

    public RouterequestType convertRoute(Route route) {

        float trim = 6.0f;

        // Create the route request
        RouterequestType monaLisaRoute = new RouterequestType();

        // Create the ship data
        CurrentShipDataType currentShipData = new CurrentShipDataType();

        if (aisHandler.getOwnShip().getStaticData() != null) {
            trim = aisHandler.getOwnShip().getStaticData().getDraught();
        }

        //Current ship data
        currentShipData.setAftdraft(trim);
        currentShipData.setForwarddraft(trim);
        currentShipData.setImoid("1234567");
        currentShipData.setMmsi("123456789");
        currentShipData.setUkcrequested(1.0f);

        monaLisaRoute.setCurrentShipData(currentShipData);

        RouteType monaLisaRouteType = new RouteType();

        WaypointsType waypoints = new WaypointsType();

        // Convert the existing waypoints into the Mona Lisa Format
        List<WaypointType> monaLisaWaypoints = waypoints.getWaypoint();
        LinkedList<RouteWaypoint> eeinsWaypoints = route.getWaypoints();

        for (int i = 0; i < eeinsWaypoints.size(); i++) {
            RouteWaypoint routeWaypoint = eeinsWaypoints.get(i);
            WaypointType waypoint = new WaypointType();

            // Set name
            waypoint.setWptName(routeWaypoint.getName());
            
            // Set ID
            waypoint.setWptId(i + 1);

            
//            if (i !=0){
                // Set ETA
                try {
                    waypoint.setETA(convertDate(route.getEtas().get(i)));
                } catch (DatatypeConfigurationException e) {
                    e.printStackTrace();
                }
//            }

            
            // Set positon
            PositionType position = new PositionType();

            position.setLatitude(routeWaypoint.getPos().getLatitude());
            position.setLongitude(routeWaypoint.getPos().getLongitude());

            waypoint.setPosition(position);
            
            monaLisaWaypoints.add(waypoint);


            
            // Set leg info
//            LeginfoType legInfo = new LeginfoType();
            // legInfo.setLegtype(value)
            // legInfo.setLhsXte(value)
            // legInfo.setRhsXte(value)

//            if (routeWaypoint.getOutLeg() != null) {
//                legInfo.setPlannedSpeed((float) routeWaypoint.getOutLeg().getSpeed());
//            } else {
//                legInfo.setPlannedSpeed(0.0f);
//            }

            // Rate of turn not needed
            // if (routeWaypoint.getRot() != null) {
            // legInfo.setTurnRadius(Double.valueOf(routeWaypoint.getRot())
            // .intValue());
            // } else {
            // legInfo.setTurnRadius(99);
            // }
            //
            // //Hardcoded to 99
            // legInfo.setTurnRadius(99);
            //
            //

//            waypoint.setLegInfo(legInfo);



        }

        monaLisaRouteType.setWaypoints(waypoints);
        monaLisaRoute.setRoute(monaLisaRouteType);

        return monaLisaRoute;
    }

    public Route convertRouteBack(RouteresponseType response) {
        Route route = new Route();

        route.setName("Optimized Mona Lisa Route");

        WaypointsType waypointsType = response.getRoute().getWaypoints();
        List<WaypointType> responseWaypoints = waypointsType.getWaypoint();

        LinkedList<RouteWaypoint> routeWaypoints = route.getWaypoints();

        for (int i = 0; i < responseWaypoints.size(); i++) {

            RouteWaypoint waypoint = new RouteWaypoint();
            WaypointType responseWaypoint = responseWaypoints.get(i);

            waypoint.setName(responseWaypoint.getWptName());

            if (i != 0) {
                RouteLeg inLeg = new RouteLeg();
                inLeg.setHeading(Heading.RL);
                waypoint.setInLeg(inLeg);

                // RouteWaypoint prevWaypoint =
                // routeWaypoints.get(routeWaypoints
                // .size() - 2);
                // System.out.println("For waypoint" + i + " creating in leg");
            }

            // Outleg always has next
            if (i != responseWaypoints.size() - 1) {
                RouteLeg outLeg = new RouteLeg();
                outLeg.setHeading(Heading.RL);
                waypoint.setOutLeg(outLeg);
                // System.out.println("For waypoint" + i + " creating out leg");
            }

            // if (waypoint.getInLeg() != null) {
            // waypoint.getInLeg().setSpeed(5.0);
            // }

            // if (waypoint.getOutLeg() != null) {
            // System.out.println("SEtting stuff?");
            // waypoint.getOutLeg().setSpeed(5.0);
            // // System.out.println(waypoint.getOutLeg().getSpeed());
            // }

            Position position = Position.create(responseWaypoint.getPosition()
                    .getLatitude(), responseWaypoint.getPosition()
                    .getLongitude());
            waypoint.setPos(position);

            if (responseWaypoint.getLegInfo() != null) {

                if (responseWaypoint.getLegInfo().getTurnRadius() != null) {
                    waypoint.setRot((double) responseWaypoint.getLegInfo()
                            .getTurnRadius());
                }

                if (responseWaypoint.getLegInfo().getPlannedSpeed() != null) {

                    waypoint.setSpeed(responseWaypoint.getLegInfo()
                            .getPlannedSpeed());

                }

                if (responseWaypoint.getLegInfo().getTurnRadius() != null) {
                    waypoint.setTurnRad((double) responseWaypoint.getLegInfo()
                            .getTurnRadius());
                }
            }

            routeWaypoints.add(waypoint);

        }

        if (routeWaypoints.size() > 1) {
            for (int i = 0; i < routeWaypoints.size(); i++) {

                // System.out.println("Looking at waypoint:" + i);
                RouteWaypoint waypoint = routeWaypoints.get(i);

                // Waypoint 0 has no in leg, one out leg... no previous
                if (i != 0) {
                    RouteWaypoint prevWaypoint = routeWaypoints.get(i - 1);

                    if (waypoint.getInLeg() != null) {
                        // System.out.println("Setting inleg prev for waypoint:"
                        // + i);
                        waypoint.getInLeg().setStartWp(prevWaypoint);
                        waypoint.getInLeg().setEndWp(waypoint);
                    }

                    if (prevWaypoint.getOutLeg() != null) {
                        // System.out.println("Setting outleg prev for waypoint:"
                        // + i);
                        prevWaypoint.getOutLeg().setStartWp(prevWaypoint);
                        prevWaypoint.getOutLeg().setEndWp(waypoint);

                    }
                }

            }
        }

        return route;
    }

    public boolean makeRouteRequest(Route route) {

        this.route = route;

        // new Thread(this).start();

        RouterequestType monaLisaRoute = convertRoute(route);

        
        RouteresponseType routeResponse = null;
        try {
            routeResponse = EPDShip.getShoreServices()
                    .makeMonaLisaRouteRequest(monaLisaRoute);
        } catch (Exception e) {
            return false;
        }
        
        
        if (routeResponse == null){
            return false;
        }
        
        Route newRoute = null;

        if (routeResponse != null) {
            try {
                newRoute = convertRouteBack(routeResponse);
            } catch (Exception e) {
                return false;
            }
        }

        if (newRoute != null) {
            EPDShip.getRouteManager().addRoute(newRoute);
        }

        return true;

    }

    @Override
    public void findAndInit(Object obj) {
        if (aisHandler == null && obj instanceof AisHandler) {
            aisHandler = (AisHandler) obj;
        }
        if (gpsHandler == null && obj instanceof GpsHandler) {
            gpsHandler = (GpsHandler) obj;
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        if (obj == aisHandler) {
            aisHandler = null;
        } else if (obj == gpsHandler) {
            gpsHandler = null;
        }
    }

    @Override
    public ComponentStatus getStatus() {
        return status;
    }

    @Override
    public void run() {

        RouterequestType monaLisaRoute = convertRoute(route);

        RouteresponseType routeResponse = EPDShip.getShoreServices()
                .makeMonaLisaRouteRequest(monaLisaRoute);

        Route newRoute = null;

        if (routeResponse != null) {
            newRoute = convertRouteBack(routeResponse);
        }

        if (newRoute != null) {

            EPDShip.getRouteManager().addRoute(newRoute);

        }
    }

    
    private XMLGregorianCalendar convertDate(Date date)
            throws DatatypeConfigurationException {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(c);

        //No time zone?
        date2.setTimezone( DatatypeConstants.FIELD_UNDEFINED );
        date2.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);


        return date2;

    }
}
