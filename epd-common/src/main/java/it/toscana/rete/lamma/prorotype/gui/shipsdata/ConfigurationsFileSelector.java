package it.toscana.rete.lamma.prorotype.gui.shipsdata;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ConfigurationsFileSelector extends JPanel {
		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9032305325597405184L;
	
	private JButton importBtn = new JButton("Import");
	private JLabel  name = new JLabel("Not Available");
	private JLabel lab;
	ConfigurationsFileSelector(String label) {
		
		lab = new JLabel(label);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lab, GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
					.addGap(8)
					.addComponent(name, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
					.addGap(7)
					.addComponent(importBtn, GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
					.addContainerGap())
					
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE, false)
						.addComponent(lab)
						.addComponent(importBtn, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(name))
					.addContainerGap())
		);
		setLayout(groupLayout);
	}
	public void setName(String name) {
		if(name != null)
			this.name.setText(name);
		else 
			this.name.setText("Not Available");
	}
	public void addActionListener(ActionListener a) {
		importBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.actionPerformed(new ActionEvent(ConfigurationsFileSelector.this, e.getID(), e.getActionCommand()));
			}
		});
	}
}
