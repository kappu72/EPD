package it.toscana.rete.lamma.prototype.gui.shipsdata;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicListUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import it.toscana.rete.lamma.prototype.event.ShipDataEvent;
import it.toscana.rete.lamma.prototype.listener.ShipDataListener;
import it.toscana.rete.lamma.prototype.model.ShipConfiguration;
import it.toscana.rete.lamma.prototype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;
import javax.swing.JFormattedTextField;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.JTextField;


public class ShipConfigurationPanel extends JPanel implements ActionListener, ShipDataListener, KeyListener {

	/**
	 * Manage the import of tables needed to calculate fuel consumption
	 * Valid-Minimal set of table
	 * Wind/Wave-generic/Propeller  
	 * Propeller setting could be more then one
	 * Wave could be generic o specific (swell and see)
	 */
	 
	private static final long serialVersionUID = -5763833385898569850L;
	private static final Logger LOG = LoggerFactory.getLogger(ShipConfigurationPanel.class);
	private ShipConfigurationsSelector configurationSelector;
	private JButton btnAddConfiguration = new JButton("New Configuration");
	private CreateConfigurationPanel createConfiguration;
	private ShipData ship;
	private JPanel filesPanel = new JPanel();
	JButton wiBtn = new JButton("Import");
	private ConfigurationsFileSelector windres = new ConfigurationsFileSelector("Windres*");
	private ConfigurationsFileSelector waveres = new ConfigurationsFileSelector("Waveres Generic*");
	private ConfigurationsFileSelector waveres_sw = new ConfigurationsFileSelector("Waveres Swell");
	private ConfigurationsFileSelector waveres_se = new ConfigurationsFileSelector("Waveres Sea");
	private ConfigurationsFileSelector hullres = new ConfigurationsFileSelector("Hull Resitance");
	private JTextField propName = new JFormattedTextField();
	private final JLabel lblNewConfigName = new JLabel("New Config. Name");
	private JList propConfigs = new JList();
	private JButton importProp = new JButton("Import");
	private JFileChooser fc = new JFileChooser();
	
	ShipConfigurationPanel() {
		this(null);

	}
	
	ShipConfigurationPanel (ShipData ship) {
		super();
		setBorder(new TitledBorder(null, "SaiConfiguration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setBounds(8, 43, 444, 455);
        setLayout(null);
		if(ship != null) {
			configurationSelector = new ShipConfigurationsSelector(ship.getShipConfigurations());
		}else {
			configurationSelector = new ShipConfigurationsSelector(new ArrayList<ShipConfiguration>());
		}
        
        configurationSelector.setBounds(119, 31, 180, 20);
        configurationSelector.addActionListener(this);
        
        /**
         * Setting add configuration btn	
         */
        btnAddConfiguration.setBounds(337, 32, 99, 20);
        btnAddConfiguration.addActionListener(this);
        
        JLabel lblSelectConfiguration = new JLabel("Select Configuration");
        lblSelectConfiguration.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
        lblSelectConfiguration.setBounds(6, 33, 113, 16);
        add(lblSelectConfiguration);
        lblSelectConfiguration.setLabelFor(configurationSelector);
        
        add(configurationSelector);
        add(btnAddConfiguration);
        
        createConfiguration = new CreateConfigurationPanel(ship);
        createConfiguration.addShipDataListener(this);
        createConfiguration.setSize(430, 27);
        createConfiguration.setLocation(6, 73);
        createConfiguration.setVisible(false);
        add(createConfiguration);
        
        // Panel for files editing
        filesPanel.setBorder(new TitledBorder(null, "Files configuration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filesPanel.setBounds(6, 73, 432, 359);
        filesPanel.setVisible(false);
        filesPanel.setLayout(null);
        add(filesPanel);
        
        windres.setBounds(6, 24, 420, 30);
        waveres.setBounds(6, 54, 420, 30);
        waveres_sw.setBounds(6, 84, 420, 30);
		waveres_se.setBounds(6, 114, 420, 30);
		hullres.setBounds(6, 144, 420, 30);
        
        filesPanel.add(windres);
        filesPanel.add(waveres);
        filesPanel.add(waveres_sw);
		filesPanel.add(waveres_se);
		filesPanel.add(hullres);
        
        propConfigs.setBounds(26, 208, 211, 99);
        propConfigs.setCellRenderer(new PropulsionListRenderer());
        JScrollPane listScroller = new JScrollPane(propConfigs);
        listScroller.setBounds(26, 208, 139, 99);
        filesPanel.add(listScroller);
        
        JLabel lblNewLabel = new JLabel("Propulsion Configurations*");
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setBounds(6, 192, 178, 16);
        filesPanel.add(lblNewLabel);
        
        
        propName.setBounds(177, 227, 130, 26);
        filesPanel.add(propName);
        propName.setColumns(10);
        propName.addKeyListener(this);
        
        lblNewConfigName.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewConfigName.setBounds(177, 208, 130, 16);
        
        filesPanel.add(lblNewConfigName);
        
        importProp.setBounds(326, 231, 100, 20);
        importProp.setEnabled(false);
        importProp.addActionListener(this);
        
        filesPanel.add(importProp);
        
        windres.addActionListener(this);
        waveres.addActionListener(this);
        waveres_sw.addActionListener(this);
		waveres_se.addActionListener(this);
		hullres.addActionListener(this);
        
	}
	public void setShip(ShipData ship) {
		this.ship = ship;
		addConfigurations(ship.getShipConfigurations());
		createConfiguration.setShip(ship);
	}
	public void addConfigurations (List<ShipConfiguration> configurations) {
		configurationSelector.addConfigurations(configurations);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		LOG.info(e.toString());
		Object source = e.getSource();
		if(source == btnAddConfiguration) {
			if(filesPanel.isVisible())
				filesPanel.setVisible(false);
			createConfiguration.reset();
			createConfiguration.setVisible(true);
		}else if(source == configurationSelector) {
			if(createConfiguration.isVisible())
				createConfiguration.setVisible(false);
			if(configurationSelector.getSelectedItem() != null) {
				resetFilesPanel();
				filesPanel.setVisible(true);
			}else 
				filesPanel.setVisible(false);
		}else if(source == windres) {
			windres.setName(importFile("windres"));
		}else if(source == waveres) {
			waveres.setName(importFile("waveres"));
		}else if(source == waveres_se) {
			waveres_se.setName(importFile("waveres_se"));
		}else if(source == waveres_sw) {
			waveres_sw.setName(importFile("waveres_sw"));
		}else if(source == hullres) {
			hullres.setName(importFile("hullres"));
		}else if(source == importProp) {
			String newProp = importFile("ps", propName.getText());
			if(newProp != null) {
				resetFilesPanel();
			}		
		}
		
	}

	@Override
	public void shipCreated(ShipDataEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shipDataChanged(ShipDataEvent e) {
		Object source = e.getSource();
		if (source == createConfiguration) {
			ShipConfiguration c = ship.getShipConfigurations().get(ship.getShipConfigurations().size()- 1);
			configurationSelector.addConfiguration(c , true);
		}
		
	}
	public void resetFilesPanel() {
		 ShipConfiguration configuration = (ShipConfiguration) configurationSelector.getSelectedItem();
		 windres.setName(configuration.getWindresName());
		 waveres_sw.setName(configuration.getWaveresSwName());
		 waveres_se.setName(configuration.getWaveresName());
		 waveres.setName(configuration.getWaveresName());
		 hullres.setName(configuration.getHullresName());
		 
		 propConfigs.setListData(configuration.getPropulsions().toArray());
	}
	private String importFile(String type) {
		return importFile(type, null);
	}
	private String importFile(String type, String name) {
        // Get filename from dialog
        ShipConfiguration configuration = (ShipConfiguration) configurationSelector.getSelectedItem();
        
       

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        // TODO try to filter files when will have a convention
        fc.addChoosableFileFilter(new FileNameExtensionFilter(
        		"Lookup file", "dat", "DAT"));
        fc.setAcceptAllFileFilterUsed(true);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = fc.getSelectedFile();
        if(file != null) {
        	LOG.info(file.getName());
        	try {
        		if(type == "windres")
        			return configuration.importWindres(file);
        		if(type == "waveres")
        			return configuration.importWaveres(file);
        		if(type == "waveres_sw")	
            		return configuration.importWaveresSw(file);
        		if(type == "waveres_se")	
					return configuration.importWaveresSe(file);
				if(type == "hullres")	
            		return configuration.importHullres(file);
        		if(type == "ps")	
            		return configuration.importPropulsion(file, name);
        	} catch (IOException e) {
        		JOptionPane.showMessageDialog(this, e.getMessage() + ": "
                      + file.getName(), "Import file error",
                      JOptionPane.ERROR_MESSAGE);
        		return null;
        		}
        	}
        return null;    
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		LOG.info(e.toString());
		// TODO the propConfig name ideally should be unique ad a check in the prop list
		if(propName.getText().length() > 0) {			
			importProp.setEnabled(true);
		}else if (importProp.isEnabled()) {
			importProp.setEnabled(false);
		}

		
	}
	protected class PropulsionListRenderer extends DefaultListCellRenderer  {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Path p = (Path) value;
            setText(Utils.stripFileExt(p.toFile()));
            return this;
        }
    }
}
