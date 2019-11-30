package it.toscana.rete.lamma.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MaskFormatter;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.EPD;
import it.toscana.rete.lamma.prototype.model.ShipData;

public class Utils {
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
	
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
}
