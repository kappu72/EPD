package it.toscana.rete.lamma.prototype.model.tables;

import static org.junit.Assert.*;

import org.junit.Test;

import com.bbn.openmap.corba.CSpecialist.ForceArrowOperations;

import it.toscana.rete.lamma.prototype.model.tables.OneDimTable;

public class OneDimTableTest {

	/**
	 * 
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.OneDimTable#getValue(float)} 
	 */
	@Test
	public final void testGetValue() {
		Double[] vals = new Double[] {0D, 10D, 20D, 30D, 40D, 50D, 60D, 70D, 80D, 90D, 100D,
				110D, 120D, 130D, 140D, 150D, 160D, 170D, 180D};
		OneDimTable<Double> p = new OneDimTable<Double>(0, 180, 10, vals);
		Double res = p.getValue(40F);
		assertEquals(40D, res, 0.000000000);
		res = p.getValue(29F);
		assertEquals(30D, res, 0.000000000);
		
		vals= new Double[]{ 5D, 6D, 7D, 8D, 9D, 10D, 11D, 12D, 13D, 14D, 15D, 16D, 17D, 18D, 19D,
							20D, 21D, 22D, 23D, 24D, 25D, 26D, 27D, 28D};
		p = new OneDimTable<Double>(5, 28, 1, vals);
		res = p.getValue(5F);
		assertEquals(5D, res, 0.000000000);
		res = p.getValue(28F);
		assertEquals(28D, res, 0.000000000);
		
	}
	/**
	 * 
	 * Test method {@link it.toscana.rete.lamma.prototype.model.tables.OneDimTable#getWeightedValue(float)} 
	 */
	@Test
	public final void testGetWeightedValue() {
		Double[] vals = new Double[] {0D, 10D, 20D, 30D, 40D, 50D, 60D, 70D, 80D, 90D, 100D,
				110D, 120D, 130D, 140D, 150D, 160D, 170D, 180D};
		OneDimTable<Double> p = new OneDimTable<Double>(0, 180, 10, vals);
		double res = p.getWeightedValue(5);
		assertEquals(5D, res, 0.000000000);
		res = p.getWeightedValue(180);
		assertEquals(180D, res, 0.000000000);
		
		
	}

}
