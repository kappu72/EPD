package it.toscana.rete.lamma.prorotype.model;

import java.io.File;
import java.io.FilenameFilter;

public class FilesFilters {
	static final String WINDRES = "wires";
	static final String WAVERES = "wares";
	static final String WAVERES_SW = "wares_sw";
	static final String WAVERES_SE = "wares_se";
	static final String PROPULSOR = "ps";
	
	public static FilenameFilter windres = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith("." + WINDRES);
	    }
	    };
    public static FilenameFilter waveres = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith("." + WAVERES);
	    }
	    };
    public static FilenameFilter waveres_sw = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith("." + WAVERES_SW);
	    }
	    };
    public static FilenameFilter waveres_se = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith("." + WAVERES_SE);
	    }
	    };
    public static FilenameFilter propulsor = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith("." + PROPULSOR);
	    }
	    };
}
