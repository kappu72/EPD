package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.EPD;
import it.toscana.rete.lamma.prorotype.event.ShipDataEvent;
import it.toscana.rete.lamma.prorotype.listener.ShipDataListener;
import it.toscana.rete.lamma.prorotype.model.ShipCondition;
import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

public class ShipsDataDialog extends JDialog implements ActionListener, ShipDataListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ShipsDataDialog.class);
	private CreateShip createShipPanel;
	private JButton btnAddShip = new JButton("Add Ship");
	protected ShipsSelector shipsSelector;
	private ShipConditionPanel shipConditionPanel;
	private ShipData selectedShip;
	
	public ShipsDataDialog (JFrame parent) {
        super(parent, "Ships Data Manager", true);
        setSize(462, 520);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel();
        panel.setLocation(10, 10);
        panel.setBorder(null);
        getContentPane().add(panel);
        panel.setSize(getWidth() - 20, 30);
       
        shipsSelector = new ShipsSelector(Utils.getShips());
        shipsSelector.setBounds(97, 5, 180, 20);
        shipsSelector.addActionListener(this);
        
        
        panel.setLayout(null);
        
        JLabel lblSelectAShip = new JLabel("Select a Ship");
        lblSelectAShip.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
        lblSelectAShip.setBounds(6, 6, 79, 16);
        panel.add(lblSelectAShip);
        lblSelectAShip.setLabelFor(shipsSelector);
        
        
        panel.add(shipsSelector);
        
        
        /**
         * Setting add ship btn	
         */
        btnAddShip.setBounds(356, 5, 80, 20);
        btnAddShip.addActionListener(this);
        panel.add(btnAddShip);
        
        createShipPanel = new CreateShip();
        
        createShipPanel.setSize(444, 110);
        createShipPanel.setLocation(8, 43);
        createShipPanel.addShipDataListener(this);
        createShipPanel.setVisible(false);
        getContentPane().add(createShipPanel);
        
        shipConditionPanel = new ShipConditionPanel();
        shipConditionPanel.setBounds(8, 43, 444, 449);
        shipConditionPanel.setVisible(false);
        getContentPane().add(shipConditionPanel);
        
        try {
        	shipsSelector.setSelectedIndex(0);
        }catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		LOG.debug("Evento", e);	
		Object source = e.getSource();
		if(source == shipsSelector) {
			if(createShipPanel.isVisible())
				createShipPanel.setVisible(false);
			
			selectedShip = (ShipData) shipsSelector.getSelectedItem();
			shipConditionPanel.setShip(selectedShip);
			shipConditionPanel.setVisible(true);
			
			
		}else if (source == btnAddShip) {
			if(shipConditionPanel.isVisible())
				shipConditionPanel.setVisible(false);
			createShipPanel.setVisible(true);
        	btnAddShip.setEnabled(false);
		}
	}

	@Override
	public void shipCreated(ShipDataEvent e) {
		if(e.getSource() == createShipPanel) {
			createShipPanel.setVisible(false);
			btnAddShip.setEnabled(true);
			shipsSelector.addShip(e.getShip(), true);
		}
		
	}

	@Override
	public void shipDataChanged(ShipDataEvent e) {
		// TODO Auto-generated method stub
		
	}


	

}
