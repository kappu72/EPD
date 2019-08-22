package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.model.ShipConfiguration;
import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;
import java.awt.Component;
import java.awt.Dimension;

/**
 * @author kappu
 *
 */
public class ShipConfigurationsSelector extends JComboBox<String> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShipConfigurationsSelector.class);
	private DefaultComboBoxModel model;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShipConfigurationsSelector (List<ShipConfiguration> configurations) {
		super();
		this.setRenderer(new ConfigurationRenderer());
		model = new DefaultComboBoxModel(configurations.toArray());
		setModel(model);
        setToolTipText("Select Ship");
        setPreferredSize(new Dimension(160, 20));
        setSelectedIndex(-1);
	}
	
	/**
	 * Add new List of configurations
	 * 
	 */
	public void addConfigurations(List<ShipConfiguration> configurations) {
		model.removeAllElements();
		for (ShipConfiguration config : configurations) {
			model.addElement(config);
		}
	}
	/**
	 * Add a configuration to selector list
	 * 
	 */
	public void addConfiguration(ShipConfiguration configuration, boolean select) {
		model.addElement(configuration);
		if(select) {
			this.setSelectedItem(configuration);
		}
	}
	private static class ConfigurationRenderer extends BasicComboBoxRenderer {
	        @Override
	        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        		if (value instanceof ShipConfiguration) {
	        			setText(((ShipConfiguration) value).getName());
	        		}
	        		return this;
	        }
	    }
	
}
