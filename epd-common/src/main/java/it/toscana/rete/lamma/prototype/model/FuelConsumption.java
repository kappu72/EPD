 package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;


public class FuelConsumption implements Serializable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ThetaUDimension current_rel = new ThetaUDimension(0, 0);
	UVDimension current_rel_uv = new UVDimension(0, 0);
	ThetaUDimension wind_rel = new ThetaUDimension(0, 0);;
	UVDimension wind_rel_uv = new UVDimension(0, 0);
	double heading = 0;
	double wave_polar = 0;
	double wind_polar = 0;
	double wave_resistance = 0.;
	double wind_resistance = 0.;
	double hull_resistance = 1000.0; //Default value is 1000 [kN] normally it Should be read in hullrestable
	double fuel_rate = 0.;
	double fuel = 0.;
	double weight = 0.;
	MetocPointForecast metoc;
	
	
	public FuelConsumption() {
		super();
	
	}

	public FuelConsumption(ThetaUDimension current_rel, ThetaUDimension wind_rel, double heading, double wave_polar,
			double wind_polar, double wave_resistance, double wind_resistance,
			double fuel_rate, double fuel, MetocPointForecast metoc, double hull_resistance, double weight) {
		super();
		this.heading = heading;
		this.wave_polar = wave_polar;
		this.wind_polar = wind_polar;
		this.wave_resistance = wave_resistance;
		this.wind_resistance = wind_resistance;
		this.fuel_rate = fuel_rate;
		this.fuel = fuel;
		this.metoc = metoc;
		this.hull_resistance = hull_resistance;
		this.weight = weight;
		this.setCurrent_rel(current_rel);
		this.setWind_rel(wind_rel);
	}
	public FuelConsumption clone() {
		return new FuelConsumption(current_rel,
				wind_rel, heading, wave_polar, wind_polar,
				wave_resistance, wind_resistance, fuel_rate, fuel, metoc, hull_resistance, weight);
		
	}
	public ThetaUDimension getCurrent_rel() {
		return current_rel;
	}

	public void setCurrent_rel(ThetaUDimension current_rel) {
		this.current_rel = current_rel;
		this.current_rel_uv = FuelConsumptionCalculator.speedDirToVector(current_rel);
	}

	public ThetaUDimension getWind_rel() {
		return wind_rel;
	}

	public void setWind_rel(ThetaUDimension wind_rel) {
		this.wind_rel = wind_rel;
		this.wind_rel_uv = FuelConsumptionCalculator.speedDirToVector(wind_rel);
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getWave_polar() {
		return wave_polar;
	}

	public void setWave_polar(double wave_polar) {
		this.wave_polar = wave_polar;
	}

	public double getWind_polar() {
		return wind_polar;
	}

	public void setWind_polar(double wind_polar) {
		this.wind_polar = wind_polar;
	}

	public double getWave_resistance() {
		return wave_resistance;
	}

	public void setWave_resistance(double wave_resistance) {
		this.wave_resistance = wave_resistance;
	}

	public double getWind_resistance() {
		return wind_resistance;
	}

	public void setWind_resistance(double wind_resistance) {
		this.wind_resistance = wind_resistance;
	}

	public double getTotalAddedResistance() {
		return wind_resistance + wave_resistance;
	}
	
	public void setFuelRate(double fuel_rate) {
		this.fuel_rate = fuel_rate;
	}
	
	public void setFuel(double fuel) {
		this.fuel = fuel;
	}
	public double getFuelRate() {
		return fuel_rate;
	}
	public double getFuel() {
		 return fuel;
	}
	public void setMetoc(MetocPointForecast metoc) {
		this.metoc = metoc;
	}
	public MetocPointForecast getMetoc() {
		 return metoc;
	}

	public double getHull_resistance() {
		return hull_resistance;
	}

	public void setHull_resistance(double hull_resistance) {
		this.hull_resistance = hull_resistance;
	}

	public double getTotalResistance() {
		return hull_resistance + wind_resistance + wave_resistance;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @return the current_rel_uv
	 */
	public UVDimension getCurrent_rel_uv() {
		return current_rel_uv;
	}

	/**
	 * @param current_rel_uv the current_rel_uv to set
	 */
	public void setCurrent_rel_uv(UVDimension current_rel_uv) {
		this.current_rel_uv = current_rel_uv;
	}

	/**
	 * @return the wind_rel_uv
	 */
	public UVDimension getWind_rel_uv() {
		return wind_rel_uv;
	}

	/**
	 * @param wind_rel_uv the wind_rel_uv to set
	 */
	public void setWind_rel_uv(UVDimension wind_rel_uv) {
		this.wind_rel_uv = wind_rel_uv;
		this.wind_rel = FuelConsumptionCalculator.vectorToSpeedDir(wind_rel_uv);
	}

	public void currentThetaFromUV() {
		this.current_rel = FuelConsumptionCalculator.vectorToSpeedDir(this.current_rel_uv);
	}
	public void windThetaFromUV() {
		this.wind_rel = FuelConsumptionCalculator.vectorToSpeedDir(this.wind_rel_uv);
	}

}
