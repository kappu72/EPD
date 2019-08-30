package it.toscana.rete.lamma.prototype.gui.shipsdata;


import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prototype.event.ShipDataEvent;
import it.toscana.rete.lamma.prototype.listener.ShipDataListener;
import it.toscana.rete.lamma.prototype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;

import javax.swing.JLabel;
import javax.swing.JFormattedTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;

/**
 * @author kappu
 *
 */
public class CreateShip extends JPanel implements ActionListener, PropertyChangeListener
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5924962304448756574L;
	private static final Logger LOG = LoggerFactory.getLogger(CreateShip.class);
	private JButton btnCreate = new JButton("Create");
	private JFormattedTextField name = new JFormattedTextField();
	private JFormattedTextField mmsi = new JFormattedTextField( Utils.getMMSIFormatter());
	
	CreateShip () {
		super();
		setBorder(new TitledBorder(null, "New Ship", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setBounds(6, 6, 438, 110);
        setLayout(null);
        
        JLabel lblName = new JLabel("Name");
        lblName.setToolTipText("Optional ship name");
        lblName.setBounds(16, 31, 61, 16);
        add(lblName);
        
        JLabel lblM = new JLabel("MMSI*");
        lblM.setToolTipText("Mandatory MMSI ID");
        lblM.setBounds(16, 59, 61, 16);
        add(lblM);
        
        
        name.setBounds(89, 26, 117, 26);
        add(name);
        mmsi.setFocusLostBehavior(JFormattedTextField.COMMIT);
     
        
        
        mmsi.setBounds(89, 54, 117, 26);
        mmsi.addPropertyChangeListener("editValid", this);
        mmsi.addActionListener(this);
        add(mmsi);
        
        btnCreate.setBounds(315, 54, 117, 29);
        btnCreate.setEnabled(false);
        btnCreate.addActionListener(this);
        add(btnCreate);
        
		
	}
	
	void  createShip() {
		
		ShipData ship = Utils.createShip(mmsi.getValue(), name.getText());
		if(ship != null) {
			fireShipCreated(new ShipDataEvent(this, "ships-created", ship));
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnCreate) {
			createShip();
		}
	}
	
	/**
	 * It checks if MMSI code is valid and toggle create button 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(mmsi.isEditValid()) {			
			btnCreate.setEnabled(true);
		}else if (btnCreate.isEnabled()) {
			btnCreate.setEnabled(false);
		}
		
		
	}
	// Block of code that manages  event dispatches
	
	public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

	public void addShipDataListener(ShipDataListener listener) {
	    listenerList.add(ShipDataListener.class, listener);
	}
	public void removeShipDataListener(ShipDataListener listener) {
	    listenerList.remove(ShipDataListener.class, listener);
	}
	void fireShipCreated(ShipDataEvent e) {
	    Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == ShipDataListener.class) {
	        ((ShipDataListener) listeners[i+1]).shipCreated(e);
	      }
	    }
	  }
}
