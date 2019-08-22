package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.toscana.rete.lamma.prorotype.event.ShipDataEvent;
import it.toscana.rete.lamma.prorotype.listener.ShipDataListener;
import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;

public class CreateConfigurationPanel extends JPanel implements ActionListener, KeyListener {
		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 244123912635184313L;
	private static final Logger LOG = LoggerFactory.getLogger(CreateConfigurationPanel.class);
	
	private JButton btnCreate = new JButton("Create");
	private JFormattedTextField name = new JFormattedTextField();
	private ShipData ship;
	CreateConfigurationPanel(ShipData ship) {
		super();
		this.ship = ship;
		setBounds(6, 6, 438, 26);
        setLayout(null);
        
		name.setBounds(113, 3, 180, 20);
		name.addKeyListener(this);
        add(name);
        
        JLabel lblName = new JLabel("Configuration Name*");
        lblName.setBounds(0, 6, 113, 16);
        lblName.setLabelFor(name);
        add(lblName);
        btnCreate.setEnabled(false);
        btnCreate.addActionListener(this);
        btnCreate.setBounds(330, 3, 99, 20);
        add(btnCreate);
	}
	public void setShip(ShipData ship) {
		this.ship = ship;
		
	}
	
	public void reset() {
		name.setText(null);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnCreate){
			ship.addConfiguration(name.getText());
			fireShipUpdated(new ShipDataEvent(this, "configuration-created", ship));
			this.setVisible(false);
		}
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		LOG.info(e.toString());
		name.getText();
		if(name.getText().length() > 0) {			
			btnCreate.setEnabled(true);
		}else if (btnCreate.isEnabled()) {
			btnCreate.setEnabled(false);
		}
		
	}
	public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

	public void addShipDataListener(ShipDataListener listener) {
	    listenerList.add(ShipDataListener.class, listener);
	}
	public void removeShipDataListener(ShipDataListener listener) {
	    listenerList.remove(ShipDataListener.class, listener);
	}
	void fireShipUpdated(ShipDataEvent e) {
	    Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == ShipDataListener.class) {
	        ((ShipDataListener) listeners[i+1]).shipDataChanged(e);
	      }
	    }
	  }
	
}
