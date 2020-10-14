package it.toscana.rete.lamma.prototype.metocservices;

import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;

import java.io.File;

public class PointMetocProvider {
    private String name;
    private RouteMetocSettings settings;

    public PointMetocProvider(RouteMetocSettings settings) {
        this.settings = settings;
        if(settings.getProvider().equals(MetocProviders.LAMMA.label())) {
            this.name = settings.getLammaMetocFile();
        }else if(settings.getProvider().equals(MetocProviders.LOCAL.label())) {
            this.name = new File(settings.getLocalMetocFile()).getName();
        }
    }

    public String getFile() {
        if(settings.getProvider().equals(MetocProviders.LAMMA.label())) {
            return settings.getLammaMetocFile();
        }else if(settings.getProvider().equals(MetocProviders.LOCAL.label())) {
            return settings.getLocalMetocFile();
        };
        return null;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return settings.getProvider();
    }

    public RouteMetocSettings getSettings() {
        return settings;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
