package it.toscana.rete.lamma.prototype.metocservices;

import com.bbn.openmap.MapHandlerChild;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.geometry.PositionTime;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceErrorCode;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetocService extends MapHandlerChild {

    public static final String[] U_WIND_10 = {"u10", "10_metre_U_wind_component_height_above_ground"};
    public static final String[] V_WIND_10 = {"v10", "10_metre_V_wind_component_height_above_ground"};
    public static final String[] H_WAVE_COMBINED = {"swh", "Significant_height_of_combined_wind_waves_and_swell_msl"};
    public static final String[] M_WAVE_PERIOD = {"mwp", "Mean_wave_period_msl"};
    public static final String[] M_WAVE_DIRECTION = {"mwd", "Mean_wave_direction_msl"};
    public static final String[] MERIDIONAL_CURRENT = {"vo", "v-component_of_current_depth_below_sea"};
    public static final String[] ZONAL_CURRENT = {"uo", "u-component_of_current_depth_below_sea"};
    // Le partizioni devono aggiungere il postfisso con il numero
    public static final String[] SWELL_WAVE_H = {"swh", ""}; // mancano i nomi per il grib
    public static final String[] SWELL_WAVE_P = {"mwp", ""}; // mancano i nomi per il grib
    public static final String[] SWELL_WAVE_D = {"mwd", ""}; // mancano i nomi per il grib
    public static final String[] WIND_WAVE_H = {"swhw", ""}; // mancano i nomi per il grib
    public static final String[] WIND_WAVE_P = {"mwpw", ""}; // mancano i nomi per il grib
    public static final String[] WIND_WAVE_D = {"mwdw", ""}; // mancano i nomi per il grib
    public static final int MAX_FORECAST_FUTURE = 60;
    static final double EARTH_RADIUS = 6371;
    private static final Logger LOG = LoggerFactory.getLogger(MetocService.class);
    protected PntHandler pntHandler;
    protected GridDataset metocDataset;
    private GeoGrid v_10_grid;
    private GeoGrid u_10_grid;
    private GeoGrid swh_grid;
    private GeoGrid mwd_grid;
    private GeoGrid mwp_grid;
    private GeoGrid uo_grid;
    private GeoGrid vo_grid;
    private GeoGrid swhw_grid;
    private GeoGrid mwdw_grid;
    private GeoGrid mwpw_grid;
    private List<GeoGrid> swhP_grid;
    private List<GeoGrid> mwpP_grid;
    private List<GeoGrid> mwdP_grid;
    private GridCoordSystem gcs;

    public MetocService() {
        // Non so se vale la pena to cache il file dato che tiene in memoria solo i file
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
     *
     * @param metocSettings
     * @return
     */
    public synchronized GridDataset openMetoc(RouteMetocSettings metocSettings) {

        if (metocDataset == null || !metocDataset.getLocation().equals(metocSettings.getLocalMetocFile())) {
            clearDatasets();
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


    public synchronized MetocForecast routeMetoc(Route route, Date now) throws ShoreServiceException, IOException {


        RouteMetocSettings metocSettings = route.getRouteMetocSettings();
        Route densifiedRoute = densifyRoute(route, now);
        if (now == null) {
            now = PntTime.getDate();
        }

        // To see densified route uncomment following line
        // EPD.getInstance().getRouteManager().addRoute(densifiedRoute);

        // Stream over generated waypoints

        openMetoc(metocSettings);
        openGrids();
        Boolean hasPartitions = hasPartitions();

        // medesimi sistemi di coordinate

        LinkedList<RouteWaypoint> wps = densifiedRoute.getWaypoints();

        List<MetocForecastPoint> metocs = IntStream.range(0, wps.size()).mapToObj(i ->
        {
            RouteWaypoint p = wps.get(i);
            Position gp = p.getPos();
            double lat = gp.getLatitude();
            double lon = gp.getLongitude();
            Date eta = densifiedRoute.getEtas().get(i);

            return readMetocPointForecast(lat,lon,eta, hasPartitions);

        } ).filter(mp -> mp != null).collect(Collectors.toList());
        MetocForecast forecast = new MetocForecast(now);
        forecast.setForecasts(metocs);
        return forecast;
    }

    /**
     * Read all forecast parameter for give position and time
     * @param lat
     * @param lon
     * @param eta
     * @param hasPartitions
     * @return
     */
    public MetocPointForecast readMetocPointForecast(double lat, double lon, Date eta, boolean hasPartitions) {
        LatLonRect bbox = gcs.getLatLonBoundingBox();
        CalendarDateRange calendarDateRange = gcs.getCalendarDateRange();
        if (!bbox.contains(lat, lon) || !calendarDateRange.includes(CalendarDate.of(eta))) {
            // Skip points outside spatial and time domain
            return null;
        }

        MetocPointForecast mp = new MetocPointForecast();

        mp.setLat(lat);
        mp.setLon(lon);
        mp.setTime(eta);


        try {
            // Ad ora fa 8 * n variabili di richieste, 56 per punto un pò troppe :-) su opendap
            Lattice lattice = new Lattice(gcs, lat, lon, eta);
            double v_10 = lattice.interpolateValue(v_10_grid);
            double u_10 = lattice.interpolateValue(u_10_grid);
            double swh = lattice.interpolateValue(swh_grid);
            double mwd = lattice.interpolateValue(mwd_grid);
            double mwp = lattice.interpolateValue(mwp_grid);
            double uo = lattice.interpolateValue(uo_grid);
            double vo = lattice.interpolateValue(vo_grid);


            // IMPORTANT:: Porti tutto a nodi! SOG in nodi wind speed in nodi curren tspeed in nodi
            UVDimension wind = new UVDimension(FuelConsumptionCalculator.msTokn(u_10), FuelConsumptionCalculator.msTokn(v_10));
            UVDimension current = new UVDimension(FuelConsumptionCalculator.msTokn(uo), FuelConsumptionCalculator.msTokn(vo));

            Wave wave = new Wave(swh, mwd, mwp);
            mp.setWind(wind);
            mp.setMeanWave(wave);
            mp.setCurrent(current);
            if (hasPartitions) {
                mp.setWindWave(readWindWave(lattice, swhw_grid, mwdw_grid, mwpw_grid));
                mp.setSwellWave(readSwellComponents(lattice, swhP_grid, mwdP_grid, mwpP_grid));
                List<Wave> ckList = new ArrayList<Wave>(mp.getSwellWave());
                if(mp.getWindWave() != null ) ckList.add(mp.getWindWave());
                if (ckList.size() > 0 && Math.abs(waveHeightQuadraticSum(ckList) - mp.getMeanWave().getHeight()) <= 0.2) {
                    mp.setHasPartitions(true);
                } else {
                    LOG.info("Wave partition doesn't match swh");
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mp = null;
        }
        return mp;
    }

    protected void clearDatasets() {
        v_10_grid = null;
        u_10_grid = null;
        swh_grid = null;
        mwd_grid = null;
        mwp_grid = null;
        uo_grid = null;
        vo_grid = null;
        swhw_grid = null;
        mwdw_grid = null;
        mwpw_grid = null;
        swhP_grid = null;
        mwpP_grid = null;
        mwdP_grid = null;
        gcs = null;
    }

    /**
     * Open all needed datasets if needed cheks un wind v component
     *
     * @throws ShoreServiceException
     */

    public void openGrids() throws ShoreServiceException {
        if (v_10_grid == null) {
            v_10_grid = getGeoGridByName(V_WIND_10);
            u_10_grid = getGeoGridByName(U_WIND_10);
            swh_grid = getGeoGridByName(H_WAVE_COMBINED);
            mwd_grid = getGeoGridByName(M_WAVE_DIRECTION);
            mwp_grid = getGeoGridByName(M_WAVE_PERIOD);
            uo_grid = getGeoGridByName(ZONAL_CURRENT);
            vo_grid = getGeoGridByName(MERIDIONAL_CURRENT);
            gcs = v_10_grid.getCoordinateSystem();
            swhw_grid = null;
            mwdw_grid = null;
            mwpw_grid = null;
            swhP_grid = null;
            mwpP_grid = null;
            mwdP_grid = null;
            /**
             * Le Partizioni possono esistere oppure essere assenti nel file
             * e sono composte dalle onde da vento e swell, lo swell può avere da 1 a 5 componenti
             */
            Boolean hasPartitions = false;
            try {
                swhw_grid = getGeoGridByName(WIND_WAVE_H);
                mwdw_grid = getGeoGridByName(WIND_WAVE_D);
                mwpw_grid = getGeoGridByName(WIND_WAVE_P);
                hasPartitions = true;
            } catch (ShoreServiceException e) {
                LOG.info("Partizioni non disponibili");
            }
            if (hasPartitions) {
                swhP_grid = getPartitionsGrid(SWELL_WAVE_H);
                mwpP_grid = getPartitionsGrid(SWELL_WAVE_P);
                mwdP_grid = getPartitionsGrid(SWELL_WAVE_D);
            }
        }
    }

    public GridCoordSystem getGcs() {
        if(gcs == null) {
            try {
                gcs = getGeoGridByName(V_WIND_10).getCoordinateSystem();
            } catch (ShoreServiceException e) {
                e.printStackTrace();
            }
        }
        return gcs;
    }

    public boolean hasPartitions() {
        return swhw_grid != null
                && mwdw_grid != null
                && mwpw_grid != null
                && swhP_grid != null
                && mwpP_grid != null
                && mwdP_grid != null;
    }

    private double waveHeightQuadraticSum(List<Wave> waveComps) {
        return Math.sqrt(waveComps.stream()
                .mapToDouble(w -> Math.pow(w.getHeight(), 2.))
                .sum());
    }

    private List<Wave> readSwellComponents(Lattice lattice, List<GeoGrid> finalSwhP_grid, List<GeoGrid> finalMwdP_grid, List<GeoGrid> finalMwpP_grid) {
        List<Wave> comps = new ArrayList<Wave>();

        try {
            for (int ii = 0; ii < finalSwhP_grid.size(); ii++) {
                Wave v = new Wave(lattice.interpolateValue(finalSwhP_grid.get(ii), true),
                        lattice.interpolateValue(finalMwdP_grid.get(ii), true),
                        lattice.interpolateValue(finalMwpP_grid.get(ii), true));
                v.sanitize();
                comps.add(v);
            }
        } catch (IOException | NullPointerException e) {
                e.printStackTrace();
        }
        return comps;
    }

    private Wave readWindWave(Lattice lattice, GeoGrid swhw_grid, GeoGrid mwdw_Grid, GeoGrid mwpw_Grid) {
        try {
            Wave w = new Wave(lattice.interpolateValue(swhw_grid, true),
                    lattice.interpolateValue(mwdw_Grid, true),
                    lattice.interpolateValue(mwpw_Grid, true));
            w.sanitize();
            return w;
        } catch (IOException | NullPointerException e) {
            return null;
        }


    }

    /**
     * Read swell partitions grid from input file
     * Partition could vary from none to five components
     *
     * @return
     */
    private List<GeoGrid> getPartitionsGrid(String[] component_names) {
        List<GeoGrid> partitions = new ArrayList<GeoGrid>();
        String[] names;
        for (int i = 1; i < 6; i++) {
            names = new String[]{component_names[0] + i, component_names[1] + i};
            try {
                partitions.add(getGeoGridByName(names));
            } catch (ShoreServiceException e) {
                e.printStackTrace();
                break;
            }
        }
        return partitions;

    }

    /**
     * Search a parameter by names
     *
     * @param names
     * @return
     * @throws ShoreServiceException
     */
    private GeoGrid getGeoGridByName(String[] names) throws ShoreServiceException {
        for (String name : names) {
            GeoGrid g = metocDataset.findGridByShortName(name);
            if (g != null) {
                return g;
            }
        }
        throw new ShoreServiceException("Missing mandatory metoc parameter: " + names[0]);
    }

    /**
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
