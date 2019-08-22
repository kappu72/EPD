package it.toscana.rete.lamma.prorotype.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipConfigurationsSelector;
import it.toscana.rete.lamma.utils.Utils;

/**
 * @author kappu
 *
 */
public class ShipData {

	private static final Logger LOG = LoggerFactory.getLogger(ShipData.class);
	private String shipName;
	private Path path;
	private List<ShipConfiguration> shipConfigurations;

	public ShipData() {
		super();
	}

	public ShipData(Path path) {
		this(Utils.parseName(path.getFileName().toString()), path);
	}

	public ShipData(String shipName, Path path) {
		super();
		this.shipName = shipName;
		this.path = path;
		shipConfigurations = initShipConfigurations();
	}
	// getters and setters
	public String getShipName() {
		return shipName;
	}

	public void setShipName(String shipName) {
		this.shipName = shipName;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
	
	public List<ShipConfiguration> getShipConfigurations() {
		return shipConfigurations;
	}

	public void setShipConfigurations(List<ShipConfiguration> shipConfigurations) {
		this.shipConfigurations = shipConfigurations;
	}
	
	/**
	 * Scan ShipData directory searching sail configuration directory
	 * @return
	 */
	private List<ShipConfiguration> initShipConfigurations() {
		List<ShipConfiguration> conditions = new ArrayList<ShipConfiguration>();
		try {
			Files.newDirectoryStream(path, p -> p.toFile().isDirectory()).forEach(entry -> {
				conditions.add(new ShipConfiguration(entry));
			});
		} catch (IOException e) {
			LOG.error("Unable to create ships dir");
			e.printStackTrace();
		}
		return conditions;
	}
	/**
	 * Create a new configuration directory in ShipData directory if
	 * it doesn't exist.
	 * @param name
	 * @return
	 */
	public ShipConfiguration addConfiguration(String name) {
		try {
			Path conditionDir = Paths.get(path.toString(), Utils.cleanConditionName(name));
			Files.createDirectory(conditionDir);
			ShipConfiguration c = new ShipConfiguration(conditionDir);
			shipConfigurations.add(c);
			return c;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	

	/**
	 * Filter ShipConfigurations removing invalid.
	 * @return a List of valid ShipConfiguration directories
	 */
	public List<ShipConfiguration> getValidShipConfigurations() {
		return shipConfigurations.stream().filter(config -> config.isValid()).collect(Collectors.toList());

	}

	public ShipConfiguration serachConfigurationByName(String configName) {
		return shipConfigurations.stream().filter(config -> config.getName().equals(configName)).findAny().orElse(null);

	}

	/**
	 * @return true if ship data has at least one valid shipCondition
	 */
	public Boolean isShipValid() {
		return getValidShipConfigurations().size() > 0;
	}

}
