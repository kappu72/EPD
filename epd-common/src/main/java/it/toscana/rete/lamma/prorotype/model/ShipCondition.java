package it.toscana.rete.lamma.prorotype.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.util.FileUtils;

import it.toscana.rete.lamma.utils.Utils;

public class ShipCondition implements Serializable {
	
	/**
	 * The minimal valid configuration should have:
	 * windres file
	 * waveres_gen file
	 * and one propulsor_configuration
	 * Wave configuration lookup tables swell and active see waves are optional as more then one propulsor setting 
	 */
	private static final long serialVersionUID = -2753028844721932538L;
	private Path path;
	private String name;
	private Path windres = null;
	private Path waveres = null;
	private Path waveres_sw = null;
	private Path waveres_se = null;
	private List<Path> propulsors = new ArrayList<Path>();
	
	
	

	public ShipCondition(Path path) {
		super();
		this.path= path;
		this.name = path.getFileName().toString();
		initFiles();
	}

	public Path getPath() {
		return path;
	}

	public String getName() {
		return name;
	}
	public List<Path> getPropulsors() {
		return propulsors;
	}
	// Import in local dir and configure the name
	public String setWindres(File file) throws IOException {
		Path newWindres = importFile(file, FilesFilters.WINDRES);
		if(windres != null) {
			Files.delete(windres);
		}
		windres = newWindres;
		return Utils.stripFileExt(file);			
	}
	public String setWaveres(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES);
		if(waveres != null) {
			Files.delete(waveres);
		}
		windres = newPath;
		return Utils.stripFileExt(file);			
	}
	public String setWaveresSw(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES_SW);
		if(waveres_sw != null) {
			Files.delete(waveres_sw);
		}
		windres = newPath;
		return Utils.stripFileExt(file);			
	}
	public String setWaveresSe(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES_SE);
		if(waveres_se != null) {
			Files.delete(waveres_se);
		}
		windres = newPath;
		return Utils.stripFileExt(file);			
	}
	public String setPropulsor(File file, String propName) throws IOException {
		String cleanedName = Utils.cleanConditionName(propName);
		Path dest = Paths.get(path.toString(), cleanedName + "." + FilesFilters.PROPULSOR);
		Files.copy(file.toPath(), dest);
		if(searchPropulsor(dest) == null) {
			propulsors.add(dest);
			return cleanedName;
		}
		return null;			
	}
	public String getWindresName () {
		if(windres == null)
			return null;
		return Utils.stripFileExt(windres.getFileName().toFile());
	}
	public String getWaveresName () {
		if(waveres == null)
			return null;
		return Utils.stripFileExt(waveres.getFileName().toFile());
	}
	public String getWaveresSwName () {
		if(waveres_sw == null)
			return null;
		return Utils.stripFileExt(waveres_sw.getFileName().toFile());
	}
	public String getWaveresSeName () {
		if(waveres_se == null)
			return null;
		return Utils.stripFileExt(waveres_se.getFileName().toFile());
	}
	
	private Path importFile(File f, String ext) throws IOException{
		String name = changeFileExt(f, ext);
		Path dest = Paths.get(path.toString(), name);		
		Files.copy(f.toPath(), dest);
		return dest;
	}
	
	
	private String changeFileExt (File f, String ext) {
		String name = Utils.stripFileExt(f);
		if(ext.indexOf(".") == -1) ext = "." + ext;
		return name + ext;
	}
	private void initFiles () {
		 
		File [] f = path.toFile().listFiles(FilesFilters.windres);		
		if (f.length > 0) {
			windres = f[0].toPath();
		}
		f = path.toFile().listFiles(FilesFilters.waveres);	
		if (f.length > 0) {
			waveres = f[0].toPath();
		}
		f = path.toFile().listFiles(FilesFilters.waveres_se);	
		if (f.length > 0) {
			waveres_se = f[0].toPath();
		}
		f = path.toFile().listFiles(FilesFilters.waveres_sw);	
		if (f.length > 0) {
			waveres_sw = f[0].toPath();
		}
		f = path.toFile().listFiles(FilesFilters.propulsor);	
		for(File ff :f) {
			propulsors.add(ff.toPath());
		}
		
		// TODO Add more files
		
	}
	private Path searchPropulsor(Path p) {
		return propulsors.stream()
				  .filter(prop -> prop.equals(p))
				  .findAny()
				  .orElse(null);
	}
}
