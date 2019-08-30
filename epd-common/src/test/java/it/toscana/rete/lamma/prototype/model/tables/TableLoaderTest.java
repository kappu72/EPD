package it.toscana.rete.lamma.prototype.model.tables;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;


import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import it.toscana.rete.lamma.prototype.model.tables.TableLoader;
import it.toscana.rete.lamma.prototype.model.tables.WaveresGenericTable;
import it.toscana.rete.lamma.prototype.model.tables.WindresTable;

public class TableLoaderTest {
	public static Path getResourcePath(String filename) throws URISyntaxException {
        URL url = ClassLoader.getSystemResource(filename);
        Assert.assertNotNull(url);
        File file = new File(url.toURI());
        Assert.assertNotNull(file);
        return 	file.toPath();
    }
	/**
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.TableLoader#laodWindres(Path)}
	 * @throws RouteLoadException
	 * @throws URISyntaxException
	 */
	@Test
	public final void testLaodWindres() throws RouteLoadException, URISyntaxException {
		WindresTable wind = TableLoader.laodWindres(getResourcePath("MEII_ldcnd01_windres_FjwrCrs.dat"));
		Assert.assertNotNull(wind);
		assertEquals(-0.063,  wind.getWindres().getValue(50), 0.0001);
		assertEquals(-0.1645, wind.getWindres().getWeightedValue(55), 0.00001);
	}
	/**
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.TableLoader#laodWaveGenericTable(Path)}
	 * @throws RouteLoadException
	 * @throws URISyntaxException
	 */
	@Test
	public final void testLaodWaveResGen() throws RouteLoadException, URISyntaxException {
		WaveresGenericTable wave = TableLoader.laodWaveGenericTable(getResourcePath("MEII_ldcnd01_waveres_Genrc.dat"));
		Assert.assertNotNull(wave);
		
		// Testing values
		assertEquals(12.136,  wave.getCawValue(28, 6, 160), 0.0001);
		
		assertEquals(14.647,  wave.getCawValue( 27.3F, 5.6F, 145), 0.0001);

	}
	/**
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.TableLoader#laodWaveCompTable(Path)}
	 * @throws RouteLoadException
	 * @throws URISyntaxException
	 */
	@Test
	public final void testLaodWaveResComp() throws RouteLoadException, URISyntaxException {
		WaveresCompTable wave = TableLoader.laodWaveCompTable(getResourcePath("MEII_ldcnd01_waveres_Swell.dat"));
		Assert.assertNotNull(wave);
		
		// Testing values
		assertEquals(10.433,  wave.getCawValue(28, 6, 160), 0.0001);
		
		assertEquals(13.6675,  wave.getCawValue( 27.3F, 5.6F, 145), 0.0001);

	}
	/**
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.TableLoader#laodWaveCompTable(Path)}
	 * @throws RouteLoadException
	 * @throws URISyntaxException
	 */
	@Test
	public final void testLaodFuelRate() throws RouteLoadException, URISyntaxException {
		FuelRateTable fr = TableLoader.laodFuelRateTable(getResourcePath("MEII_ldcnd01_fuelrate_Ns2Nel1.dat"));
		Assert.assertNotNull(fr);
		
		// Testing values
		assertEquals(2.8708,  fr.getFuelRate(20, -70), 0.0001);
		
		assertEquals(1.3307,  fr.getFuelRate(11.8F, -100F), 0.0001);

	}
}
