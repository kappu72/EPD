package it.toscana.rete.lamma.prototype.model.tables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;

import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;

/**
 * Utility class for loading ships fuel consumption tables
 * @author kappu
 *
 */

public class TableLoader {
	private static final Logger LOG = LoggerFactory.getLogger(TableLoader.class);
	private static String FORMAT_ERROR_MESSAGE = "Unrecognized format";

	// Load data from windres
	public static WindresTable laodWindres(Path path) throws RouteLoadException {
		BufferedReader reader = null;
		WindresTable windres = null;
		try {
			reader = new BufferedReader(new FileReader(path.toFile()));
			String line = null;
			boolean dimension = false;
			while ((line = reader.readLine()) != null) {
				// Ignore empty lines and comments
				String l = line.trim();
				if (l.length() == 0 || l.startsWith("//") || l.startsWith("#")) {
					continue;
				}
				if(l.startsWith("$1:Dr")) {
					if( dimension)
						throw new RouteLoadException(FORMAT_ERROR_MESSAGE);
					dimension = true;
					windres = new WindresTable(parseDimension(l));
				}else if(l.startsWith("1")) {
					windres.getWindres().setValues(parseValues(l));
					break;
				}
			}
		}
		catch (IOException e) {
			LOG.error("Failed to load windres table file: " + e.getMessage());
			throw new RouteLoadException("Error reading wondres file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return windres;
	}


	public static WaveresGenericTable laodWaveGenericTable(Path path) throws RouteLoadException {
		BufferedReader reader = null;
		WaveresGenericTable waveresg = null;
		Parameter tM = null;
		Parameter caw = null;
		Parameter spd = null;

		WavePeriods waveGeneric;
		try {
			reader = new BufferedReader(new FileReader(path.toFile()));
			String line = null;


			line = reader.readLine(); // comment skip
			line = reader.readLine().trim(); // spd
			spd = parseDimension(line);
			waveresg = new WaveresGenericTable(spd);
			line = reader.readLine().trim(); // Tm
			tM  = parseDimension(line);
			line = reader.readLine(); // skip Tp
			line = reader.readLine().trim(); // pdsComp
			caw = parseDimension(line);


			for(int i = 0; i <spd.getSize(); i++) {
				waveGeneric = waveresg.addWavePeriods(new WavePeriods(tM,caw), i);

				for(int n =0; n < tM.getSize(); n++) {
					line = reader.readLine().trim(); 
					waveGeneric.addCawValue(new OneDimDouble(caw.getMin(), caw.getMax(), caw.getDelta(), parseValues(line)), n);
					waveGeneric.addOptionalTValue(getTp(line), n);
				}
			}

		}
		catch (IOException e) {
			LOG.error("Failed to load windres table file: " + e.getMessage());
			throw new RouteLoadException("Error reading wondres file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
		}
		return waveresg;
	}


	public static WaveresCompTable laodWaveCompTable(Path path) throws RouteLoadException {
		BufferedReader reader = null;
		WaveresCompTable waveresComp = null;
		Parameter tP = null;
		Parameter caw = null;
		Parameter spd = null;

		WavePeriods waveGeneric;
		try {
			reader = new BufferedReader(new FileReader(path.toFile()));
			String line = null;

			// TODO aggiungi controllo se esiste commento prima linea
			line = reader.readLine(); // comment skip
			line = reader.readLine().trim(); // spd
			spd = parseDimension(line);
			waveresComp = new WaveresCompTable(spd);
			line = reader.readLine(); // skip Tm
			line = reader.readLine().trim(); // Tp
			tP  = parseDimension(line);

			line = reader.readLine().trim(); // cawComp
			caw = parseDimension(line);


			for(int i = 0; i <spd.getSize(); i++) {
				waveGeneric = waveresComp.addWavePeriods(new WavePeriods(tP,caw), i);

				for(int n =0; n < tP.getSize(); n++) {
					line = reader.readLine().trim(); 
					waveGeneric.addCawValue(new OneDimDouble(caw.getMin(), caw.getMax(), caw.getDelta(), parseValues(line)), n);
					waveGeneric.addOptionalTValue(getTm(line), n);
				}
			}

		}
		catch (IOException e) {
			LOG.error("Failed to load windres table file: " + e.getMessage());
			throw new RouteLoadException("Error reading wondres file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
		}
		return waveresComp;
	}

	public static FuelRateTable laodFuelRateTable(Path path) throws RouteLoadException {
		BufferedReader reader = null;
		FuelRateTable fuelRateTable = null;
		Parameter rHull = null;
		Parameter fuelRate = null;
		Parameter spd = null;

		WavePeriods waveGeneric;
		try {
			reader = new BufferedReader(new FileReader(path.toFile()));
			String line = null;

			// TODO aggiungi controllo se esiste commento prima linea
			line = reader.readLine(); // comment skip
			line = reader.readLine().trim(); // spd
			spd = parseDimension(line);

			line = reader.readLine().trim(); // fuelRate
			fuelRate = parseDimension(line);

			fuelRateTable = new FuelRateTable(spd, fuelRate, path.toString());

			int frMin, frMax, frDelta;
			frMin = (int) fuelRate.getMin();
			frMax = (int) fuelRate.getMax();
			frDelta = (int) fuelRate.getDelta();

			for(int n =0; n < spd.getSize(); n++) {
				line = reader.readLine().trim(); 
				fuelRateTable.addfuelRateValue(new OneDimDouble(frMin, frMax, frDelta, parseValues(line)), n);
			}


		}
		catch (IOException e) {
			LOG.error("Failed to load windres table file: " + e.getMessage());
			throw new RouteLoadException("Error reading wondres file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
		}
		return fuelRateTable;
	}


	private static El parseDimensionEl(String f) throws RouteLoadException {
		try {

			String[] d = f.split(":");
			return new El(d[0], Double.parseDouble(d[1]));
		}catch (Exception e) {
			throw new RouteLoadException(FORMAT_ERROR_MESSAGE);
		}
	}
	private static Parameter parseDimension(String dimension) throws RouteLoadException {

		try {
			String[] fields = dimension.split("\\|");
			El var = parseDimensionEl(fields[0].substring(fields[0].indexOf(":")+1));
			El min = parseDimensionEl(fields[1]);
			El max = parseDimensionEl(fields[2]);
			El delta = parseDimensionEl(fields[3]);
			return new Parameter(var.getName(), min.getValue(), max.getValue(), delta.getValue(), (int) var.getValue(),  "1".contentEquals(fields[4]) );
		}catch (Exception e) {
			throw new RouteLoadException(FORMAT_ERROR_MESSAGE);
		}
	}
	private static Double[] parseValues(String line) {
		return  Arrays.stream((line.split("\\$"))[1].split(","))
				.map(Double::parseDouble)
				.toArray(Double[]::new);
	}
	private static Double getTp(String line) {
		String tPstr = line.split("\\$")[0].split("\\|")[4];
		return Double.parseDouble(tPstr.substring(tPstr.lastIndexOf(":") + 1));
	}
	private static Double getTm(String line) {
		String tMstr = line.split("\\$")[0].split("\\|")[3];
		return Double.parseDouble(tMstr.substring(tMstr.lastIndexOf(":") + 1));
	}
	static  class El {
		String name;
		double  value;

		public El(String name, double value) {
			super();
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public double getValue() {
			return value;
		}
	}

}
