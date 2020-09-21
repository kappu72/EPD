package it.toscana.rete.lamma.prototype.model;

import org.geotools.ows.wms.xml.Dimension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LammaMetocWMSConfig {
    private Date currentTime;
    private String run;
    private List<String> layers = new ArrayList<>();
    private Boolean show = true;
    private Boolean legend = false;

    public LammaMetocWMSConfig() {
        super();
    }

    public LammaMetocWMSConfig(Date currentTime, String run, List<String> layers) {
        this();
        this.currentTime = currentTime;
        this.run = run;
        this.layers = layers;
    }
    public LammaMetocWMSConfig(Date currentTime, String run, List<String> layers, boolean show, boolean legend) {
        this(currentTime, run, layers);
        this.show = show;
        this.legend = legend;
    }
    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public Boolean getLegend() {
        return legend;
    }

    public void setLegend(Boolean legend) {
        this.legend = legend;
    }

    public boolean isValid() {
        return run != null && currentTime != null && layers != null && layers.size() > 0;
    }
    @Override
    public boolean equals(Object obj) {
        LammaMetocWMSConfig c = (LammaMetocWMSConfig) obj;
        return super.equals(obj) &&
                (layers != null && layers.equals(c.getLayers()) || c.getLayers() == null) &&
                (run == c.getRun()) &&
                (currentTime != null && currentTime.equals(c.getCurrentTime()) || c.getCurrentTime() == null)
                && show == c.getShow()
                && legend == c.getLegend();
    }
}
