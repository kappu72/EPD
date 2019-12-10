package it.toscana.rete.lamma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.MaskFormatter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.text.Formatter;
import dk.frv.enav.common.xml.metoc.MetocForecastTriplet;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;
import it.toscana.rete.lamma.prototype.model.ShipData;
import it.toscana.rete.lamma.prototype.model.ThetaUDimension;
import it.toscana.rete.lamma.prototype.model.UVDimension;
import it.toscana.rete.lamma.prototype.model.Wave;

public class Utils {
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
	private static final String TAB = "\t";

	public static Path getHomePath () {
		if(EPD.getInstance() != null) {
			return EPD.getInstance().getHomePath();
		}
		return null;
	}
	/**
	 * Return current ships data directory if exists or try to create it
	 * @return
	 * @throws IOException 
	 */
	public static Path getShipsPath () throws IOException {
		Path homepath = getHomePath();
		if(homepath != null) {
			Path shipsPath =  Paths.get(homepath.toString(), "ships");
			if (!Files.exists(shipsPath)) {
	            Files.createDirectory(shipsPath);
	        }
			return shipsPath;
		}
		return null;
	}
	/**
	 * Scan the home/ships and search for ships folder
	 * @return List<String> of ships names
	 * @throws IOException 
	 */
	 public static List<ShipData> getShips() {
		List<ShipData> ships = new ArrayList<ShipData>();
		try {
			Path shipsPath = getShipsPath();
			if(shipsPath != null) {
				Files.newDirectoryStream(shipsPath, path -> path.toFile().isDirectory())
				.forEach(entry -> {
					ships.add(new ShipData(entry));
				});
			}
		}catch (IOException e) {
			LOG.error("Unable to create ships dir");
			e.printStackTrace();
		}
		return ships;
	}
	synchronized public static ShipData createShip(Object mmsi, Object name) {
		try {
			Path shipsPath = getShipsPath();
			if(shipsPath != null) {
				Path shipDir = Paths.get(shipsPath.toString(), buildShipDir(mmsi,name));
				Files.createDirectory(shipDir);
				return new ShipData(shipDir);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * MMSI format see https://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity
	 * 
	 * @return
	 */
	public static MaskFormatter getMMSIFormatter () {
		
		try {
			MaskFormatter formatter = new MaskFormatter("#########");
			formatter.setCommitsOnValidEdit(true);
			formatter.setPlaceholderCharacter('#');
			return formatter;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * MMSI format see https://en.wikipedia.org/wiki/Maritime_Mobile_Service_Identity
	 * @param String MMSI String Name given MMSI and name build ship dir name
	 * @return
	 */
	public static String buildShipDir (Object mmsi, Object name) {
		try {
			name = CharMatcher.JAVA_LETTER_OR_DIGIT.retainFrom((String) name);
		}catch (NullPointerException e) {
			e.printStackTrace();
		}
		return CharMatcher.is('_').trimFrom(Joiner.on('_').skipNulls().join( mmsi, name));
		
	}
	// Strip file name from path return dir
	public static String parseName (String dir) {
		String name, mmsi;
		List<String> els =  Splitter.on("_").splitToList(dir);
		int size = els.size();
		LOG.info(" " + CharMatcher.JAVA_DIGIT.countIn(els.get(0)) + " " + size);
		if(size == 2 ) {
			name = els.get(1);
			mmsi = els.get(0);
			return name + " (" + mmsi + ")";
		}else if(size == 1 && CharMatcher.JAVA_DIGIT.countIn(els.get(0)) == 9) {
			return "N/A " + "(" + els.get(0) + ")";
		}
		return dir;			
		
	}
	
	public static String cleanConditionName (Object name) {
		return CharMatcher.JAVA_LETTER_OR_DIGIT.retainFrom((String) name);	
	}
	public static String stripFileExt(File f) {
		int i = f.getName().lastIndexOf('.');
		return f.getName().substring(0,i);
	}
	public static boolean saveSimple(Route route, File file) {
		
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {

            // Write the header, making sure null isn't printed to the file
			String header = new String("Lat \t Lon \t Time \t cuU \t cuV \t wiU \t wiV \t cuSp \t cuD \t wiSp \t wiD \t waH \t waD \t waP \t");
			header += "TBRG \t rCuU \t rCuV \t rWiU \t rWiV \t rCuSp \t rCuD \t rWiSp \t rWiD \t wiDP \t waDP \t wiR \t waR \t huR \t fuelR \t fuel \t weight";
			writeRow(writer,header);
            
			String uom = new String("° \t ° \t DateTime \t kn \t kn \t kn \t kn \t m*s-1 \t ° \t m*s-1 \t ° \t m \t ° \t s \t");
			uom += "° \t kn \t kn \t kn \t kn \t kn \t ° \t kn \t ° \t ° \t ° \t kN \t kN \t kN \t t*h-1 \t t";
			writeRow(writer, uom);
			
			route.getWaypoints().stream()
			.filter(wp -> wp != null)
			.map(wp -> wp.getOutLeg())
			.filter(ol -> ol != null)
			.forEach(ol -> {
				String legRow = getLegRow(ol);
				writeRow(writer, legRow);
				String summaryRow = getFcRow(ol.getFuelConsumption());
				writeRow(writer, summaryRow);
				Iterator<FuelConsumption> iFc = ol.getInnerPointsConsumption().iterator();
				while(iFc.hasNext()) {
					FuelConsumption fc = iFc.next();
					String row = getFcRow(fc);
					writeRow(writer, row);
				}
				writeNL(writer);
				
			});
        } catch (IOException e) {
            LOG.error("Failed to save fuel consumption file: " + e.getMessage());
            return false;
        }
        return true;
	}

	private static String getFcRow(FuelConsumption fc) {

		MetocPointForecast me = fc.getMetoc();
		String row = fo(me.getLat()) + fo(me.getLon()) + Formatter.formatShortDateTime(me.getTime()) + TAB ;
		UVDimension cur = me.getCurrent();
		UVDimension wind = me.getWind();
		Wave wave = me.getMeanWave();
		row += fo(cur.getU()) + fo(cur.getV());
		row += fo(wind.getU()) + fo(wind.getV()) ;
		row += fo(me.getCurrentSpeed()) + fo(me.getCurrentDirection());
		row += fo(me.getWindSpeed()) + fo(me.getWindDirection());
		row += fo(wave.getHeight()) + fo(wave.getDirection()) + fo(wave.getPeriod());
		// Ends metoc start with output
		UVDimension curR = fc.getCurrent_rel_uv();
		UVDimension windR = fc.getWind_rel_uv();
		row += fo(fc.getHeading());
		row += fo(curR.getU()) + fo(curR.getV());
		row += fo(windR.getU()) + fo(windR.getV());
		ThetaUDimension curRT = fc.getCurrent_rel();
		ThetaUDimension windRT = fc.getWind_rel();
		row += fo(curRT.getU()) + fo(curRT.getTheta());
		row += fo(windRT.getU()) + fo(windRT.getTheta());
		row += fo(fc.getWind_polar()) + fo(fc.getWave_polar());
		row += fo(fc.getWind_resistance()) + fo(fc.getWave_resistance()) + fo(fc.getHull_resistance());
		row += fo(fc.getFuelRate()) + fo(fc.getFuel());
		row += fo(fc.getWeight());
		return row;

	}

	private static String getLegRow(RouteLeg leg) {
		String row = leg.getStartWp().getName() + " - " + leg.getEndWp().getName() + TAB + fo(leg.calcBrg()) + fo(leg.getSpeed()) + Formatter.formatTime(leg.calcTtg());
		return row;

	}



	/**
	 * just to format output and reduce writing
	 * @param val
	 * @return
	 */
	private static String fo(MetocForecastTriplet val) {
		
		return fo( val != null ? val.getForecast() : 0.);
	}
	private static String fo(double val) {
		return Formatter.formatDouble(val) + TAB;

	}
	private static void writeRow(BufferedWriter writer, String row) {
		try {
			writer.write(row);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	private static void writeNL(BufferedWriter writer) {
		try {
			writer.newLine();
			writer.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
