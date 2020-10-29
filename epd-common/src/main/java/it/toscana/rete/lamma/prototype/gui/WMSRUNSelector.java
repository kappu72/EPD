package it.toscana.rete.lamma.prototype.gui;


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

/**
 * @author kappu72@gmail.com
 *
 */
public class WMSRUNSelector extends JComboBox<Date> {

		private static final Logger LOG = LoggerFactory.getLogger(WMSRUNSelector.class);
		private DefaultComboBoxModel<String> model;
		private static final long serialVersionUID = 1L;


		public WMSRUNSelector() {
			this(new ArrayList<String>());
		}

		public WMSRUNSelector(List<String> runs) {
			super();
			model = new DefaultComboBoxModel(runs.toArray());
			setModel((DefaultComboBoxModel) model);
			setToolTipText("Select RUN");
		}

		/**
		 * Add runs
		 *
		 */
		public void addRUNS(List<String> runs) {
			model.removeAllElements();
			for (String r : runs) {
				model.addElement(r);
			}
		}
}
