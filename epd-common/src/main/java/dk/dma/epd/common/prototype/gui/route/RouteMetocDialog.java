/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.epd.common.prototype.gui.route;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;
import dk.dma.epd.common.prototype.gui.metoc.MetocRequestDialog;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteMetocSettings;
import dk.dma.epd.common.prototype.monalisa.XMLDialog;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import dk.dma.epd.common.prototype.shoreservice.Metoc;
import dk.dma.epd.common.text.Formatter;
import dk.frv.enav.common.xml.metoc.MetocDataTypes;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.request.MetocForecastRequest;
import it.toscana.rete.lamma.prototype.event.OpenDapCatalogEvent;
import it.toscana.rete.lamma.prototype.gui.shipsdata.DatasetSelector;
import it.toscana.rete.lamma.prototype.listener.OpenDapCatalogListener;
import it.toscana.rete.lamma.prototype.metocservices.MetocProviders;
import thredds.client.catalog.Dataset;
import java.awt.Component;

/**
 * Dialog with METOC settings
 */
public class RouteMetocDialog extends JDialog implements ActionListener, FocusListener, OpenDapCatalogListener {

        private static final long serialVersionUID = 1L;

        private JTextField windLimit;
        private JTextField currentLimit;
        private JTextField waveLimit;
        private JCheckBox showCheckbox;
        private JLabel currentLabel;
        private JLabel currentMetocDataLbl;
        private JLabel intervalLbl;
        private JComboBox<String> intervalDb;
        private JButton requestBtn;
        private JCheckBox windCb;
        private JCheckBox currentCb;
        private JCheckBox wavesCb;
        private JCheckBox seaLevelCb;
        private JCheckBox densityCb;
        private JLabel windLimitLbl;
        private JLabel currentLimitLbl;
        private JLabel waveLimitLbl;
        private JPanel statusPanel;
        private JPanel typesPanel;
        private JPanel warnLimitsPanel;
        private JPanel localMetocPanel;
        private JPanel lammaMetocPanel;

        JButton closeBtn;

        private RouteManagerCommon routeManager;
        private Route route;
        private JPanel providerPanel;
        private JComboBox<String> providerBox;
        private JCheckBox chckbxShowRawRequest;
        private JCheckBox fromToCb;
        private JCheckBox uvTuCb;
        private JButton btnSelectFile;
        private JButton btnSelectPartFile;
        private JLabel lblMetocLocal;
        private String localFilePath;
        private JLabel lblMetocLocalWavePart;
        private String localPartFilePath;
        private DatasetSelector lammaSelector;

        private JFileChooser fc = new JFileChooser();

        public RouteMetocDialog(Window parent, RouteManagerCommon routeManager, int routeId) {
                super(parent, "Route METOC properties", Dialog.ModalityType.APPLICATION_MODAL);

                this.routeManager = routeManager;
                this.route = routeManager.getRoute(routeId);

                setSize(350, 700);
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                setLocationRelativeTo(parent);

                initGui();
                updateFields();
                checkStatus();
                // Checks request btn
        }
        private RouteMetocSettings getRouteMetocSettings() {
                if (route.getRouteMetocSettings() == null) {
                        // Set default settings
                        route.setRouteMetocSettings(routeManager.getDefaultRouteMetocSettings());
                }
                return route.getRouteMetocSettings();
        }
        private void updateFields() {
                
                RouteMetocSettings metocSettings = getRouteMetocSettings();
                MetocForecast metocForecast = route.getMetocForecast();

                // Enabled or not
                if (metocForecast == null) {
                        currentMetocDataLbl.setText("None");
                } else {
                        currentMetocDataLbl.setText(Formatter.formatLongDateTime(metocForecast.getCreated()));
                }
                showCheckbox.setSelected(metocSettings.isShowRouteMetoc());

                // Interval
                intervalDb.getModel().setSelectedItem(Integer.toString(metocSettings.getInterval()));

                // Provider
                providerBox.setSelectedItem(metocSettings.getProvider());

                // METOC data
                windCb.setSelected(metocSettings.getDataTypes().contains(MetocDataTypes.WI));
                currentCb.setSelected(metocSettings.getDataTypes().contains(MetocDataTypes.CU));
                wavesCb.setSelected(metocSettings.getDataTypes().contains(MetocDataTypes.WA));
                seaLevelCb.setSelected(metocSettings.getDataTypes().contains(MetocDataTypes.SE));
                densityCb.setSelected(metocSettings.getDataTypes().contains(MetocDataTypes.DE));

                // Warn limits
                windLimit.setText(String.format("%.2f", metocSettings.getWindWarnLimit()));
                windLimit.addFocusListener(this);
                currentLimit.setText(String.format("%.2f", metocSettings.getCurrentWarnLimit()));
                currentLimit.addFocusListener(this);
                waveLimit.setText(String.format("%.2f", metocSettings.getWaveWarnLimit()));
                waveLimit.addFocusListener(this);

                // local metoc settings
                fromToCb.setSelected(metocSettings.getTo());
                uvTuCb.setSelected(metocSettings.getUvDim());
                localFilePath = metocSettings.getLocalMetocFile();
                if (localFilePath != null) {
                        Path p = Paths.get(localFilePath);
                        lblMetocLocal.setText(p.getFileName().toString());
                }
                localPartFilePath = metocSettings.getLocalPartMetocFile();
                if (localPartFilePath != null) {
                        Path p = Paths.get(localPartFilePath);
                        lblMetocLocal.setText(p.getFileName().toString());
                }

                // lamma metoc settings
                // if(metocSettings.getProvider().equals(MetocProviders.LAMMA.label())) {
                //         routeManager.getLammaMetocService().getDatasetList(this);
                // }
                // show raw xml
                if (chckbxShowRawRequest.isSelected()) {
                        try {
                                MetocForecastRequest req = Metoc.generateMetocRequest(route,
                                                EPD.getInstance().getPosition());

                                JAXBContext context = JAXBContext.newInstance(MetocForecastRequest.class);
                                Marshaller m = context.createMarshaller();
                                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                                m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                                StringWriter st = new StringWriter();

                                m.marshal(req, st);

                                new XMLDialog(st.toString(), "RAW XML Request").setVisible(true);

                        } catch (ShoreServiceException | JAXBException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }
                checkStatus();
        }

        void saveValues() {
                RouteMetocSettings metocSettings = route.getRouteMetocSettings();

                metocSettings.setShowRouteMetoc(showCheckbox.isSelected());
                metocSettings.setInterval(Integer.parseInt((String) intervalDb.getSelectedItem()));

                Set<MetocDataTypes> dataTypes = metocSettings.getDataTypes();
                dataTypes.clear();
                if (windCb.isSelected()) {
                        dataTypes.add(MetocDataTypes.WI);
                }
                if (currentCb.isSelected()) {
                        dataTypes.add(MetocDataTypes.CU);
                }
                if (wavesCb.isSelected()) {
                        dataTypes.add(MetocDataTypes.WA);
                }
                if (seaLevelCb.isSelected()) {
                        dataTypes.add(MetocDataTypes.SE);
                }
                if (densityCb.isSelected()) {
                        dataTypes.add(MetocDataTypes.DE);
                }

                metocSettings.setWindWarnLimit(parseFieldVal(windLimit, metocSettings.getWindWarnLimit()));
                metocSettings.setCurrentWarnLimit(parseFieldVal(currentLimit, metocSettings.getCurrentWarnLimit()));
                metocSettings.setWaveWarnLimit(parseFieldVal(waveLimit, metocSettings.getWaveWarnLimit()));

                metocSettings.setProvider((String) providerBox.getSelectedItem());

                metocSettings.setUvDim(uvTuCb.isSelected());
                metocSettings.setTo(fromToCb.isSelected());
                metocSettings.setLocalMetocFile(localFilePath);
                metocSettings.setLocalPartMetocFile(localPartFilePath);
                Dataset d = (Dataset) lammaSelector.getSelectedItem();
                String lammaMetoc = null;
                if (d != null) {
                        lammaMetoc= d.getName();
                }
                metocSettings.setLammaMetocFile(lammaMetoc);
        }

        private void requestMetoc() {
                showCheckbox.setSelected(true);
                saveValues();
                MetocRequestDialog.requestMetoc(this, routeManager, route);
                updateFields();
        }

        // Enable disable the btn based on current config
        private void toggleRequestBtn() {
                if(providerBox.getSelectedItem().equals( MetocProviders.LOCAL.label())){
                        requestBtn.setEnabled(localFilePath != null);
                }else if(providerBox.getSelectedItem().equals (MetocProviders.LAMMA.label())) {
                        requestBtn.setEnabled(lammaSelector != null && lammaSelector.getSelectedItem() != null);
                }else {
                       
                                requestBtn.setEnabled(true);
                }
        }

        private void toggleLocalPanel() {
                boolean isEnabled = MetocProviders.LOCAL.label() == providerBox.getSelectedItem();

                localMetocPanel.setEnabled(isEnabled);
                uvTuCb.setEnabled(isEnabled);
                fromToCb.setEnabled(isEnabled);
                btnSelectFile.setEnabled(isEnabled);
                lblMetocLocal.setEnabled(isEnabled);
                btnSelectPartFile.setEnabled(isEnabled);
                lblMetocLocalWavePart.setEnabled(isEnabled);

        }

        private void toggleLammaPanel() {
                boolean isEnabled = MetocProviders.LAMMA.label() == providerBox.getSelectedItem() && lammaSelector.getModel().getSize() > 0 ;

                lammaMetocPanel.setEnabled(isEnabled);
                lammaSelector.setEnabled(isEnabled);

        }

        private void checkStatus() {
                toggleRequestBtn();
                toggleLocalPanel();
                toggleLammaPanel();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
                if (e.getSource() == closeBtn) {
                        saveValues();
                        dispose();
                } else if (e.getSource() == requestBtn) {
                        requestMetoc();
                } else if (e.getSource() == btnSelectFile) {
                        setLocalFile();
                } else if (e.getSource() == btnSelectPartFile) {
                        setLocalPartFile();
                } else if (e.getSource() == providerBox &&  providerBox.getSelectedItem().equals(MetocProviders.LAMMA.label()) && lammaSelector.getModel().getSize() == 0) {
                        routeManager.getLammaMetocService().getDatasetList(this);
                }
                checkStatus(); 

        }

        @Override
        public void focusLost(FocusEvent e) {
                if (!(e.getSource() instanceof JTextField)) {
                        return;
                }
                RouteMetocSettings metocSettings = route.getRouteMetocSettings();
                if (e.getSource() == windLimit) {
                        parseFieldVal(windLimit, metocSettings.getWindWarnLimit());
                }
                if (e.getSource() == currentLimit) {
                        parseFieldVal(currentLimit, metocSettings.getCurrentWarnLimit());
                }
                if (e.getSource() == waveLimit) {
                        parseFieldVal(waveLimit, metocSettings.getWaveWarnLimit());
                }
        }

        @Override
        public void focusGained(FocusEvent e) {

        }

        private static Double parseFieldVal(JTextField field, Double defaultVal) {
                Double val = defaultVal;
                String strVal = field.getText();
                // Be relaxed
                strVal = strVal.replace(',', '.');
                try {
                        val = Double.parseDouble(strVal);
                } catch (NumberFormatException e) {
                }
                field.setText(String.format("%.2f", val));
                return val;
        }

        private String setLocalFile() {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(false);
                // TODO add more metoc file ext
                fc.addChoosableFileFilter(new FileNameExtensionFilter("metoc", "nc", "grb"));
                fc.setAcceptAllFileFilterUsed(true);

                if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                        lblMetocLocal.setText("Select a metoc file!");
                        localFilePath = null;
                        return null;
                }
                File file = fc.getSelectedFile();
                if (file != null) {
                        lblMetocLocal.setText(file.getName());
                        localFilePath = file.getPath();
                }

                return null;
        }

        private String setLocalPartFile() {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(false);
                // TODO add more metoc file ext
                fc.addChoosableFileFilter(new FileNameExtensionFilter("metoc", "nc", "grb"));
                fc.setAcceptAllFileFilterUsed(true);

                if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                        lblMetocLocalWavePart.setText("Optional Wave partitions file");
                        localPartFilePath = null;
                        return null;
                }
                File file = fc.getSelectedFile();
                if (file != null) {
                        lblMetocLocalWavePart.setText(file.getName());
                        localPartFilePath = file.getPath();
                }

                return null;
        }

        private void initGui() {

                showCheckbox = new JCheckBox("Show route METOC (if available)");
                showCheckbox.setSelected(true);
                showCheckbox.setEnabled(true);
                currentLabel = new JLabel("Current METOC data:");
                currentMetocDataLbl = new JLabel("None");
                currentMetocDataLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                intervalLbl = new JLabel("Point interval (min)");
                intervalDb = new JComboBox<>();
                intervalDb.setModel(new DefaultComboBoxModel<>(new String[] { "15", "30", "45", "60" }));
                intervalDb.setSelectedIndex(0);
                intervalDb.setMaximumRowCount(4);
                requestBtn = new JButton("Request METOC");
                requestBtn.addActionListener(this);
                closeBtn = new JButton("Close");
                closeBtn.addActionListener(this);

                windCb = new JCheckBox("Wind");
                currentCb = new JCheckBox("Current");
                wavesCb = new JCheckBox("Waves");
                seaLevelCb = new JCheckBox("Sea Level");
                densityCb = new JCheckBox("Density");

                uvTuCb = new JCheckBox("UV*/ØU");
                fromToCb = new JCheckBox("To*/From");

                windLimitLbl = new JLabel("Wind speed m/s");
                currentLimitLbl = new JLabel("Current speed kn");
                waveLimitLbl = new JLabel("Mean wave height m");
                windLimit = new JTextField();
                windLimit.setColumns(10);
                currentLimit = new JTextField();
                currentLimit.setColumns(10);
                waveLimit = new JTextField();
                waveLimit.setColumns(10);

                statusPanel = new JPanel();

                statusPanel.setBorder(new TitledBorder(null, "METOC status", TitledBorder.LEADING, TitledBorder.TOP,
                                null, null));

                typesPanel = new JPanel();
                typesPanel.setBorder(new TitledBorder(null, "METOC data", TitledBorder.LEADING, TitledBorder.TOP, null,
                                null));
                warnLimitsPanel = new JPanel();
                warnLimitsPanel.setBorder(new TitledBorder(null, "Warn limits", TitledBorder.LEADING, TitledBorder.TOP,
                                null, null));

                localMetocPanel = new JPanel();
                localMetocPanel.setBorder(new TitledBorder(null, "METOC Local", TitledBorder.LEADING, TitledBorder.TOP,
                                null, null));

                lammaMetocPanel = new JPanel();
                lammaMetocPanel.setBorder(new TitledBorder(null, "Lamma OPENDAP", TitledBorder.LEADING,
                                TitledBorder.TOP, null, null));

                providerPanel = new JPanel();
                providerPanel.setBorder(new TitledBorder(null, "METOC provider", TitledBorder.LEADING, TitledBorder.TOP,
                                null, null));

                providerBox = new JComboBox<String>();
                for (MetocProviders p : MetocProviders.values()) {
                        providerBox.addItem(p.label());
                }
                providerBox.addActionListener(this);
                providerBox.addFocusListener(this);

                providerBox.setMaximumRowCount(4);
                GroupLayout gl_providerPanel = new GroupLayout(providerPanel);
                gl_providerPanel.setHorizontalGroup(gl_providerPanel.createParallelGroup(Alignment.LEADING)
                                .addComponent(providerBox, 0, 247, Short.MAX_VALUE));
                gl_providerPanel.setVerticalGroup(gl_providerPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_providerPanel.createSequentialGroup()
                                                .addComponent(providerBox, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
                providerPanel.setLayout(gl_providerPanel);

                lammaSelector = new DatasetSelector();
                

                GroupLayout gl_lammaMetocPanel = new GroupLayout(lammaMetocPanel);
                gl_lammaMetocPanel.setHorizontalGroup(gl_lammaMetocPanel.createParallelGroup(Alignment.LEADING)
                                .addComponent(lammaSelector, 0, 247, Short.MAX_VALUE));
                gl_lammaMetocPanel.setVerticalGroup(gl_lammaMetocPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_lammaMetocPanel.createSequentialGroup()
                                                .addComponent(lammaSelector, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
                lammaMetocPanel.setLayout(gl_lammaMetocPanel);

                GroupLayout groupLayout = new GroupLayout(getContentPane());
                groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(groupLayout.createSequentialGroup().addContainerGap(190, Short.MAX_VALUE)
                                                .addComponent(closeBtn, GroupLayout.PREFERRED_SIZE, 69,
                                                                GroupLayout.PREFERRED_SIZE)
                                                .addGap(3))
                                .addGroup(groupLayout.createSequentialGroup().addComponent(providerPanel).addGap(3))
                                .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(typesPanel, GroupLayout.DEFAULT_SIZE, 259,
                                                                Short.MAX_VALUE)
                                                .addGap(3))
                                .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(warnLimitsPanel, GroupLayout.PREFERRED_SIZE, 259,
                                                                Short.MAX_VALUE)
                                                .addGap(3))
                                .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(localMetocPanel, GroupLayout.PREFERRED_SIZE, 259,
                                                                Short.MAX_VALUE)
                                                .addGap(3))
                                .addGroup(groupLayout.createSequentialGroup().addComponent(lammaMetocPanel).addGap(3))
                                .addGroup(groupLayout
                                                .createSequentialGroup().addComponent(statusPanel,
                                                                GroupLayout.PREFERRED_SIZE, 266, Short.MAX_VALUE)
                                                .addGap(3)));
                groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
                                .createSequentialGroup().addGap(7)
                                .addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(providerPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(typesPanel, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(localMetocPanel, GroupLayout.PREFERRED_SIZE, 85,
                                                GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(lammaMetocPanel, GroupLayout.PREFERRED_SIZE, 85,
                                                GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(warnLimitsPanel, GroupLayout.PREFERRED_SIZE, 111,
                                                GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeBtn).addGap(17)));

                GroupLayout gl_warnLimitsPanel = new GroupLayout(warnLimitsPanel);
                gl_warnLimitsPanel.setHorizontalGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_warnLimitsPanel.createSequentialGroup().addGroup(gl_warnLimitsPanel
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.LEADING)
                                                                .addGroup(gl_warnLimitsPanel.createSequentialGroup()
                                                                                .addContainerGap()
                                                                                .addComponent(windLimitLbl).addGap(41))
                                                                .addGroup(gl_warnLimitsPanel.createSequentialGroup()
                                                                                .addContainerGap()
                                                                                .addComponent(currentLimitLbl,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                100, Short.MAX_VALUE)
                                                                                .addPreferredGap(
                                                                                                ComponentPlacement.RELATED)))
                                                .addGroup(gl_warnLimitsPanel.createSequentialGroup().addContainerGap()
                                                                .addComponent(waveLimitLbl, GroupLayout.PREFERRED_SIZE,
                                                                                119, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)))
                                                .addGroup(gl_warnLimitsPanel
                                                                .createParallelGroup(Alignment.LEADING, false)
                                                                .addComponent(waveLimit, GroupLayout.PREFERRED_SIZE, 45,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(currentLimit, 0, 0, Short.MAX_VALUE)
                                                                .addComponent(windLimit, 0, 0, Short.MAX_VALUE))
                                                .addGap(78)));
                gl_warnLimitsPanel.setVerticalGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_warnLimitsPanel.createSequentialGroup()
                                                .addGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(windLimitLbl).addComponent(windLimit,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(currentLimit, GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(currentLimitLbl))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_warnLimitsPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(waveLimit, GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(waveLimitLbl))
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
                warnLimitsPanel.setLayout(gl_warnLimitsPanel);

                GroupLayout gl_typesPanel = new GroupLayout(typesPanel);
                gl_typesPanel.setHorizontalGroup(gl_typesPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_typesPanel.createSequentialGroup().addGroup(gl_typesPanel
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(gl_typesPanel.createSequentialGroup()
                                                                .addComponent(windCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(currentCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_typesPanel.createSequentialGroup()
                                                                .addComponent(wavesCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(seaLevelCb, GroupLayout.PREFERRED_SIZE,
                                                                                88, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(densityCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap(72, Short.MAX_VALUE)));
                gl_typesPanel.setVerticalGroup(gl_typesPanel.createParallelGroup(Alignment.TRAILING)
                                .addGroup(Alignment.LEADING, gl_typesPanel.createSequentialGroup()
                                                .addGroup(gl_typesPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(windCb).addComponent(currentCb))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_typesPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(wavesCb).addComponent(seaLevelCb))
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(densityCb)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
                typesPanel.setLayout(gl_typesPanel);

                btnSelectFile = new JButton("Metoc");
                btnSelectFile.addActionListener(this);
                lblMetocLocal = new JLabel("Select a metoc file!");
                btnSelectPartFile = new JButton("Wave Part.");
                btnSelectPartFile.addActionListener(this);
                lblMetocLocalWavePart = new JLabel("Optional Wave partitions file");
                GroupLayout gl_localMetocPanel = new GroupLayout(localMetocPanel);
                gl_localMetocPanel.setHorizontalGroup(gl_localMetocPanel.createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_localMetocPanel.createSequentialGroup().addGroup(gl_localMetocPanel
                                                .createParallelGroup(Alignment.LEADING, false)
                                                .addGroup(gl_localMetocPanel.createSequentialGroup()
                                                                .addComponent(uvTuCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(fromToCb, GroupLayout.PREFERRED_SIZE, 88,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                .addComponent(btnSelectFile, GroupLayout.PREFERRED_SIZE,
                                                                                60, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(btnSelectPartFile,
                                                                                GroupLayout.PREFERRED_SIZE, 70,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_localMetocPanel.createSequentialGroup().addGap(5)
                                                                .addGroup(gl_localMetocPanel
                                                                                .createParallelGroup(Alignment.LEADING,
                                                                                                false)
                                                                                .addComponent(lblMetocLocal,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                248,
                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(lblMetocLocalWavePart,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                248,
                                                                                                GroupLayout.PREFERRED_SIZE))))
                                                .addContainerGap()));
                gl_localMetocPanel.setVerticalGroup(gl_localMetocPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_localMetocPanel.createSequentialGroup().addGroup(gl_localMetocPanel
                                                .createParallelGroup(Alignment.LEADING).addComponent(uvTuCb)
                                                .addGroup(gl_localMetocPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(fromToCb).addComponent(btnSelectFile)
                                                                .addComponent(btnSelectPartFile)))
                                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)
                                                .addComponent(lblMetocLocal, GroupLayout.PREFERRED_SIZE, 15,
                                                                GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)
                                                .addComponent(lblMetocLocalWavePart, GroupLayout.PREFERRED_SIZE, 15,
                                                                GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap()));
                localMetocPanel.setLayout(gl_localMetocPanel);
                localMetocPanel.setEnabled(false);

                chckbxShowRawRequest = new JCheckBox("Show Raw Request (debug)");

                GroupLayout gl_statusPanel = new GroupLayout(statusPanel);
                gl_statusPanel.setHorizontalGroup(gl_statusPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_statusPanel.createSequentialGroup().addGroup(gl_statusPanel
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(chckbxShowRawRequest)
                                                .addGroup(gl_statusPanel.createSequentialGroup().addGap(63)
                                                                .addComponent(requestBtn))
                                                .addGroup(gl_statusPanel.createSequentialGroup()
                                                                .addComponent(intervalLbl)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(intervalDb, GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_statusPanel.createSequentialGroup()
                                                                .addComponent(currentLabel)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(currentMetocDataLbl))
                                                .addComponent(showCheckbox)).addContainerGap(119, Short.MAX_VALUE)));
                gl_statusPanel.setVerticalGroup(gl_statusPanel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_statusPanel.createSequentialGroup().addComponent(showCheckbox)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(chckbxShowRawRequest)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_statusPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(intervalLbl).addComponent(intervalDb,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addGroup(gl_statusPanel.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(currentLabel)
                                                                .addComponent(currentMetocDataLbl))
                                                .addGap(18).addComponent(requestBtn).addContainerGap()));
                statusPanel.setLayout(gl_statusPanel);
                getContentPane().setLayout(groupLayout);

                addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowOpened(WindowEvent e) {
                                closeBtn.requestFocus();
                        }
                });

                addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                                saveValues();
                        }
                });

        }

        @Override
        public void onCatalogChanged(OpenDapCatalogEvent e) {
                List<Dataset> ds = e.getDatasetList();
                String metocFile = getRouteMetocSettings().getLammaMetocFile();
                if(lammaSelector != null && ds != null) {
                        lammaSelector.addDatasets(ds);
                        if(metocFile != null) {
                                lammaSelector.selectByName(metocFile);
                        }
                
                }
                checkStatus();
    }
}
