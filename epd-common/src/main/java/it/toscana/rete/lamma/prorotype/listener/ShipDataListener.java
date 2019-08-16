package it.toscana.rete.lamma.prorotype.listener;

import java.util.EventListener;

import it.toscana.rete.lamma.prorotype.event.ShipDataEvent;

public interface ShipDataListener extends EventListener {
	public abstract void shipCreated(ShipDataEvent e);
	public abstract void shipDataChanged(ShipDataEvent e);
}
