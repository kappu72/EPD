package it.toscana.rete.lamma.prorotype.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipConditionsSelector;
import it.toscana.rete.lamma.utils.Utils;

public class ShipData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6086419901003648743L;
	private static final Logger LOG = LoggerFactory.getLogger(ShipData.class);
	private String shipName;
	private Path path;
	private List<ShipCondition> shipConditions;
	
	public ShipData(Path path) {
		this(Utils.parseName(path.getFileName().toString()), path);
	}
	
	public ShipData(String shipName, Path path) {
		super();
		this.shipName = shipName;
		this.path = path;
		shipConditions = initShipConditions();
	}

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
	private List<ShipCondition> initShipConditions () {
		List<ShipCondition> conditions = new ArrayList<ShipCondition>();
		try {
			Files.newDirectoryStream(path, p -> p.toFile().isDirectory())
				.forEach(entry -> {
					conditions.add(new ShipCondition(entry));
				});
		}catch (IOException e) {
			LOG.error("Unable to create ships dir");
			e.printStackTrace();
		}
		return conditions;	
	}
	synchronized public ShipCondition addCondition (String name) {
			try {
					Path conditionDir = Paths.get(path.toString(), Utils.cleanConditionName(name));
					Files.createDirectory(conditionDir);
					ShipCondition c = new ShipCondition(conditionDir);
					shipConditions.add(c);
					return c;
					
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
		
	public List<ShipCondition> getShipConditions() {
		return shipConditions;
	}
	
	
	

}
