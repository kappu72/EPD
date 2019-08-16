package it.toscana.rete.lamma.gui;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.ais.VesselStaticData;
import dk.dma.epd.common.prototype.ais.VesselTarget;
import dk.dma.epd.ship.EPDShip;

import dk.dma.epd.ship.ownship.OwnShipHandler;
import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipsDataDialog;

public class ShipDataDialog extends ShipsDataDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3432586010125593170L;
	private static final Logger LOG = LoggerFactory.getLogger(ShipsDataDialog.class);	
	
	private OwnShipHandler ownShipHandler;
	public ShipDataDialog(JFrame parent) {
		super(parent);
		ownShipHandler = EPDShip.getInstance().getOwnShipHandler();
		if(ownShipHandler != null) {
			VesselStaticData data = ownShipHandler.getStaticData();
			String name = "N/A";
			if(data != null ) {
				name = data.getName().trim();
			}
			String mmsi = "(" + ownShipHandler.getMmsi() + ")";
			String shipName = name + " " + mmsi;
			shipsSelector.selectByName(shipName);
		}
	}

}
