package it.toscana.rete.lamma.prototype.grib;

import java.io.Serializable;
import java.util.Properties;

import com.bbn.openmap.util.PropUtils;

public class GribSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String PREFIX = "grib.";
	// Requested grib file
	private String path = "src/main/resources/grib/WW3_Mediterraneo_2017122200-whole.grb";
	
	// NetCdf cache settings
	private int minElementsInMemory = 100; //keep this number in the cache
	private int maxElementsInMemory = 200; // trigger a cleanup if it goes over this number.
	private int period = 15*60;              // (secs) do periodic cleanups every this number of seconds. set to < 0 to not cleanup
	
	public GribSettings() {
		// TODO Auto-generated constructor stub
	}
	
	 public static String getPrefix() {
	        return PREFIX;
	    }
	    
    public void readProperties(Properties props) {
    	
    	path = props.getProperty(PREFIX + "path", path);
    	minElementsInMemory = PropUtils.intFromProperties(props, "minElementsInMemory", minElementsInMemory);
    	maxElementsInMemory = PropUtils.intFromProperties(props, "maxElementsInMemory", maxElementsInMemory);
    	period = PropUtils.intFromProperties(props, "period", period);
    }
    public void setProperties(Properties props) {
        props.put(PREFIX + "path", path);
    }
    
    public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getMinElementsInMemory() {
		return minElementsInMemory;
	}

	public void setMinElementsInMemory(int minElementsInMemory) {
		this.minElementsInMemory = minElementsInMemory;
	}

	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	public void setMaxElementsInMemory(int maxElementsInMemory) {
		this.maxElementsInMemory = maxElementsInMemory;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
	

}
