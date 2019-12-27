package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;
/**
 * It models the configurations needed to calculate fuel consumption
 * @author kappu
 *
 */
public class RouteFuelConsumptionSettings implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ship;
	private ShipConfiguration configuration;
	private boolean waveComponents = false;
	private boolean noWind = false; // Skip metoc and apparent wind
	private boolean skipWind = false; // Skip metoc wind
	private boolean skipWave = false; // Skip metoc wave 
	private boolean skipCurrent = false; // Skip metoc current

	public RouteFuelConsumptionSettings() {

	}

	public String getShip() {
		return ship;
	}

	public void setShip(String ship) {
		this.ship = ship;
	}

	public ShipConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ShipConfiguration configuration) {
		this.configuration = configuration;
	}

	public boolean isWaveComponents() {
		return waveComponents;
	}

	public void setWaveComponents(boolean waveComponents) {
		this.waveComponents = waveComponents;
	}

	public boolean isNoWind() {
		return noWind;
	}

	public void setNoWind(boolean noWind) {
		this.noWind = noWind;
	}

	public boolean isSkipWind() {
		return skipWind;
	}

	public void setSkipWind(boolean skipWind) {
		this.skipWind = skipWind;
	}

	public boolean isSkipWave() {
		return skipWave;
	}

	public void setSkipWave(boolean skipWave) {
		this.skipWave = skipWave;
	}

	public boolean isSkipCurrent() {
		return skipCurrent;
	}

	public void setSkipCurrent(boolean skipCurrent) {
		this.skipCurrent = skipCurrent;
	}

}
