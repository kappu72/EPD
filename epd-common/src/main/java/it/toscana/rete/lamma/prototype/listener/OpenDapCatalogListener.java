package it.toscana.rete.lamma.prototype.listener;

import java.util.EventListener;

import it.toscana.rete.lamma.prototype.event.OpenDapCatalogEvent;;

public interface OpenDapCatalogListener extends EventListener {
	public abstract void onCatalogChanged(OpenDapCatalogEvent e);
}
