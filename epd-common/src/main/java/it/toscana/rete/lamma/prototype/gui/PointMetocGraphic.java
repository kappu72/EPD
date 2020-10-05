package it.toscana.rete.lamma.prototype.gui;

import com.bbn.openmap.omGraphics.OMGraphicList;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.metoc.MetocCurrentGraphic;
import dk.dma.epd.common.prototype.gui.metoc.MetocGraphic;
import dk.dma.epd.common.prototype.gui.metoc.MetocWaveGraphic;
import dk.dma.epd.common.prototype.gui.metoc.MetocWindGraphic;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;
import dk.dma.epd.common.prototype.settings.EnavSettings;
import dk.frv.enav.common.xml.metoc.MetocDataTypes;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import dk.frv.enav.common.xml.metoc.MetocForecastTriplet;

import java.awt.*;

public class PointMetocGraphic extends OMGraphicList {
    private static final long serialVersionUID = 1L;

    private MetocWindGraphic windMarker;
    private MetocCurrentGraphic currentMarker;
    private MetocWaveGraphic waveMarker;
    private MetocForecastPoint metocPoint;
    private MetocGraphic metocGraphic;
    private RouteMetocSettings metocSettings;
    private double lat;
    private double lon;
    EnavSettings eNavSettings;

    public PointMetocGraphic(MetocForecastPoint metocPoint,RouteMetocSettings metocSettings, EnavSettings eNavSettings) {
        this.eNavSettings = eNavSettings;
        this.metocPoint = metocPoint;
        this.metocSettings = metocSettings;
        this.setVague(true);
        double lat = metocPoint.getLat();
        this.lat = lat;
        double lon = metocPoint.getLon();
        this.lon = lon;
        // Get wind speed in m/s
        MetocForecastTriplet windSpeed = metocPoint.getWindSpeed();
        // Wind from direction in degrees clockwise from north
        MetocForecastTriplet windDirection = metocPoint.getWindDirection();
        // Add wind marker
        if(windSpeed != null && windDirection != null){
            double windForecastDirection = windDirection.getForecast();
            double windForecastMs = windSpeed.getForecast();
            double windForecastDirectionRadian = Math.toRadians(windForecastDirection);
            windMarker = new MetocWindGraphic(lat, lon, windForecastDirectionRadian, windForecastMs, metocSettings.getWindWarnLimit());
            add(windMarker);
        }

        // Current speed in m/s
        MetocForecastTriplet currentSpeed = metocPoint.getCurrentSpeed();
        // Current towards direction in degrees from north
        MetocForecastTriplet currentDirection = metocPoint.getCurrentDirection();
        // Add current marker
        if(currentSpeed != null && currentDirection != null){
            double currentForecastMs = currentSpeed.getForecast();
            double currentForecastDirection = currentDirection.getForecast();
            double currentForecastDirectionRadian = Math.toRadians(currentForecastDirection);
            currentMarker = new MetocCurrentGraphic(lat, lon, currentForecastDirectionRadian, currentForecastMs, metocSettings.getCurrentWarnLimit(), eNavSettings);
            add(currentMarker);
        }

        // Mean wave height in meters
        MetocForecastTriplet waveHeight = metocPoint.getMeanWaveHeight();
        // Mean wave from direction in degrees from north
        MetocForecastTriplet waveDirection = metocPoint.getMeanWaveDirection();
        // Add wave marker
        if(waveHeight != null && waveDirection != null){
            double waveForecastDirection = waveDirection.getForecast();
            waveForecastDirection += 180;
            double waveForecastHeight = waveHeight.getForecast();
            double waveForecastDirectionRadian = Math.toRadians(waveForecastDirection);
            waveMarker = new MetocWaveGraphic(lat, lon, waveForecastDirectionRadian, waveForecastHeight, metocSettings.getWaveWarnLimit(), eNavSettings);
            add(waveMarker);
        }
    }

    public MetocForecastPoint getMetocPoint() {
        return metocPoint;
    }

    @Override
    public void render(Graphics gr) {
        Graphics2D image = (Graphics2D) gr;
        image.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        image.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.render(image);
    }

    public MetocGraphic getMetocGraphic() {
        return metocGraphic;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
