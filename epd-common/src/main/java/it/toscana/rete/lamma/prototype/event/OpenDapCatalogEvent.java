package it.toscana.rete.lamma.prototype.event;

import java.util.List;
import java.util.EventObject;

import thredds.client.catalog.Dataset;

public class OpenDapCatalogEvent extends EventObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
	/**
     * The non localized string that gives more details
     * of what actually caused the event.
     * This information is very specific to the component
     * that fired it.

     * @serial
     * @see #getActionCommand
     */
    private List<Dataset> ds;
	
	public OpenDapCatalogEvent(Object source) {
		super(source);
	}
	public OpenDapCatalogEvent(Object source, List<Dataset> ds) {
		super(source);
		this.ds = ds;
	}
	public List<Dataset> getDatasetList() {
		return ds;
	}
}
