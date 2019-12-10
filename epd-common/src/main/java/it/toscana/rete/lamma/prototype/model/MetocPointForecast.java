package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import dk.frv.enav.common.xml.metoc.MetocForecastTriplet;
import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;

/**
 * It extends MetocForecastPoint to adapt to fuel consumption computation.
 * Metoc comes from different sources in different unit and convention
 * Internal common convention
 * 
 * Wind kn UV components direction to angle °
 * WindSpeed in m/s dir direction ° from angle
 * Current kn UV components direction to angle °
 * CurrentSpeed in m/s direction to
 * Wave (mean) height m direction from angle ° period s
 * Wave partitions:
 * Wave wind dir h p
 * Wave swell 1 to n [dir h p]
 * 
 * @author kappu
 * 
 */

public class MetocPointForecast extends MetocForecastPoint implements Serializable {

    private static final long serialVersionUID = 1L;
    private UVDimension wind = new UVDimension(0, 0);
    private UVDimension current = new UVDimension(0, 0);
    private Wave meanWave = new Wave();
    private Wave windWave = new Wave();
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
            // Current is in m/s and to
            double ckn = FuelConsumptionCalculator.msTokn(m.getCurrentSpeed().getForecast().doubleValue());
            // double cto = FuelConsumptionCalculator.reverseAngle(m.getCurrentDirection().getForecast().doubleValue());
            this.current = FuelConsumptionCalculator.speedDirToVector(new ThetaUDimension(ckn, m.getCurrentDirection().getForecast().doubleValue()));
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
        if(wind != null) {
            ThetaUDimension wsd = FuelConsumptionCalculator.vectorToSpeedDir(wind);
            double ms = FuelConsumptionCalculator.knToms(wsd.getU());
            double from = FuelConsumptionCalculator.reverseAngle(wsd.getTheta());
            this.setWindDirection(new MetocForecastTriplet(from));
            this.setWindSpeed(new MetocForecastTriplet(ms));
        }
        if(meanWave != null) {
            this.setMeanWaveDirection(new MetocForecastTriplet(meanWave.getDirection()));
            this.setMeanWaveHeight(new MetocForecastTriplet(meanWave.getHeight()));
            this.setMeanWavePeriod(new MetocForecastTriplet(meanWave.getPeriod()));
        }
        if(current != null) {
            ThetaUDimension csd = FuelConsumptionCalculator.vectorToSpeedDir(current);
            this.setCurrentDirection(new MetocForecastTriplet(csd.getTheta()));
            this.setCurrentSpeed(new MetocForecastTriplet(csd.getU()));
        }
    }
    public MetocPointForecast clone() {
        MetocPointForecast t = new MetocPointForecast(wind.clone(), current.clone(), meanWave.clone());
        t.setLat(getLat());
        t.setLon(getLon());
        t.setDensity(getDensity());
        if(getExpires() != null)
            t.setExpires((Date) getExpires().clone());
        t.setSeaLevel(getSeaLevel());
        if(getTime() != null)
            this.setTime((Date) getTime().clone());
        return t;
    }
    /**
     * @return the wind in knt and to dir
     */
    public UVDimension getWind() {
        return wind;
    }

    /**
     * @param wind the wind to set in knt and to dir
     */
    public void setWind(UVDimension wind) {
        this.wind = wind;
        if(wind != null) {
            ThetaUDimension wsd = FuelConsumptionCalculator.vectorToSpeedDir(wind);
            double ms = FuelConsumptionCalculator.knToms(wsd.getU());
            double from = FuelConsumptionCalculator.reverseAngle(wsd.getTheta());
            this.setWindDirection(new MetocForecastTriplet(from));
            this.setWindSpeed(new MetocForecastTriplet(ms));
        }
        
    }

    /**
     * @return the current in knt and to dir
     */
    public UVDimension getCurrent() {
        return current;
    }

    /**
     * @param current the current to set in knt and to dir
     */
    public void setCurrent(UVDimension current) {
        this.current = current;
        if(current != null) {
            ThetaUDimension csd = FuelConsumptionCalculator.vectorToSpeedDir(current);
            double ms = FuelConsumptionCalculator.knToms(csd.getU());
            this.setCurrentDirection(new MetocForecastTriplet(csd.getTheta()));
            this.setCurrentSpeed(new MetocForecastTriplet(ms));
        }
    }

    /**
     * @return the meanWave
     */
    public Wave getMeanWave() {
        return meanWave;
    }

    /**
     * @param meanWave the meanWave to set height in m period in sec and from dir
     */
    public void setMeanWave(Wave meanWave) {
        this.meanWave = meanWave;
        if(meanWave != null) {
            this.setMeanWaveDirection(new MetocForecastTriplet(meanWave.getDirection()));
            this.setMeanWaveHeight(new MetocForecastTriplet(meanWave.getHeight()));
            this.setMeanWavePeriod(new MetocForecastTriplet(meanWave.getPeriod()));
        }
    }
    /**
     * set wave object from values in metocforecastpoint super class
     */
    public void setMeanWaveFromTriplet() {
        this.meanWave = new Wave(this.getMeanWaveHeight().getForecast(), this.getMeanWaveDirection().getForecast(),this.getMeanWavePeriod().getForecast());

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