package it.toscana.rete.lamma.prototype.gui.shipsdata;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prototype.model.ShipConfiguration;
import it.toscana.rete.lamma.prototype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;
import java.awt.Component;
import java.awt.Dimension;
import java.nio.file.Path;

/**
 * @author kappu
 *
 */
public class ShipPropulsionConfigurationsSelector extends JComboBox<String> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShipPropulsionConfigurationsSelector.class);
	private DefaultComboBoxModel model;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShipPropulsionConfigurationsSelector (List<Path> propulsionConfigurations) {
		super();
		this.setRenderer(new PropulsionConfigurationRenderer());
		model = new DefaultComboBoxModel(propulsionConfigurations.toArray());
		setModel(model);
        setToolTipText("Select Propulsion Configuration");
        setPreferredSize(new Dimension(160, 20));
        setSelectedIndex(-1);
	}
	
	/**
	 * Add new List of configurations
	 * 
	 */
	public void addConfigurations(List<Path> configurations) {
		model.removeAllElements();
		for (Path config : configurations) {
			model.addElement(config);
		}
	}
	/**
	 * Add a configuration to selector list
	 * 
	 */
	public void addConfiguration(Path configuration, boolean select) {
		model.addElement(configuration);
		if(select) {
			this.setSelectedItem(configuration);
		}
	}
	protected class PropulsionConfigurationRenderer extends BasicComboBoxRenderer  {
		 /**
		 * 
		 */
		private static final long serialVersionUID = 10926084912941047L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(value != null) {
            	Path p = (Path) value;
            	setText(Utils.stripFileExt(p.toFile()));
            }
            return this;
        }
    }
}
