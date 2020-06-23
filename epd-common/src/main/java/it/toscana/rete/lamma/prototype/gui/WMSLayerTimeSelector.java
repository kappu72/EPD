package it.toscana.rete.lamma.prototype.gui;


import it.toscana.rete.lamma.prototype.model.ShipData;
import org.geotools.ows.wms.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.geotools.ows.wms.xml.Dimension;

/**
 * @author kappu72@gmail.com
 *
 */
public class WMSLayerTimeSelector extends JComboBox<Date> {

	private static final Logger LOG = LoggerFactory.getLogger(WMSLayerTimeSelector.class);
	private DefaultComboBoxModel<Date> model;
	private static final long serialVersionUID = 1L;

	private static String pattern = "dd/MM/yyyy HH:mm";
	private static DateFormat df;


	public WMSLayerTimeSelector() {
		this(new ArrayList<>());
	}

	public WMSLayerTimeSelector(List<Date> times) {
		super();
		df = new SimpleDateFormat(pattern);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		setRenderer(new DimensionRenderer());
		model = new DefaultComboBoxModel(times.toArray());

		setModel((DefaultComboBoxModel) model);
        setToolTipText("Select Time");
	}
	
	/**
	 * Add the ships 
	 * 
	 */
	public void addTimes(List<Date> times) {
		model.removeAllElements();
		for (Date time : times) {
			model.addElement(time);
		}
	}
	private static class DimensionRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        		if (value instanceof Date) {
        			int step = isSelected? list.getSelectedIndex() : index;
        			step = step == -1 ? 0 : step; // non prende indice corretto sto stronzo!!
        			setText(df.format((Date) value) + String.format(" + %03d", step));
        		}
        		return this;
        }
    }
//	public Layer selectByName(String name) {
//		Layer selected = null;
//		for (int i = 0; i < model.getSize(); i++) {
//			if(model.getElementAt(i).getName().equals(name)) {
//				selected = model.getElementAt(i);
//				break;
//			}
//		}
//
//		if(selected != null) {
//			this.setSelectedItem(selected);
//		}
//		return selected;
//	}

}
