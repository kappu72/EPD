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
	
	public FuelConsumption() {
		super();
	
	}

	public FuelConsumption(ThetaUDimension current_rel, ThetaUDimension wind_rel, double heading, double wave_polar,
			double wind_polar, double wave_resistance, double wind_resistance, double total_resistance) {
		super();
		this.current_rel = current_rel;
		this.wind_rel = wind_rel;
		this.heading = heading;
		this.wave_polar = wave_polar;
		this.wind_polar = wind_polar;
		this.wave_resistance = wave_resistance;
		this.wind_resistance = wind_resistance;
		this.total_added_resistance = total_resistance;
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

	public double getTotal_resistance() {
		return total_added_resistance;
	}

	public void setTotal_resistance(double total_resistance) {
		this.total_added_resistance = total_resistance;
	}
	
	
}
