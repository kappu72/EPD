/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.epd.shore.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import dk.dma.epd.common.prototype.gui.ComponentFrame;
import dk.dma.epd.common.prototype.gui.settings.BaseSettingsPanel;
import dk.dma.epd.common.prototype.gui.settings.ISettingsListener;
import dk.dma.epd.common.prototype.layers.wms.SingleWMSService;
import dk.dma.epd.shore.EPDShore;
import dk.dma.epd.shore.ais.AisHandler;
import dk.dma.epd.shore.event.ToolbarMoveMouseListener;
import dk.dma.epd.shore.gui.settingtabs.AisSettingsPanel;
import dk.dma.epd.shore.gui.settingtabs.ConnectionStatus;
import dk.dma.epd.shore.gui.settingtabs.ENavSettingsPanel;
import dk.dma.epd.shore.gui.settingtabs.GuiStyler;
import dk.dma.epd.shore.gui.settingtabs.MapSettingsPanel;
import dk.dma.epd.shore.gui.settingtabs.MapWindowsPanel;
import dk.dma.epd.shore.gui.settingtabs.ShoreCloudSettingsPanel;
import dk.dma.epd.shore.services.shore.ShoreServices;
import dk.dma.epd.shore.settings.EPDSettings;

/**
 * The main {@code EPDShore} settings frame
 */
public class JSettingsWindow extends ComponentFrame implements MouseListener, ISettingsListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(JSettingsWindow.class);

    private Timer timer = new Timer(500, new ActionListener() {        
        @Override public void actionPerformed(ActionEvent e) {
            checkSettingsChanged();
        }});
    
    private JPanel backgroundPane;

    Border paddingLeft = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(65, 65, 65));
    Border paddingBottom = BorderFactory.createMatteBorder(0, 0, 5, 0, new Color(83, 83, 83));
    Border notificationPadding = BorderFactory.createCompoundBorder(paddingBottom, paddingLeft);
    Border notificationsIndicatorImportant = BorderFactory.createMatteBorder(0, 0, 0, 10, new Color(206, 120, 120));
    Border paddingLeftPressed = BorderFactory.createMatteBorder(0, 8, 0, 0, new Color(45, 45, 45));
    Border notificationPaddingPressed = BorderFactory.createCompoundBorder(paddingBottom, paddingLeftPressed);

    Font defaultFont = new Font("Arial", Font.PLAIN, 11);
    Color textColor = new Color(237, 237, 237);

    private JLabel breadCrumps;

    private MapSettingsPanel mapSettingsPanel = new MapSettingsPanel();
    private MapWindowsPanel mapWindowsPanel = new MapWindowsPanel();
    private ConnectionStatus connectionsPanel = new ConnectionStatus();
    private AisSettingsPanel aisSettingsPanel = new AisSettingsPanel();
    private ENavSettingsPanel eNavSettingsPanel = new ENavSettingsPanel();
    private ShoreCloudSettingsPanel cloudSettingsPanel = new ShoreCloudSettingsPanel();

    // Settings panels
    private List<BaseSettingsPanel> settingsPanels = new ArrayList<>();
    private Map<BaseSettingsPanel, JLabel> settingsLabels = new HashMap<>();

    private JPanel contentPane;

    private JPanel labelContainer;

    private JLabel ok;
    private JLabel cancel;

    MouseMotionListener[] actions;
    private JLabel moveHandler;
    private JPanel masterPanel;
    private JPanel mapPanel;
    private static int moveHandlerHeight = 18;
    public int width;
    public int height;
    private MainFrame mainFrame;
    private EPDSettings settings;

    /**
     * Constructor.
     * Create the frame.
     */
    public JSettingsWindow() {
        super("Settings Window", false, true, false, false);

        settings = EPDShore.getInstance().getSettings();

        registerSettings(mapSettingsPanel, mapWindowsPanel, connectionsPanel, 
                aisSettingsPanel, eNavSettingsPanel, cloudSettingsPanel);

        // NB: Setting up the GUI is postponed until the mainFrame
        //     has been set via findAndInit...
        
    }
    
    /**
     * Register the given settings panels
     * @param settingsPanels the settings panels to register
     */
    private void registerSettings(BaseSettingsPanel... panels) {
        for (BaseSettingsPanel settingsPanel : panels) {
            settingsPanel.setVisible(false);
            createLabel(settingsPanel, settingsLabels);
            settingsPanels.add(settingsPanel);
            settingsPanel.addListener(this);
        }
    }
    
    /**
     * Activates the given panel 
     * @param settingsPanel the panel to activate
     */
    private void activateSettingsPanel(BaseSettingsPanel settingsPanel) {
        LOG.info("Activating panel: " + settingsPanel.getName());

        for (BaseSettingsPanel panel : settingsPanels) {
            if (panel == settingsPanel) {
                panel.setVisible(true);
                settingsLabels.get(panel).setBackground(new Color(55, 55, 55));
                breadCrumps.setText("Preferences > " + panel.getName());
            } else {
                panel.setVisible(false);                
                settingsLabels.get(panel).setBackground(new Color(65, 65, 65));
            }
        }
    }
    
    /**
     * Function for setting up custom GUI for the map frame
     */
    public void initGUI() {

        setSize(800, 600);
        setLocation(10, 10);


        setResizable(false);
        setTitle("Preferences");
        setBounds(100, 100, 661, 481);
        
        // Background pane
        backgroundPane = new JPanel();

        backgroundPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("653px:grow"), }, new RowSpec[] {
                FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("23px:grow"), RowSpec.decode("428px:grow"), }));

        backgroundPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(70, 70, 70)));
        backgroundPane.add(topPanel, "1, 2, fill, fill");
        topPanel.setLayout(null);
        topPanel.setBackground(GuiStyler.backgroundColor);

        // Bread crumps
        breadCrumps = new JLabel("Preferences > Map Settings");
        GuiStyler.styleText(breadCrumps);

        breadCrumps.setBounds(10, 4, 603, 14);
        breadCrumps.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(breadCrumps);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel();
        backgroundPane.add(bottomPanel, "1, 3, fill, fill");
        bottomPanel.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(0, 0, 140, 428);
        scrollPane.setBorder(null);
        bottomPanel.add(scrollPane);

        // Menu panel
        JPanel menuPanel = new JPanel();
        scrollPane.setViewportView(menuPanel);
        menuPanel.setBackground(GuiStyler.backgroundColor);
        menuPanel.setLayout(null);

        // Label container
        labelContainer = new JPanel();
        labelContainer.setLocation(0, 0);
        labelContainer.setBackground(GuiStyler.backgroundColor);
        labelContainer.setSize(new Dimension(140, 500));

        menuPanel.add(labelContainer);

        // Content pane
        contentPane = new JPanel();
        contentPane.setBorder(new MatteBorder(0, 1, 0, 0, new Color(70, 70, 70)));
        contentPane.setBounds(140, 0, 513, 428);
        bottomPanel.add(contentPane);
        contentPane.setBackground(GuiStyler.backgroundColor);
        contentPane.setLayout(null);

        // OK button
        ok = new JLabel("OK", EPDShore.res().getCachedImageIcon("images/buttons/ok.png"), SwingConstants.CENTER);
        ok.setBounds(335, 390, 75, 20);
        GuiStyler.styleButton(ok);
        contentPane.add(ok);

        // Cancel button
        cancel = new JLabel("CANCEL", EPDShore.res().getCachedImageIcon("images/buttons/cancel.png"), SwingConstants.CENTER);
        GuiStyler.styleButton(cancel);
        cancel.setBounds(417, 390, 75, 20);
        contentPane.add(cancel);

        // Add the settings panels
        for (BaseSettingsPanel settingsPanel : settingsPanels) {
            contentPane.add(settingsPanel);
            labelContainer.add(settingsLabels.get(settingsPanel));
        }
        activateSettingsPanel(mapSettingsPanel);

        // Strip off
        setRootPaneCheckingEnabled(false);
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Map tools
        mapPanel = new JPanel(new GridLayout(1, 3));
        mapPanel.setPreferredSize(new Dimension(500, moveHandlerHeight));
        mapPanel.setOpaque(true);
        mapPanel.setBackground(Color.DARK_GRAY);
        mapPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 30)));

        ToolbarMoveMouseListener mml = new ToolbarMoveMouseListener(this, mainFrame);
        mapPanel.addMouseListener(mml);
        mapPanel.addMouseMotionListener(mml);

        // Placeholder - for now
        mapPanel.add(new JLabel());

        // Movehandler/Title dragable)
        moveHandler = new JLabel("Preferences", SwingConstants.CENTER);
        moveHandler.setFont(new Font("Arial", Font.BOLD, 9));
        moveHandler.setForeground(new Color(200, 200, 200));
        moveHandler.addMouseListener(this);
        moveHandler.addMouseListener(mml);
        moveHandler.addMouseMotionListener(mml);
        actions = moveHandler.getListeners(MouseMotionListener.class);
        mapPanel.add(moveHandler);

        // The tools (minimize, maximize and close)
        JPanel mapToolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        mapToolsPanel.setOpaque(false);
        mapToolsPanel.setPreferredSize(new Dimension(60, 50));

        JLabel close = new JLabel(EPDShore.res().getCachedImageIcon("images/window/close.png"));
        close.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                close(false);
            }

        });
        close.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 2));
        mapToolsPanel.add(close);
        mapPanel.add(mapToolsPanel);

        // Create the masterpanel for aligning
        masterPanel = new JPanel(new BorderLayout());
        masterPanel.add(mapPanel, BorderLayout.NORTH);
        masterPanel.add(backgroundPane, BorderLayout.SOUTH);
        masterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, new Color(30, 30, 30), new Color(
                45, 45, 45)));

        this.setContentPane(masterPanel);

        // Hook up listeners
        for (BaseSettingsPanel settingsPanel : settingsPanels) {
            final BaseSettingsPanel panel = settingsPanel;
            settingsLabels.get(settingsPanel).addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    activateSettingsPanel(panel);
                }
            });
        }
        
        ok.addMouseListener(this);
        cancel.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    /**
     * Called when OK and Cancel have been clicked
     * @param evt the mouse event
     */
    @Override
    public void mouseReleased(MouseEvent evt) {

        // OK button clicked
        if (evt.getSource() == ok && ok.isEnabled()) {
            // Close and save
            close(true);
        }
        
        // Cancel button clicked
        else if (evt.getSource() == cancel) {
            // Close but do not save
            close(false);
        }
    }

    /**
     * Called when a new bean is set at the context
     */
    @Override
    public void findAndInit(Object obj) {
        if (obj instanceof MainFrame) {
            mainFrame = (MainFrame)obj;
            initGUI();
            
        } else if (obj instanceof AisHandler) {
            // aisHandler = (AisHandlerCommon) obj;
            connectionsPanel.addStatusComponent((AisHandler) obj);
            
        } else if (obj instanceof ShoreServices) {
            // shoreServices = (ShoreServicesCommon) obj;
            connectionsPanel.addStatusComponent((ShoreServices) obj);
            
        } else if (obj instanceof SingleWMSService) {
            // System.out.println("wmsService");
            // shoreServices = (ShoreServicesCommon) obj;
            connectionsPanel.addStatusComponent((SingleWMSService) obj);
        }
    }

    /**
     * Change the visibility
     */
    public void toggleVisibility() {
        if (isVisible()) {
            close(false);
        } else {
            open();
        }
    }

    /**
     * Opens the settings panel
     */
    public void open() {
        // Is it already open
        if (isVisible()) {
            return;
        }
        
        // Load settings and make the panel visible
        for (BaseSettingsPanel settingsPanel : settingsPanels) {
            settingsPanel.loadSettings();
        }
        activateSettingsPanel(mapSettingsPanel);
        setVisible(true);
        timer.start();
    }
    
    /**
     * Closes the settings panel and optionally saves the updated settings
     * @param save whether to save the settings or not
     */
    public void close(boolean save) {
        // Is it already closed
        if (!isVisible()) {
            return;
        }

        if (save) {
            // Save settings and make the panel visible
            for (BaseSettingsPanel settingsPanel : settingsPanels) {
                settingsPanel.saveSettings();
            }
            settings.saveToFile();
        }
        setVisible(false);
        timer.stop();
    }
    
    /**
     * Creates a new JLabel for the given settings panel
     */
    private JLabel createLabel(BaseSettingsPanel settingsPanel, Map<BaseSettingsPanel, JLabel> labelMap) {
        JLabel label = new JLabel(
                settingsPanel.getName(), 
                settingsPanel.getIcon(), 
                SwingConstants.LEFT);
        GuiStyler.styleTabButton(label);
        labelMap.put(settingsPanel, label);
        return label;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void settingsChanged(Type type) {
        EPDShore.getInstance().settingsChanged(type);
    }
    
    /**
     * Checks if the settings have changed and update the OK button enabled state
     */
    private void checkSettingsChanged() {
        if (!isVisible()) {
            return;
        }
        boolean changed = false;
        for (BaseSettingsPanel settingsPanel : settingsPanels) {
            changed |= settingsPanel.settingsChanged();
        }
        ok.setEnabled(changed);
    }
}
