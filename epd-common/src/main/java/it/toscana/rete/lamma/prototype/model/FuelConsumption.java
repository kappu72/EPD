package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;


public class FuelConsumption implements Serializable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ThetaUDimension current_rel;
	ThetaUDimension wind_rel;
	double heading;
	double wave_polar;
	double wind_polar;
	double wave_resistance;
	double wind_resistance;
	double total_added_resistance;
	double hull_resistance = 1000.0; //Default value is 1000 [kN] normally it Should be read in hullrestable
	double fuel_rate;
	double fuel;
	MetocPointForecast metoc;
	
	
	public FuelConsumption() {
		super();
	
	}

	public FuelConsumption(ThetaUDimension current_rel, ThetaUDimension wind_rel, double heading, double wave_polar,
			double wind_polar, double wave_resistance, double wind_resistance, double total_added_resistance,
			double fuel_rate, double fuel, MetocPointForecast metoc, double hull_resistance) {
		super();
		this.current_rel = current_rel;
		this.wind_rel = wind_rel;
		this.heading = heading;
		this.wave_polar = wave_polar;
		this.wind_polar = wind_polar;
		this.wave_resistance = wave_resistance;
		this.wind_resistance = wind_resistance;
		this.total_added_resistance = total_added_resistance;
		this.fuel_rate = fuel_rate;
		this.fuel = fuel;
		this.metoc = metoc;
		this.hull_resistance = hull_resistance;

	}
	public FuelConsumption clone() {
		return new FuelConsumption(current_rel,
				wind_rel, heading, wave_polar, wind_polar,
				wave_resistance, wind_resistance, total_added_resistance, fuel_rate, fuel, metoc, hull_resistance);
		
	}
	public ThetaUDimension getCurrent_rel() {
		return current_rel;
	}

	public void setCurrent_rel(ThetaUDimension current_rel) {
		this.current_rel = current_rel;
	}

	public ThetaUDimension getWind_rel() {
		return wind_rel;
	}

	public void setWind_rel(ThetaUDimension wind_rel) {
		this.wind_rel = wind_rel;
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
		return total_added_resistance;
	}

	public void setTotalresistance(double total_resistance) {
		this.total_added_resistance = total_resistance;
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

}
