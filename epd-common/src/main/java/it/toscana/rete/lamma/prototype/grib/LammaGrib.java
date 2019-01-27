package it.toscana.rete.lamma.prototype.grib;



import java.io.IOException;
import java.util.MissingResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
/**
 * Generic class to extract data form lamma's grib file
 * @author kappu
 *
 */
public class LammaGrib {
	private static final Logger LOG = LoggerFactory
            .getLogger(LammaGrib.class);

	
	private NetcdfDataset grib = null;
	
	private GridDataset gds = null;
	
	public GridDataset getGds() {
		return gds;
	}
	private String path;
	
	public LammaGrib(String grib) throws IOException {
		
		
		path = grib;
		this.grib = NetcdfDataset.acquireDataset(path, true, null);
		gds = new GridDataset(this.grib);
		LOG.debug("Grib start date: "+ gds.getCalendarDateStart().toString());
		LOG.debug("Grib end date: "+ gds.getCalendarDateEnd().toString());
		LOG.debug("Available parameters");
		for(VariableSimpleIF var: gds.getDataVariables()) {
			LOG.debug(var.getShortName());
		}
	}
	
	/**
	 * Return meteo param values for a specific lat, lng 
	 */
	public Array getParamAtPoint(String param, double lat, double lon) throws IOException, MissingResourceException, IndexOutOfBoundsException  {
		GridDatatype grid = getGrid(param);
		int[] xy = getXYindex(lat, lon, grid);
		Array data = grid.readDataSlice(-1, 0, xy[1], xy[0]); 
		return data;
	}
	public void close() throws IOException {
		if(grib != null){
			grib.close();
		}
	}
	private GridDatatype getGrid(String param) throws MissingResourceException{
		GridDatatype grid = gds.findGridDatatype(param);
		if(grid == null) {
			throw new MissingResourceException("Requested parameter("+param+") not present", path, param);
		}
		return grid;
	}
	private int[] getXYindex(double lat, double lon, GridDatatype grid) throws IndexOutOfBoundsException {
		GridCoordSystem gcs = grid.getCoordinateSystem();
		int[] xy = gcs.findXYindexFromLatLon(lat, lon, null);
		if(xy[0] == -1 || xy[1] == -1) {
			throw new IndexOutOfBoundsException("Requested point is outside grib bbox");
		}
		return xy;
	}
	/**
	 * Return the index of passed date if it's included in grib run 
	 * note that normally netcdf lib return the max or min value if the date It' outside
	 */
	public int getTimeindex(CalendarDate datetime, String param) throws IndexOutOfBoundsException, MissingResourceException{
		GridDatatype grid = gds.findGridDatatype(param);
		GridCoordSystem gcs = grid.getCoordinateSystem();
		if(!gcs.hasTimeAxis1D()) {
			throw new MissingResourceException("Time dimension missing", path, "time");
		}
		CoordinateAxis1DTime tax = gcs.getTimeAxis1D();
		// Check if the date is included in the domain or throw an exception
		CalendarDateRange dtRange = tax.getCalendarDateRange();
		if(!dtRange.includes(datetime)) {
			System.out.println(datetime.toString());
			throw new IndexOutOfBoundsException("Requested datetime is outside grib timeseries");
		}
		int ti = gcs.getTimeAxis1D().findTimeIndexFromCalendarDate(datetime);
		return ti;
	}
	public CalendarDate getStartDate() {
		return gds.getCalendarDateStart();
	}

}
