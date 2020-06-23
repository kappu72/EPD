package it.toscana.rete.lamma.prototype.gui;


import org.geotools.ows.wms.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;



/**
 * @author kappu72@gmail.com
 *
 */
public class WMSLayerSelector extends JComboBox<String> {

	private static final Logger LOG = LoggerFactory.getLogger(WMSLayerSelector.class);
	private DefaultComboBoxModel<Layer> model;
	private static final long serialVersionUID = 1L;


	public WMSLayerSelector(Layer[] layers) {
		super();
		setRenderer(new LayerRenderer());
		model = new DefaultComboBoxModel(layers);

		setModel((DefaultComboBoxModel) model);
        setToolTipText("Select Layer");
        setSize(160, 20);
        
	}
	
	/**
	 * Add the ships 
	 * 
	 */
	public void addLayers(Layer[] layers) {
		model.removeAllElements();
		for (int i =0; i < layers.length; i++) {
			model.addElement(layers[i]);
		}
	}
	private static class LayerRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        		if (value instanceof Layer) {
        			setText(((Layer) value).getName());
        		}
        		return this;
        }
    }
	public Layer selectByName(String name) {
		Layer selected = null;
		for (int i = 0; i < model.getSize(); i++) {
			if(model.getElementAt(i).getName().equals(name)) {
				selected = model.getElementAt(i);
				break;
			}
		}

		if(selected != null) {
			this.setSelectedItem(selected);
		}
		return selected;
	}

}
