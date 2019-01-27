package it.toscana.rete.lamma.prototype.grib;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.openmap.MapHandlerChild;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.Heading;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceErrorCode;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.model.route.ActiveRoute;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.sensor.pnt.PntData;
import dk.dma.epd.common.prototype.sensor.pnt.PntHandler;
import dk.dma.epd.common.prototype.shoreservice.Metoc;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import dk.frv.enav.common.xml.metoc.MetocForecastTriplet;
import dk.frv.enav.common.xml.metoc.request.MetocForecastRequest;
import dk.frv.enav.common.xml.metoc.request.MetocForecastRequestWp;
import ucar.coord.CalendarDateFactory;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;
import ucar.ma2.Array;
import dk.dma.epd.common.util.Calculator;
import com.bbn.openmap.proj.RhumbCalculator;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.LatLonPoint.Double;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;
import static java.lang.Math.toDegrees;


public class GribServices extends MapHandlerChild {
	
	 static final double EARTH_RADIUS = 6371;
	
	public static final String  WS = "Wind_speed_surface";
	public static final String  WD = "Wind_direction_surface";
	public static final String  SH = "Sig_height_of_wind_waves_and_swell_surface";
	public static final String  SD = "Direction_of_wind_waves_surface"; 
	
	
	protected GribSettings gribSettings;
	private LammaGrib grib;
	private PntHandler pntHandler;
	
	private static final Logger LOG = LoggerFactory
            .getLogger(GribServices.class);
	
	public GribServices(GribSettings gribSettings) {
		this.gribSettings = gribSettings;
		NetcdfDataset.initNetcdfFileCache(gribSettings.getMinElementsInMemory(),gribSettings.getMaxElementsInMemory(),gribSettings.getPeriod());
		try {
			grib = new LammaGrib(gribSettings.getPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stop() {
		try {
			grib.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetcdfDataset.shutdown();
	}
	/**
     * Called when a bean is added to this bean context
     * 
     * @param obj
     *            the bean that was added
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
     * @param obj
     *            the bean that was removed
     */
    @Override
    public void findAndUndo(Object obj) {
        if (obj == pntHandler) {
            pntHandler = null;
        }
        super.findAndUndo(obj);
    }
	
	// Extract metoc value from grib files as in ShoreServiceExceptions
	public MetocForecast routeMetoc(Route route) throws ShoreServiceException {
		
		Position pos = null;
        if (route instanceof ActiveRoute) {
            PntData pntData = pntHandler.getCurrentData();
            if (pntData.isBadPosition()) {
                throw new ShoreServiceException(ShoreServiceErrorCode.NO_VALID_GPS_DATA);
            }
            pos = pntData.getPosition();
        }
        MetocForecastRequest request = Metoc.generateMetocRequest(route, pos);
        List<MetocForecastRequestWp> waypoints = new ArrayList<>();
        
        
        
        
        for(int i=0; i< request.getWaypoints().size() -1; i++) {
        	MetocForecastRequestWp curWp = request.getWaypoints().get(i);
        	MetocForecastRequestWp nextWp = request.getWaypoints().get(i + 1);
        	waypoints.add(curWp);
        	long secs = (nextWp.getEta().getTime() - curWp.getEta().getTime()) / 1000;
        
        	double d = Position.create(curWp.getLat(), curWp.getLon()).rhumbLineDistanceTo(Position.create(nextWp.getLat(), nextWp.getLon())) ;
        	double speed = d/secs; // m/s
        	 while (farApart(curWp, nextWp, request.getDt())) {   				
        		curWp = createWaypoint(curWp, nextWp,speed, request.getDt());
        		waypoints.add(curWp);
              }
        }
        // Add last Point
        waypoints.add(request.getWaypoints().get((request.getWaypoints().size() - 1)));
     
  
 

        
        final long diff = new Date().getTime() - grib.getStartDate().getMillis() + (1000 * 3600 * 2); // trick to use an old lamma grib file
		// Stream over generated waypoints
        List<MetocForecastPoint> forecasts = waypoints.parallelStream().map(w -> {
        	MetocForecastPoint point = new MetocForecastPoint();
        	point.setLat(w.getLat());
        	point.setLon(w.getLon());
        	point.setTime(new Date ( w.getEta().getTime() - diff));
        	try {
        		int timeIndex = grib.getTimeindex(CalendarDate.of(new Date ( w.getEta().getTime() - diff)), GribServices.WS );
	        	Array wspeeds= grib.getParamAtPoint(GribServices.WS, w.getLat(), w.getLon());
	        	Array wdirss= grib.getParamAtPoint(GribServices.WD, w.getLat(), w.getLon());
	        	Array sdirss= grib.getParamAtPoint(GribServices.SD, w.getLat(), w.getLon());
	        	Array sheights= grib.getParamAtPoint(GribServices.SH, w.getLat(), w.getLon());
        		        		
	        	point.setWindDirection(new MetocForecastTriplet(wdirss.getDouble(timeIndex)));
	        	point.setWindSpeed(new MetocForecastTriplet(wspeeds.getDouble(timeIndex)));
	        	point.setMeanWaveDirection(new MetocForecastTriplet(sdirss.getDouble(timeIndex)));
	        	point.setMeanWaveHeight(new MetocForecastTriplet(sheights.getDouble(timeIndex)));
            	
        		
			} catch (Exception e) {
				e.printStackTrace();

			}
        	return point;
        	
        	
        }).collect(Collectors.toList());
        
        MetocForecast mf = new MetocForecast(new Date());
        mf.setForecasts(forecasts);
		
		return mf;
		}
	 
	private boolean farApart(MetocForecastRequestWp wp1, MetocForecastRequestWp wp2, int interval) {
	        long dif = wp2.getEta().getTime() - wp1.getEta().getTime();
	        
	        return dif > interval * 60 * 1000;
	   }

	private MetocForecastRequestWp createWaypoint(MetocForecastRequestWp curWp, MetocForecastRequestWp nextWp, double speed, int dt) {
		
		Heading heading = Heading.valueOf(curWp.getHeading());
		Position curP = Position.create(curWp.getLat(), curWp.getLon());
		Position nextP = Position.create(nextWp.getLat(), nextWp.getLon());
		
		if (heading == Heading.RL) {
			
            Position nP = positionAt(curP,curP.rhumbLineBearingTo(nextP), speed * dt * 60);
            
           
            MetocForecastRequestWp nm =  new MetocForecastRequestWp();
            nm.setLat(nP.getLatitude());
            nm.setLon(nP.getLongitude());
            nm.setHeading(curWp.getHeading());
            nm.setEta(new Date(curWp.getEta().getTime() + (60000 * dt)));
            return nm;
        } 
           return null;
       }
	 /**
     * Calculates the position following a rhumb line with the given bearing for the specified distance.
     *
     * @param bearing
     *            the bearing (in compass degrees)
     * @param distance
     *            the distance (in meters)
     * @return    the position
     */
	 public Position positionAt(Position p, double bearing, double distance) {
	        final double d = distance / (EARTH_RADIUS*1e3);
	        final double bearingRad = toRadians(bearing);
	        final double lat1Rad = toRadians(p.getLatitude());
	        final double lon1Rad = toRadians(p.getLongitude());

	        final double lat2Rad = asin(sin(lat1Rad)*cos(d) + cos(lat1Rad)*sin(d)*cos(bearingRad));
	        final double a = atan2(sin(bearingRad)*sin(d)*cos(lat1Rad), cos(d) - sin(lat1Rad)*sin(lat2Rad));
	        final double lon2Rad = (lon1Rad + a + 3*PI) % (2*PI) - PI;

	        final double lat2 = toDegrees(lat2Rad);
	        final double lon2 = toDegrees(lon2Rad);

	        return Position.create(lat2, lon2);
	    }

}
