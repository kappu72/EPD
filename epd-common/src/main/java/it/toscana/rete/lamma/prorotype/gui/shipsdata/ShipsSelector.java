package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;

/**
 * @author kappu72@gmail.com
 *
 */
public class ShipsSelector extends JComboBox<String> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShipsSelector.class);
	private DefaultComboBoxModel<ShipData> model;
	private static final long serialVersionUID = 1L;
	private List<ShipData>  shipsList;

	ShipsSelector (List<ShipData> ships) {
		super();
		setRenderer(new ShipRenderer());
		model = new DefaultComboBoxModel(ships.toArray());
		shipsList = ships;
		setModel((DefaultComboBoxModel) model);
        setToolTipText("Select Ship");
        setSize(160, 20);
        
	}
	
	/**
	 * Add the ships 
	 * 
	 */
	public void addShips(List<ShipData> ships) {
		shipsList = ships;
		model.removeAllElements();
		for (ShipData ship : ships) {
			model.addElement(ship);
		}
	}
	/**
	 * Add a ship to selector
	 * 
	 */
	public void addShip(ShipData ship, boolean select) {
		shipsList.add(ship);
		model.addElement(ship);
		if(select) {
			this.setSelectedItem(ship);
		}
	}
	private static class ShipRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        		if (value instanceof ShipData) {
        			setText(((ShipData) value).getShipName());
        		}
        		return this;
        }
    }
	public void selectByName(String name) {
		ShipData d = shipsList.stream()
				  .filter(data -> data.getShipName().equals(name))
				  .findAny()
				  .orElse(null);
		if(d != null) {
			this.setSelectedItem(d);
		}
	}

}
