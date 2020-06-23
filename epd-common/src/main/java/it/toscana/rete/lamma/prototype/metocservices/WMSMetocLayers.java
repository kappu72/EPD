package it.toscana.rete.lamma.prototype.metocservices;

import java.util.HashMap;
import java.util.Map;

public enum WMSMetocLayers {
    MEAN_WAVE_HEIGHT ("WWW3_MEDIT:ww3_medit_Sig_height_of_wind_waves_and_swell_surface", true),
    MEAN_WAVE_DIR ("WWW3_MEDIT:ww3_medit_Direction_of_wind_waves_surface", false),
    MEAN_WAVE_PERIOD("WWW3_MEDIT:ww3_medit_Mean_period_of_wind_waves_surface", true);


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
        return lookup.get(layerName);
    }


}