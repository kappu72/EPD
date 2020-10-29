package it.toscana.rete.lamma.prototype.metocservices;

import java.util.HashMap;
import java.util.Map;
// Request URL: https://geoportale.lamma.rete.toscana.it/geoserver/WW3_MEDIT_RUN00/ows?SERVICE=WMS&EXCEPTIONS=application%2Fvnd.ogc.se_xml&TRANSPARENT=TRUE&VERSION=1.3.0&REQUEST=GetLegendGraphic&ELEVATION=0.0&CRS=EPSG%3A900913&LAYER=ww3_medit_Mean_period_of_wind_waves_surface_20200623T000000000Z&STYLE=&LEGEND_OPTIONS=forceLabels%3Aon%3BfontSize%3A10&WIDTH=12&HEIGHT=12&FORMAT=image%2Fgif&SCALE=17471284.63743896
public enum WMSMetocLayers {
    MEAN_WAVE_HEIGHT ("swh", true),
    MEAN_WAVE_PERIOD("mwp", true),
    MEAN_WAVE_DIR ("mwd", false),
    WIND_SPEED_DIR( "wind", false),
    WIND_SPEED( "wind_speed", true),
    WIND_DIR( "wind_dir", false),
    WIND_GUST("fg10", true),
    CURRENT("current", false),
    CURRENT_SPEED("current_speed", true),
    CURRENT_DIR("current_dir", false);


    private final String name;
    private final boolean interpolate;

    private static final Map<String, WMSMetocLayers> lookup = new HashMap<String, WMSMetocLayers>();

    static {
        for (WMSMetocLayers d : WMSMetocLayers.values()) {
            lookup.put(d.layerName(), d);
        }
    }


    WMSMetocLayers(String name, boolean interpolate) {
            this.name = name;
            this.interpolate = interpolate;
    }
    public String layerName() {
        return name;
    }
    public boolean isInterpolate() {
        return interpolate;
    }
    public static WMSMetocLayers get(String layerName) {
        if(layerName.contains(":")) {
            return lookup.get(layerName.split(":")[1]);
        }
        return lookup.get(layerName);
    }


}