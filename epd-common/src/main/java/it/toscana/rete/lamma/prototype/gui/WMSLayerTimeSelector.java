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
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
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
		public Date selectByDate(Date searchDate) {
			Layer selected = null;
			Long diff;

			if(model.getSize() == 0 ) {
				return null;
			}
			if(searchDate.before(model.getElementAt(0))) {
				this.setSelectedIndex(0);
				return (Date) model.getSelectedItem();
			}
			if(searchDate.after(model.getElementAt(model.getSize()-1))){
				this.setSelectedIndex(model.getSize() -1);
				return (Date) model.getSelectedItem();
			}

			diff =  Math.abs(searchDate.getTime() - model.getElementAt(0).getTime());

			for (int i = 1; i < model.getSize(); i++) {
				if (diff < Math.abs(searchDate.getTime() - model.getElementAt(i).getTime())) {
					this.setSelectedIndex(i);
					break;
				}
				   diff = Math.abs(searchDate.getTime() - model.getElementAt(i).getTime() );
			}
			return (Date) model.getSelectedItem();
		}


}
