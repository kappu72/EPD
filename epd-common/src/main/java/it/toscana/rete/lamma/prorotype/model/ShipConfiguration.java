package it.toscana.rete.lamma.prorotype.model;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import it.toscana.rete.lamma.utils.Utils;

/**
 * @author kappu
 *
 */
public class ShipConfiguration implements Serializable {

	/**
	 * The minimal valid configuration should have: windres file waveres file and
	 * one propulsion_configuration Wave configuration lookup tables swell and active
	 * see waves are optional as more then one propulsion setting
	 */

	private static final long serialVersionUID = 1L;
	private Path path;
	private String name;
	private Path windres = null;
	private Path waveres = null;
	private Path waveres_sw = null;
	private Path waveres_se = null;
	private List<Path> propulsions = new ArrayList<Path>();

	public ShipConfiguration() {
		super();
	}

	public ShipConfiguration(Path path) {
		super();
		this.path = path;
		this.name = path.getFileName().toString();
		initFiles();
	}

	public Path getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public List<Path> getPropulsions() {
		return propulsions;
	}

	public Path getWindres() {
		return windres;
	}

	public void setWindres(Path windres) {
		this.windres = windres;
	}

	public Path getWaveres() {
		return waveres;
	}

	public void setWaveres(Path waveres) {
		this.waveres = waveres;
	}

	public Path getWaveres_sw() {
		return waveres_sw;
	}

	public void setWaveres_sw(Path waveres_sw) {
		this.waveres_sw = waveres_sw;
	}

	public Path getWaveres_se() {
		return waveres_se;
	}

	public void setWaveres_se(Path waveres_se) {
		this.waveres_se = waveres_se;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPropulsions(List<Path> propulsions) {
		this.propulsions = propulsions;
	}

	// Import in local directory and configure the name
	public String importWindres(File file) throws IOException {
		Path newWindres = importFile(file, FilesFilters.WINDRES);
		if (windres != null) {
			Files.delete(windres);
		}
		windres = newWindres;
		return Utils.stripFileExt(file);
	}

	public String importWaveres(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES);
		if (waveres != null) {
			Files.delete(waveres);
		}
		windres = newPath;
		return Utils.stripFileExt(file);
	}

	public String importWaveresSw(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES_SW);
		if (waveres_sw != null) {
			Files.delete(waveres_sw);
		}
		windres = newPath;
		return Utils.stripFileExt(file);
	}

	public String importWaveresSe(File file) throws IOException {
		Path newPath = importFile(file, FilesFilters.WAVERES_SE);
		if (waveres_se != null) {
			Files.delete(waveres_se);
		}
		windres = newPath;
		return Utils.stripFileExt(file);
	}

	public String importPropulsion(File file, String propName) throws IOException {
		String cleanedName = Utils.cleanConditionName(propName);
		Path dest = Paths.get(path.toString(), cleanedName + "." + FilesFilters.PROPULSION);
		Files.copy(file.toPath(), dest);
		if (searchPropulsion(dest) == null) {
			propulsions.add(dest);
			return cleanedName;
		}
		return null;
	}

	public String getWindresName() {
		if (windres == null)
			return null;
		return Utils.stripFileExt(windres.getFileName().toFile());
	}

	public String getWaveresName() {
		if (waveres == null)
			return null;
		return Utils.stripFileExt(waveres.getFileName().toFile());
	}

	public String getWaveresSwName() {
		if (waveres_sw == null)
			return null;
		return Utils.stripFileExt(waveres_sw.getFileName().toFile());
	}

	public String getWaveresSeName() {
		if (waveres_se == null)
			return null;
		return Utils.stripFileExt(waveres_se.getFileName().toFile());
	}

	private Path importFile(File f, String ext) throws IOException {
		String name = changeFileExt(f, ext);
		Path dest = Paths.get(path.toString(), name);
		Files.copy(f.toPath(), dest);
		return dest;
	}

	private String changeFileExt(File f, String ext) {
		String name = Utils.stripFileExt(f);
		if (ext.indexOf(".") == -1)
			ext = "." + ext;
		return name + ext;
	}

	private void initFiles() {

		File[] f = path.toFile().listFiles(FilesFilters.windres);
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
		f = path.toFile().listFiles(FilesFilters.propulsion);
		for (File ff : f) {
			propulsions.add(ff.toPath());
		}

	}

	private Path searchPropulsion(Path p) {
		return propulsions.stream().filter(prop -> prop.equals(p)).findAny().orElse(null);
	}

	public boolean isValid() {
		return windres != null && (waveres != null || hasAdvancedWave()) && propulsions.size() > 0;

	}

	// Wave components configuration splits sea in swell and active wind components.
	public boolean hasAdvancedWave() {
		return waveres_se != null && waveres_sw != null;
	}

	
	/**
	 * They are equal if they have same path
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof ShipConfiguration) {
			return this.path.equals(((ShipConfiguration) obj).getPath());
		}
		return false;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		name = aInputStream.readUTF();
		path = Paths.get(aInputStream.readUTF());
		propulsions = new ArrayList<Path>();
		initFiles();
	}
	
	
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException
    {
		aOutputStream.writeUTF(name);
		aOutputStream.writeUTF(path.toString());
    }
}
