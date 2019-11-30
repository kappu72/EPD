package it.toscana.rete.lamma.prototype.metocservices;

import java.util.HashMap;
import java.util.Map;

public enum MetocProviders {
    DMI ("dmi"),
    FCO ("fco"),
    LOCAL("Local metoc"),
    LAMMA("Lamma (opendap)");

    private final String label;
    
    private static final Map<String, MetocProviders> lookup = new HashMap<String, MetocProviders>();

    static {
        for (MetocProviders d : MetocProviders.values()) {
            lookup.put(d.label(), d);
        }
    }


    MetocProviders(String label) {
            this.label = label;
    }
    public String label () {
        return label;
    }

    public static MetocProviders get(String label) {
        return lookup.get(label);
    }


}