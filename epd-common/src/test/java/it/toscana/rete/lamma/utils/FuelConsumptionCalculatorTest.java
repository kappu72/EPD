/**
 * 
 */
package it.toscana.rete.lamma.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import it.toscana.rete.lamma.prototype.model.ThetaUDimension;
import it.toscana.rete.lamma.prototype.model.UVDimension;
import it.toscana.rete.lamma.prototype.model.tables.FuelRateTable;
import it.toscana.rete.lamma.prototype.model.tables.TableLoader;
import it.toscana.rete.lamma.prototype.model.tables.WaveresGenericTable;
import it.toscana.rete.lamma.prototype.model.tables.WindresTable;

/**
 * @author kappu
 *
 */
public class FuelConsumptionCalculatorTest {

	public static Path getResourcePath(String filename) throws URISyntaxException {
        URL url = ClassLoader.getSystemResource(filename);
        Assert.assertNotNull(url);
        File file = new File(url.toURI());
        Assert.assertNotNull(file);
        return 	file.toPath();
    }
	@Test
	public final void testReverseAngle() {
		
		assertEquals(180.0, FuelConsumptionCalculator.reverseAngle(0.0), 0.0001);
		assertEquals(90.0, FuelConsumptionCalculator.reverseAngle(270), 0.0001);
		assertEquals(45., FuelConsumptionCalculator.reverseAngle(225.), 0.0001);
	}
	
	
	@Test
	public final void testWrapTo360() {
		
		assertEquals(2.0, FuelConsumptionCalculator.wrapTo360(-358), 0.0001);
		assertEquals(10, FuelConsumptionCalculator.wrapTo360(730), 0.0001);
		assertEquals(350, FuelConsumptionCalculator.wrapTo360(-730), 0.0001);
	}
	@Test
	public final void testWrapTo180() {
		
		assertEquals(2.0, FuelConsumptionCalculator.wrapTo180(-358), 0.0001);
		assertEquals(10, FuelConsumptionCalculator.wrapTo180(730), 0.0001);
		assertEquals(170, FuelConsumptionCalculator.wrapTo180(-730), 0.0001);
	}
	
	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#rilevamentoPolare(double, double)}.
	 */
	@Test
	public final void testRilevamentoPolare() {
		double a = 90.0;
		double b = 225.0;
		double result = FuelConsumptionCalculator.rilevamentoPolare(a, b);
		assertEquals(result, 135.0, 0.00001);
		b = 10.0;
		result = FuelConsumptionCalculator.rilevamentoPolare(a, b);
		assertEquals(result, -80.0, 0.00001);
				
	}

	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#vectorToSpeedDir(it.toscana.rete.lamma.prototype.model.UVDimension)}.
	 */
	@Test
	public final void testvectorToSpeedDir() {
		 UVDimension val = new UVDimension(2, 1);
		 
		 ThetaUDimension result = FuelConsumptionCalculator.vectorToSpeedDir(val);
		 assertEquals(2.2361, result.getU(), 0.0001);
		 assertEquals(243.4349, result.getTheta(), 0.0001);
		 
	}
	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#speedDirToVector(ThetaUDimension)}.
	 */
	@Test
	public final void testspeedDirToVector() {
		
		ThetaUDimension val = new ThetaUDimension(10, 180);
		UVDimension result = FuelConsumptionCalculator.speedDirToVector(val);
		assertEquals(0.0, result.getU(), 0.0001);
		assertEquals(-10.0, result.getV(), 0.0001);
		
	}
	
	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#kinematical(ThetaUDimension, ThetaUDimension)
	 */
	@Test
	public final void testkinematical() {
		
		ThetaUDimension SOG = new ThetaUDimension(10, 180);
		UVDimension CUR_SOG = new UVDimension(2, 1);
		
		ThetaUDimension	result = FuelConsumptionCalculator.kinematical(CUR_SOG, SOG, true);
		assertEquals(11.1803, result.getU(), 0.0001);
		assertEquals(10.3048, result.getTheta(), 0.0001);
		ThetaUDimension	result1 = FuelConsumptionCalculator.kinematical(CUR_SOG, SOG, false);
		assertEquals(11.1803, result1.getU(), 0.0001);
		assertEquals(10.3048, result1.getTheta(), 0.0001);
	}
	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#CalculateAllKinematical(ThetaUDimension, UVDimension, UVDimension, double, boolean)
	 */
	@Test
	public final void testAllKinematic() {
		// input
		ThetaUDimension SOG = new ThetaUDimension(10, 180);
		UVDimension CUR_SOG = new UVDimension(2, 1);
		double wave_dir = 190;
		UVDimension WIND_SOG = new UVDimension(-2, 1);
		
		FuelConsumption r = FuelConsumptionCalculator.CalculateAllKinematical(SOG, CUR_SOG, WIND_SOG, wave_dir, true);
		// checks rel_cur
		assertEquals(11.1803, r.getCurrent_rel().getU(), 0.0001);
		assertEquals(10.3048, r.getCurrent_rel().getTheta(), 0.0001);
		// heading
		assertEquals(190.3048, r.getHeading(), 0.0001);
		// polar wave dir
		assertEquals(-0.30485, r.getWave_polar(), 0.0001);
		// wind_rel
		assertEquals(11.1803, r.getWind_rel().getU(), 0.0001);
		assertEquals(-10.3048, r.getWind_rel().getTheta(), 0.0001);
		//  polar wind dir
		assertEquals(-20.6097, r.getWind_polar(), 0.0001);
		
		r = FuelConsumptionCalculator.CalculateAllKinematical(SOG, CUR_SOG, WIND_SOG, wave_dir, false);
		// checks rel_cur
		assertEquals(11.1803, r.getCurrent_rel().getU(), 0.0001);
		assertEquals(10.3048, r.getCurrent_rel().getTheta(), 0.0001);
		// heading
		assertEquals(190.3048, r.getHeading(), 0.0001);
		// polar wave dir
		assertEquals(-0.30485, r.getWave_polar(), 0.0001);
		// wind_rel
		assertEquals(11.1803, r.getWind_rel().getU(), 0.0001);
		assertEquals(-10.3048, r.getWind_rel().getTheta(), 0.0001);
		//  polar wond dir
		assertEquals(-20.6097, r.getWind_polar(), 0.0001);
	}
	/**
	 * Test method for {@link it.toscana.rete.lamma.utils.FuelConsumptionCalculator#CalculateResistance(ThetaUDimension, UVDimension, UVDimension, double, double, double, it.toscana.rete.lamma.prototype.model.tables.WindresTable, it.toscana.rete.lamma.prototype.model.tables.WaveresGenericTable, int, boolean)}
	 * @throws URISyntaxException 
	 * @throws RouteLoadException 
	 */
	@Test
	public final void testCalcRes() throws RouteLoadException, URISyntaxException {
		// input
		WindresTable cxRes = TableLoader.laodWindres(getResourcePath("MEII_ldcnd01_windres_FjwrCrs.dat"));
		WaveresGenericTable cawRes = TableLoader.laodWaveGenericTable(getResourcePath("MEII_ldcnd01_waveres_Genrc.dat"));
		FuelRateTable fr = TableLoader.laodFuelRateTable(getResourcePath("MEII_ldcnd01_fuelrate_Ns2Nel2.dat"));
		double waveH= 2;
		double waveD=190;
		double waveTm=4.50;
		UVDimension WIND_SOG = new UVDimension(-5, 5);
		ThetaUDimension SOG = new ThetaUDimension(20, 180);
		UVDimension CUR_SOG = new UVDimension(2, 1);
		
		FuelConsumption c = FuelConsumptionCalculator.CalculateAllKinematical(SOG, CUR_SOG, WIND_SOG, waveD, true);
		FuelConsumption r = FuelConsumptionCalculator.CalculateResistance(c, waveH, waveTm, cxRes, cawRes, 850);
		double fuelRate = fr.getFuelRate((float) r.getCurrent_rel().getU(), (float) r.getTotalAddedResistance());
		
	 }
	 
	
}
