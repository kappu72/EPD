package it.toscana.rete.lamma.utils;

import com.bbn.openmap.proj.RhumbCalculator;

import dk.dma.epd.common.prototype.model.route.RouteLeg;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import it.toscana.rete.lamma.prototype.model.ThetaUDimension;
import it.toscana.rete.lamma.prototype.model.UVDimension;
import it.toscana.rete.lamma.prototype.model.tables.WaveresGenericTable;
import it.toscana.rete.lamma.prototype.model.tables.WindresTable;

/**
 *  Main utility class, contains all formulas and methods to
 *  forecast fuel consumption, all formulas have been provided
 *  by Andrea Orlandi.
 * @author kappu 
 * 		
 */
public class FuelConsumptionCalculator {
	public static double rhoAir = 1.225;
	/**
	 * Calculate the angle of b respect to a, given a and b thetas.
	 * 
	 * @param a degree
	 * @param b degree
	 * @return degree
	 */

	public static double rilevamentoPolare(double a, double b) {
		double theta = b - a;
		if (theta >= 180.0)
			theta = theta - 360.0;
		else if (theta <= -180.0)
			theta = 360.0 + theta;
		return theta;
	}

	/**
	 * Convert from  ThetaUDimension to UVDimension form
	 * 
	 * @param ThetaUDimension 
	 * @return UVDimension
	 */
	public static UVDimension speedDirToVector(ThetaUDimension v) {
		
		double UU = v.getU() * Math.sin(Math.toRadians(v.getTheta()));
		double VV = v.getU() * Math.cos(Math.toRadians(v.getTheta()));
		return new UVDimension(UU, VV);
	}

	/**
	 * Convert from UVDimension to ThetaUDimension form
	 * 
	 * @param UVDimension
	 * @return ThetaUDimension
	 */
	public static ThetaUDimension vectorToSpeedDir(UVDimension v) {

		double U = Math.sqrt(square(v.getU()) + square(v.getV()));
		double theta = Math.toDegrees(Math.atan2(-v.getU(), -v.getV()));
		if (theta < 0)
			theta = theta + 360;
		
		return new ThetaUDimension(U, theta);
	}

	/**
	 * Calculate currentKinematical
	 * @param param UVDimension
	 * @param sog UVDimension
	 * @return ThetaUDimension
	 */
	public static ThetaUDimension kinematical(UVDimension param, UVDimension sog) {

		double uu = param.getU() - sog.getU();
		double vv = param.getV() - sog.getV();

		double theta = Math.toDegrees(Math.atan2( -(uu * sog.getV() - sog.getU() *vv), -(uu * sog.getU() + vv * sog.getV())));
		double u = Math.sqrt(square(uu) + square(vv));
		
		return new ThetaUDimension(u, theta);
		
	}
	/**
	 * Calculate Kinematical
	 * @param curr or wind UVDimension
	 * @param sog ThetaUDimension
	 * @return ThetaUDimension
	 */
	public static ThetaUDimension kinematical( UVDimension param, ThetaUDimension sog, boolean useUV) {
		return useUV ? kinematical(param, speedDirToVector(sog)) : kinematical(vectorToSpeedDir(param), sog);
	}
	
	/**
	 * Calculate Kinematical
	 * @param curr or wind ThetaUDimension
	 * @param sog ThetaUDimension
	 * @return ThetaUDimension
	 */
	public static ThetaUDimension kinematical( ThetaUDimension param, ThetaUDimension sog) {
		if(param.getU() == 0.0) {
			return new ThetaUDimension(sog.getU(), 0D);
		}
		double Theta = rilevamentoPolare(sog.getTheta(), param.getTheta());
		double Theta_Rad = Math.toRadians(Theta);
		
		double sin = Math.sin(Theta_Rad);
		double cos = Math.cos(Theta_Rad);
		
		double theta=Math.toDegrees(Math.atan2(param.getU() * sin , sog.getU() + param.getU() * cos));
		double u= Math.sqrt(square(sog.getU() + param.getU() * cos) + square(param.getU() * sin));
		
		return new ThetaUDimension(u, theta);

	}
	
	/**
	 * 
	 * @param sog
	 * @param cur
	 * @param wind
	 * @param waveDir
	 * @param waveH
	 * @param waveTm
	 * @param cxTables
	 * @param cawTables
	 * @param aT sezione nave
	 * @param useUV 
	 * @return
	 */
	public static FuelConsumption CalculateResistance(ThetaUDimension sog, UVDimension cur, UVDimension wind, double waveDir, double waveH, double waveTm , WindresTable cxTables, WaveresGenericTable cawTables, int aT, boolean useUV) {
		
		FuelConsumption c = CalculateAllKinematical(sog, cur, wind, waveDir, useUV);
		double rAw = square(waveH) * cawTables.getCawValue((float) c.getCurrent_rel().getU(), (float) waveTm, (float)c.getWave_polar());
		double rWind = (0.5 * rhoAir * aT * square(c.getWind_rel().getU()) * cxTables.getCx((float)c.getWind_polar())) / 1000;

		c.setWind_resistance(rWind);
		c.setWave_resistance(rAw);
		c.setTotal_resistance(rWind+ rAw);
		
		return c;
	}
	
	public static FuelConsumption CalculateAllKinematical(ThetaUDimension sog, UVDimension cur, UVDimension wind, double wave, boolean useUV) {
		
		FuelConsumption c = new FuelConsumption();
		ThetaUDimension cur_rel = kinematical(cur, sog, useUV);
		// current kinematical
		c.setCurrent_rel(cur_rel);
		
		double heading = wrapTo360(sog.getTheta()+cur_rel.getTheta());
		c.setHeading(heading);
		
		c.setWave_polar(rilevamentoPolare(heading, wave));

		// wind kinematical
		ThetaUDimension wind_rel = kinematical(wind, sog, useUV);
		c.setWind_rel(wind_rel);
		
		c.setWind_polar(rilevamentoPolare(cur_rel.getTheta(), wind_rel.getTheta()));
		return c;
		
	}
	
//public static FuelConsumption CalculateAllResistences(FuelConsumption kinematical ) {
//		
//		FuelConsumption c = new FuelConsumption();
//		ThetaUDimension cur_rel = kinematical(cur, sog, useUV);
//		// current kinematical
//		c.setCurrent_rel(cur_rel);
//		
//		double heading = wrapTo360(sog.getTheta()+cur_rel.getTheta());
//		c.setHeading(heading);
//		
//		c.setWave_polar(rilevamentoPolare(heading, wave));
//
//		// wind kinematical
//		ThetaUDimension wind_rel = kinematical(wind, sog, useUV);
//		c.setWind_rel(wind_rel);
//		
//		c.setWind_polar(rilevamentoPolare(cur_rel.getTheta(), wind_rel.getTheta()));
//		return c;
//		
//	}
//	
	
	
	public static double square(double val) {
		return Math.pow(val, 2.0);
	}
	
	public static double wrapTo360(double angle) {
		return (angle %= 360) >= 0 ? angle : (angle + 360);
	}
	public static double wrapTo180(double angle) {
		return (angle %= 180) >= 0 ? angle : (angle + 180);
	}
}
