package it.toscana.rete.lamma.prototype.event;

import java.util.EventObject;

import it.toscana.rete.lamma.prototype.model.ShipData;

public class ShipDataEvent extends EventObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7300536642430638680L;
	/**
     * The non localized string that gives more details
     * of what actually caused the event.
     * This information is very specific to the component
     * that fired it.

     * @serial
     * @see #getActionCommand
     */
    private String actionCommand;
    private ShipData ship;
	
	public ShipDataEvent(Object source, String command) {
		super(source);
		this.actionCommand = command;
		
	}
	public ShipDataEvent(Object source, String command, ShipData ship) {
		super(source);
		this.actionCommand = command;
		this.ship = ship;
	}
	public ShipData getShip() {
		return ship;
	}
	public String getActionCommand() {
		return actionCommand;
	}

}
