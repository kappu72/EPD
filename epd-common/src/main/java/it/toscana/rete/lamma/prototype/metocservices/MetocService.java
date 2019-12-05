package it.toscana.rete.lamma.prototype.metocservices;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.bbn.openmap.MapHandlerChild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.geometry.PositionTime;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceErrorCode;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.ActiveRoute;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.sensor.pnt.PntData;
import dk.dma.epd.common.prototype.sensor.pnt.PntHandler;
import dk.dma.epd.common.prototype.sensor.pnt.PntTime;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;
import it.toscana.rete.lamma.prototype.model.UVDimension;
import it.toscana.rete.lamma.prototype.model.Wave;
import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;
import it.toscana.rete.lamma.utils.Lattice;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

public class MetocService extends MapHandlerChild {

    static final double EARTH_RADIUS = 6371;

    public static final String U_WIND_10 = "u10";
    public static final String V_WIND_10 = "v10";
    public static final String H_WAVE_COMBINED = "swh";
    public static final String M_WAVE_PERIOD = "mwp";
    public static final String M_WAVE_DIRECTION = "mwd";
    public static final String MERIDIONAL_CURRENT = "vo";
    public static final String ZONAL_CURRENT = "uo";

    public static final int MAX_FORECAST_FUTURE = 60;

    protected PntHandler pntHandler;

    private static final Logger LOG = LoggerFactory.getLogger(MetocService.class);
    protected GridDataset metocDataset;

    public MetocService() {
        // Non so se vale la pena cachere il file dato che tiene in memoria solo i file
        // handler o altro
        // da approfondire
        // NetcdfDataset.initNetcdfFileCache(minElementsInMemory,maxElementsInMemory,period);

        // Setting up the Enhanced mode

        NetcdfDataset.setDefaultEnhanceMode(NetcdfDataset.getEnhanceAll());
    }

    public GridDataset openMetoc(String path) {
        try {
            metocDataset = GridDataset.open(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return metocDataset;
    }

/**
 * this method has to be overwritten by subclass this is the version for local file
 * @param metocSettings
 * @return
 */
    public GridDataset openMetoc(RouteMetocSettings metocSettings) {

        if (metocDataset == null || !metocDataset.getLocation().equals(metocSettings.getLocalMetocFile())) {
            
            return this.openMetoc(metocSettings.getLocalMetocFile());

        }
        return metocDataset;
    }

    public void closeCurrentMetoc() {
        if (null != metocDataset) {
            try {
                metocDataset.close();
            } catch (IOException ioe) {
                LOG.error("trying to close ", ioe);
            }
        }

    }

    public GridDataset getMetocDataset() {

        return metocDataset;

    }

    public void stop() {
        if (null != metocDataset) {
            try {
                metocDataset.close();
            } catch (IOException ioe) {
                LOG.error("trying to close ", ioe);
            }
        }
        NetcdfDataset.shutdown();
    }

    /**
     * Called when a bean is added to this bean context
     * 
     * @param obj the bean that was added
     */
    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if (pntHandler == null && obj instanceof PntHandler) {
            pntHandler = (PntHandler) obj;
        }
    }

    /**
     * Called when a bean is removed from this bean context
     * 
     * @param obj the bean that was removed
     */
    @Override
    public void findAndUndo(Object obj) {
        if (obj == pntHandler) {
            pntHandler = null;
        }
        super.findAndUndo(obj);
    }

    // Extract metoc value from a local file grib and netcdf format accepted files
    // as in ShoreServiceExceptions
    // First of all densify the waypoints
    public MetocForecast routeMetoc(Route route) throws ShoreServiceException, IOException {
        return routeMetoc(route, null);

    }


    public MetocForecast routeMetoc(Route route, Date now) throws ShoreServiceException, IOException {

        
        
        RouteMetocSettings metocSettings = route.getRouteMetocSettings();
        Route densifiedRoute = densifyRoute(route, now);
        if (now == null) {
            now = PntTime.getDate();
        }
        
        // To see densified route uncomment following line
        // EPD.getInstance().getRouteManager().addRoute(densifiedRoute);

        // Stream over generated waypoints

        openMetoc(metocSettings);
        
        GeoGrid v_10_Grid = metocDataset.findGridByShortName(V_WIND_10);
        GeoGrid u_10_Grid = metocDataset.findGridByShortName(U_WIND_10);
        GeoGrid swh_Grid = metocDataset.findGridByShortName(H_WAVE_COMBINED);
        GeoGrid mwd_Grid = metocDataset.findGridByShortName(M_WAVE_DIRECTION);
        GeoGrid mwp_Grid = metocDataset.findGridByShortName(M_WAVE_PERIOD);
        GeoGrid uo_Grid = metocDataset.findGridByShortName(ZONAL_CURRENT);
        GeoGrid vo_Grid = metocDataset.findGridByShortName(MERIDIONAL_CURRENT);

        GridCoordSystem gcs = v_10_Grid.getCoordinateSystem(); // Si assume che tutti i parametri nel file abbiano i
                                                               // medesimi sistemi di coordinate
        LatLonRect bbox = gcs.getLatLonBoundingBox();
        CalendarDateRange calendarDateRange = gcs.getCalendarDateRange();
        LinkedList<RouteWaypoint> wps = densifiedRoute.getWaypoints();

        List<MetocForecastPoint> metocs = IntStream.range(0, wps.size()).mapToObj(i -> {
            MetocPointForecast mp = new MetocPointForecast();

            RouteWaypoint p = wps.get(i);
            Position gp = p.getPos();
            double lat = gp.getLatitude();
            double lon = gp.getLongitude();
            Date eta = densifiedRoute.getEtas().get(i);
            mp.setLat(lat);
            mp.setLon(lon);
            mp.setTime(eta);

            // System.out.println(p.getName());
            // System.out.println(lon);
            // System.out.println(lat);
            // System.out.println(eta.toString());
            // Do the same for point outside temporal domain
            if(!bbox.contains(lat, lon) || !calendarDateRange.includes(CalendarDate.of(eta))) {
                // Skip points outside spatial domain
                return null;
            }
            try {
                // Ad ora fa 8 * n variabili di richieste, 56 per punto un pò troppe :-) su opendap
                Lattice lattice = new Lattice(gcs, lat, lon, eta);
                double v_10 = lattice.interpolateValue(v_10_Grid);
                double u_10 = lattice.interpolateValue(u_10_Grid);
                double swh = lattice.interpolateValue(swh_Grid);
                double mwd = lattice.interpolateValue(mwd_Grid);
                double mwp = lattice.interpolateValue(mwp_Grid);
                double uo = lattice.interpolateValue(uo_Grid);
                double vo = lattice.interpolateValue(vo_Grid);
                


                UVDimension wind = new UVDimension(FuelConsumptionCalculator.msTokn(u_10), FuelConsumptionCalculator.msTokn(v_10));
                
                UVDimension current = new UVDimension(FuelConsumptionCalculator.msTokn(uo), FuelConsumptionCalculator.msTokn(vo));
                Wave wave = new Wave(swh, mwd, mwp);
                mp.setWind(wind);
                mp.setMeanWave(wave);
                mp.setCurrent(current);
                
                // System.out.println("u_10 " + u_10 + " v_10 " + v_10 + " " + windD.getTheta() + " " + windD.getU() + " ° " + mp.getWindDirection().getForecast() + " " + mp.getWindSpeed().getForecast());
                // System.out.println("uo " + uo + " vo " + vo + " ° " + mp.getCurrentDirection().getForecast() + " " + mp.getCurrentSpeed().getForecast());
                // System.out.println("swh " + swh + " mwd " + mwd + " mwp " + mwp);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mp = null;
            }
                return mp;
        }).filter(mp -> mp != null).collect(Collectors.toList());
        MetocForecast forecast = new MetocForecast(now);
        forecast.setForecasts(metocs);
        return forecast;
    }

    /**
     * 
     * @param route the route to be densified
     * @param now   optional the date used to cal max MAX_FORECAST_FUTURE if null
     *              uses pnt handle date
     * @return
     * @throws ShoreServiceException
     */
    protected Route densifyRoute(Route route, Date now) throws ShoreServiceException {

        Position pos = null;
        Route densifiedRoute;

        densifiedRoute = route.copy();

        // if the route is active, we create a new starting waypoints from It's position
        // and
        // filter out all previous waypoints Vale solo per la rotta attiva!!
        if (route instanceof ActiveRoute) {
            PntData pntData = pntHandler.getCurrentData();
            if (pntData.isBadPosition()) {
                throw new ShoreServiceException(ShoreServiceErrorCode.NO_VALID_GPS_DATA);
            }
            pos = pntData.getPosition();
            ActiveRoute activeRoute = (ActiveRoute) route;
            // Recalculate all remaining ETA's
            if (!activeRoute.reCalcRemainingWpEta()) {
                throw new ShoreServiceException(ShoreServiceErrorCode.NO_VALID_GPS_DATA);
            }

            int startWpIndex = activeRoute.getActiveWaypointIndex();

            if (startWpIndex > 0) {

                LinkedList<RouteWaypoint> wps = densifiedRoute.getWaypoints();
                RouteWaypoint activeWp = wps.get(startWpIndex);
                RouteWaypoint newWaypoint = new RouteWaypoint(activeWp);
                newWaypoint.setPos(pos);
                RouteLeg leg = newWaypoint.getInLeg();
                leg.setStartWp(newWaypoint);
                activeWp.setInLeg(leg);
                newWaypoint.setOutLeg(leg);
                newWaypoint.setInLeg(null);
                wps.add(startWpIndex - 1, newWaypoint);
                // Filter out all previous waypoints
                // Fix waypoint names;
                LinkedList<RouteWaypoint> nWps = IntStream.range(startWpIndex - 1, wps.size()).mapToObj(i -> wps.get(i))
                        .collect(Collectors.toCollection(LinkedList::new));
                densifiedRoute.setWaypoints(nWps);
            }
        }

        RouteMetocSettings metocSettings = route.getRouteMetocSettings();
        long timeStep = 60000L * metocSettings.getInterval(); // comes in minutes to ms

        // Here we densified the route adding points with a metoc interval step, we also
        // filter out waypoint too much in future
        LinkedList<RouteWaypoint> newWps = densifiedRoute.getWaypoints().stream().flatMap(wp -> {
            LinkedList<RouteWaypoint> subRoute = new LinkedList<RouteWaypoint>();
            subRoute.add(wp);
            RouteLeg leg = wp.getOutLeg();
            if (leg != null) {
                long durata = leg.calcTtg();
                while (durata > timeStep) {

                    RouteWaypoint previousWaypoint = leg.getStartWp();
                    PositionTime nPosT = PositionTime.createExtrapolated(
                            PositionTime.create(previousWaypoint.getPos(), 0L), (float) leg.calcBrg(),
                            (float) leg.getSpeed(), (long) timeStep);

                    RouteWaypoint nextWaypoint = leg.getEndWp();

                    RouteWaypoint newWaypoint = new RouteWaypoint(previousWaypoint);
                    RouteLeg newRouteLeg = new RouteLeg(leg);
                    // set up legs
                    leg.setEndWp(newWaypoint);
                    newRouteLeg.setStartWp(newWaypoint);
                    // set up waypoints
                    newWaypoint.setInLeg(leg);
                    newWaypoint.setOutLeg(newRouteLeg);
                    newWaypoint.setPos((Position) nPosT);
                    newWaypoint.calcRot();
                    nextWaypoint.setInLeg(newRouteLeg);
                    subRoute.add(newWaypoint);
                    leg = newRouteLeg;
                    durata = leg.calcTtg();
                }
            }
            return subRoute.stream();
        }).collect(Collectors.toCollection(LinkedList::new));
        // Recalculate all etas
        densifiedRoute.setWaypoints(newWps);
        densifiedRoute.calcValues(true);

        List<Date> etas = densifiedRoute.getEtas();
        if (now == null) {
            now = PntTime.getDate();
        }
        Long nowMs = now.getTime();
        // Fix waypoint names;
        densifiedRoute.setWaypoints(IntStream.range(0, newWps.size()).filter(i -> {
            // Stop if ETA is too far in the future
            double inFutureHours = (etas.get(i).getTime() - nowMs) / 1000.0 / 3600.0;
            return inFutureHours < MAX_FORECAST_FUTURE;
        }).mapToObj(i -> {
            RouteWaypoint wp = newWps.get(i);
            wp.setName("D_WP_" + i);
            return wp;
        }).collect(Collectors.toCollection(LinkedList::new)));

        return densifiedRoute;

    }


    

}
