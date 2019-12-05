package it.toscana.rete.lamma.prototype.gui.shipsdata;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;


import thredds.client.catalog.ServiceType;
import thredds.client.catalog.Dataset;



/**
 * @author kappu72@gmail.com
 *
 */
public class DatasetSelector extends JComboBox<String> {
	
	private DefaultComboBoxModel<Dataset> model;
	private static final long serialVersionUID = 1L;
	private List<Dataset>  datasetList;

	public DatasetSelector () {
		super();
		setRenderer(new DatasetRenderer());
		model = new DefaultComboBoxModel();
		setModel((DefaultComboBoxModel) model);
        setToolTipText("Select metoc file");
        setSize(160, 20);
	}
	
	/**
	 * Add the ships 
	 * 
	 */
	public void addDatasets(List<Dataset> ds) {
		datasetList = ds;
		model.removeAllElements();
		for (Dataset d : ds) {
			model.addElement(d);
		}
	}

	private static class DatasetRenderer extends BasicComboBoxRenderer {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        		if (value instanceof Dataset) {
        			setText(((Dataset) value).getName());
        		}
        		return this;
        }
    }
	public Dataset selectByName(String name) {
		Dataset d = datasetList.stream()
				  .filter(data -> data.getName().equals(name))
				  .findAny()
				  .orElse(null);
		if(d != null) {
			this.setSelectedItem(d);
		}
		return d;
	}
	public Dataset selectByURL(String metocFile) {
		Dataset d = datasetList.stream()
				  .filter(data -> data.getAccess(ServiceType.OPENDAP).getUrlPath().equals(metocFile))
				  .findAny()
				  .orElse(null);
		if(d != null) {
			this.setSelectedItem(d);
		}
		return d;
	}

}
