package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.model.ShipCondition;
import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;
import java.awt.Component;

/**
 * @author kappu
 *
 */
public class ShipConditionsSelector extends JComboBox<String> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShipConditionsSelector.class);
	private DefaultComboBoxModel model;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ShipConditionsSelector (List<ShipCondition> conditions) {
		super();
		this.setRenderer(new ConditionRenderer());
		model = new DefaultComboBoxModel(conditions.toArray());
		setModel(model);
        setToolTipText("Select Ship");
        setSize(160, 20);
        setSelectedIndex(-1);
	}
	
	/**
	 * Add new List of condition
	 * 
	 */
	public void addConditions(List<ShipCondition> conditions) {
		model.removeAllElements();
		for (ShipCondition condition : conditions) {
			model.addElement(condition);
		}
	}
	/**
	 * Add a condition to selector
	 * 
	 */
	public void addCondition(ShipCondition condition, boolean select) {
		model.addElement(condition);
		if(select) {
			this.setSelectedItem(condition);
		}
	}
	private static class ConditionRenderer extends BasicComboBoxRenderer {
	        @Override
	        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        		if (value instanceof ShipCondition) {
	        			setText(((ShipCondition) value).getName());
	        		}
	        		return this;
	        }
	    }

}
