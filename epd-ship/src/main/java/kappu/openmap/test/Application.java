package kappu.openmap.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.time.CalendarDate;

public class Application {
	private static Logger LOG;
	public static void	 main (String[] ergs) throws IOException {
		
		DOMConfigurator.configure(Paths.get("/Users/kappu/lamma/EPD/epd-ship/src/main/resources/log4j.xml").toUri().toURL());
		
		LOG = LoggerFactory
	            .getLogger(Application.class);
		
		NetcdfDataset.initNetcdfFileCache(100,200,15*60);
		
		LammaGrib grib = new LammaGrib();
		Array values = grib.getParamAtPoint("Wind_speed_surface", 34.0, 12.0);
		System.out.println( values.getDouble(grib.getTimeindex(CalendarDate.of(null, 2017, 12, 23, 13, 0, 0), "Wind_speed_surface")));
//		java.awt.EventQueue.invokeLater(new Runnable() {
//			         @Override
//			         public void run() {
//			            new MapFrame().setVisible(true);
//			         }
//			      });
		grib.close();
		NetcdfDataset.shutdown();
	}
	

}
