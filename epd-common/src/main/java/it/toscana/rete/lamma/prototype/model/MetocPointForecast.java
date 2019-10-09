package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;
import java.util.List;

import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;
/**
 * It extends MetocForecastPoint to adapt to fuelconsumption caluclation
 * Metoc comes from different sources in different unit and convention
 * Internal common convention
 * 
 * Wind kn UV components direction to angle °
 * Currnet kn UV components direction to angle °
 * Wave (mean) heigth m direction from angle ° period s
 * Wave partiotions:
 * Wave wind dir h p
 * Wave swell 1 to n [dir h p]
 * 
 * @author kappu
 *
 * 
 */



public class MetocPointForecast extends MetocForecastPoint implements Serializable {

    private static final long serialVersionUID = 1L;
    private UVDimension wind;
    private UVDimension current;
    private Wave meanWave;
    private Wave windWave;
    private List<Wave> swellWave;

    /**
     * 
     */

    public MetocPointForecast() {
        super();
    }
    // Used to transform metoc from epd (dmi provider)
    // current in kn and to
    // wind ms and from
    // wave from
    public MetocPointForecast(MetocForecastPoint m ) {
        super();
        if(m.getCurrentSpeed() != null && m.getCurrentSpeed() != null){
            this.setCurrentDirection(m.getCurrentDirection());
            this.setCurrentSpeed(m.getCurrentSpeed());
            // Current is in kn and to
            this.current = FuelConsumptionCalculator.speedDirToVector(new ThetaUDimension(m.getCurrentSpeed().getForecast().doubleValue(), m.getCurrentDirection().getForecast().doubleValue()));
        }
        if(m.getWindDirection() != null && m.getWindSpeed()!= null) {
            this.setWindDirection(m.getWindDirection());
            this.setWindSpeed(m.getWindSpeed());
            // Wind is in m/s and from
            double kn = FuelConsumptionCalculator.msTokn(m.getWindSpeed().getForecast().doubleValue());
            double to = FuelConsumptionCalculator.reverseAngle(m.getWindDirection().getForecast().doubleValue());
            this.wind = FuelConsumptionCalculator.speedDirToVector(new ThetaUDimension(kn, to));
        }
        // mean wave
        if(m.getMeanWaveDirection() != null && m.getMeanWaveHeight() != null && m.getMeanWavePeriod() != null) {
            this.setMeanWaveDirection(m.getMeanWaveDirection());
            this.setMeanWaveHeight(m.getMeanWaveHeight());
            this.setMeanWavePeriod(m.getMeanWavePeriod());
            this.meanWave = new Wave(m.getMeanWaveHeight().getForecast().doubleValue(), m.getMeanWaveDirection().getForecast().doubleValue(), m.getMeanWavePeriod().getForecast().doubleValue());
        }else{
            // Initialize empty
            this.meanWave = new Wave(); // Potresti dover fare la medesima cosa per correnti e vento se vuoi calcolare senza considerarli o se hanno buchi nei dati!!
        }
        
        this.setLat(m.getLat());
        this.setLon(m.getLon());
        this.setDensity(m.getDensity());
        this.setExpires(m.getExpires());
        this.setSeaLevel(m.getSeaLevel());
        this.setTime(m.getTime());
    }

    /**
     * @param wind
     * @param current
     * @param meanWave
     */

    public MetocPointForecast(UVDimension wind, UVDimension current, Wave meanWave) {
        this.wind = wind;
        this.current = current;
        this.meanWave = meanWave;
    }

    /**
     * @return the wind
     */
    public UVDimension getWind() {
        return wind;
    }

    /**
     * @param wind the wind to set
     */
    public void setWind(UVDimension wind) {
        this.wind = wind;
    }

    /**
     * @return the current
     */
    public UVDimension getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(UVDimension current) {
        this.current = current;
    }

    /**
     * @return the meanWave
     */
    public Wave getMeanWave() {
        return meanWave;
    }

    /**
     * @param meanWave the meanWave to set
     */
    public void setMeanWave(Wave meanWave) {
        this.meanWave = meanWave;
    }

    /**
     * @return the windWave
     */
    public Wave getWindWave() {
        return windWave;
    }

    /**
     * @param windWave the windWave to set
     */
    public void setWindWave(Wave windWave) {
        this.windWave = windWave;
    }

    /**
     * @return the swellWave
     */
    public List<Wave> getSwellWave() {
        return swellWave;
    }

    /**
     * @param swellWave the swellWave to set
     */
    public void setSwellWave(List<Wave> swellWave) {
        this.swellWave = swellWave;
    }
    



    
}