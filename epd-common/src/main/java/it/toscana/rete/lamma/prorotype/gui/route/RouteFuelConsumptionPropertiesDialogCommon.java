package it.toscana.rete.lamma.prorotype.gui.route;


import static dk.dma.epd.common.graphics.GraphicsUtil.fixSize;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.WEST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.FormatException;
import dk.dma.epd.common.Heading;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.route.LockTableCell;
import dk.dma.epd.common.prototype.gui.views.ChartPanelCommon;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.sensor.pnt.PntTime;
import dk.dma.epd.common.text.Formatter;
import dk.dma.epd.common.util.ParseUtils;
import dk.dma.epd.common.util.TypedValue.Time;
import dk.dma.epd.common.util.TypedValue.TimeType;
import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipConfigurationsSelector;
import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipPropulsionConfigurationsSelector;
import it.toscana.rete.lamma.prorotype.gui.shipsdata.ShipsSelector;
import it.toscana.rete.lamma.prorotype.model.RouteFuelConsumptionSettings;
import it.toscana.rete.lamma.prorotype.model.ShipConfiguration;
import it.toscana.rete.lamma.prorotype.model.ShipData;
import it.toscana.rete.lamma.utils.Utils;


/**
* Dialog used for viewing and editing route fuel consumption properties
* Freely inspired by RoutePropertiedDialogCommon
*/
public class RouteFuelConsumptionPropertiesDialogCommon extends JDialog implements ActionListener, ListSelectionListener, ItemListener {

   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(RouteFuelConsumptionPropertiesDialogCommon.class);
   
   private static final String[] COL_NAMES = {
           " ", "Name", "Latutide", "Longtitude", 
           "TTG", "ETA", 
           "RNG", "BRG", "Head.", "SOG", "PS.CFG."
            };

   private static final int[] COL_MIN_WIDTHS = {
       25, 60, 70, 70,
       70, 70,
       70, 50, 40, 50,
       60
   };
   
   

   private Window parent;
   private ChartPanelCommon chartPanel;
   protected Route route = new Route();
   protected boolean[] locked;
   protected boolean readOnlyRoute;
   boolean quiescent;
   protected List<RouteChangeListener> listeners = new CopyOnWriteArrayList<>();


   // Column 1 widgets
   private JTextField nameTxT = new JTextField();
   private JTextField etdTxT = new JTextField();
   private JTextField etaTxT = new JTextField();
   private JTextField distanceTxT = new JTextField();
   private JTextField inrouteTxT = new JTextField();
   private JTextField totalFuel = new JTextField();

   // Column 2 widgets
   // Fuel configurations settings
   protected ShipsSelector shipsSelector = new ShipsSelector(new ArrayList<ShipData>());
   protected ShipConfigurationsSelector configurationsSelector = new ShipConfigurationsSelector(new ArrayList());
   protected ShipPropulsionConfigurationsSelector propConfigsSelector = new ShipPropulsionConfigurationsSelector(new ArrayList());
   protected JCheckBox waveComponents = new JCheckBox("Wave Components");
   protected JCheckBox fromToMetoc = new JCheckBox("From/To Metoc");
   protected JCheckBox skipWind = new JCheckBox("Skip Wind");
   protected JCheckBox skipWave = new JCheckBox("Skip Wave");
   protected JCheckBox skipCurrent = new JCheckBox("Skip Current");
   
   // Route details table
   private DefaultTableModel routeTableModel;    
   private JTable routeDetailTable;
   private int selectedWp = -1;
   
   // Button panel
   private JButton btnZoomToRoute = new JButton("Zoom to Route");
   private JButton btnZoomToWp = new JButton("Zoom to Way Point");
   protected JButton btnActivate = new JButton("Activate");
   private JButton btnClose = new JButton("Close");
   private JCheckBox cbVisible = new JCheckBox("Visible");
   

   
   /**
    * Constructor
    * 
    * @param parent the parent window
    * @param chartPanel the chart panel
    * @param routeId the route index
    */
   public RouteFuelConsumptionPropertiesDialogCommon(Window parent, ChartPanelCommon chartPanel, int routeId) {
       this(parent, 
            chartPanel, 
            EPD.getInstance().getRouteManager().getRoute(routeId), 
            EPD.getInstance().getRouteManager().isActiveRoute(routeId));
       
       try {
           setOpacity((float) 0.95);
       } catch (Exception E) {
           System.out.println("Failed to set opacity, ignore");
       }

   }
   
   /**
    * Constructor
    * 
    * @param parent the parent window
    * @param route the route
    * @param readOnlyRoute whether the route is read-only or not
    * @wbp.parser.constructor
    */
   public RouteFuelConsumptionPropertiesDialogCommon(Window parent, ChartPanelCommon chartPanel, Route route, boolean readOnlyRoute) {
       super(parent, "Route Fuel Consumption Properties", Dialog.ModalityType.APPLICATION_MODAL);
       
       this.parent = parent;
       this.chartPanel = chartPanel;
       this.route = route;
       this.readOnlyRoute = false;
       locked = new boolean[route.getWaypoints().size()];
       
       addWindowListener(new WindowAdapter() {
           @Override public void windowClosed(WindowEvent e) {
               EPD.getInstance().getRouteManager().validateMetoc(RouteFuelConsumptionPropertiesDialogCommon.this.route);
           }});
       
       initGui();
       initValues();
       
       setBounds(100, 100, 1000, 450);
       setLocationRelativeTo(parent);
   }
   
   /***************************************************/
   /** UI initialization                             **/
   /***************************************************/
   
   /**
    * Initializes the user interface
    */
   private void initGui() {
       Insets insets1  = new Insets(5, 5, 0, 5);
       Insets insets2  = new Insets(5, 25, 0, 5);
       Insets insets3  = new Insets(5, 5, 0, 0);
       Insets insets4  = new Insets(5, 0, 0, 5);
       Insets insets5  = new Insets(5, 5, 5, 5);
       Insets insets6  = new Insets(5, 25, 5, 5);
       Insets insets10  = new Insets(10, 10, 10, 10);
       
       JPanel content = new JPanel(new GridBagLayout());
       getContentPane().add(content);
       
       // Properties container
       JPanel prosContainer = new JPanel(new GridBagLayout());

       content.add(prosContainer, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, insets10, 0, 0));
       
       
       // ********************************
       // ** Route properties panel
       // ********************************
       
       
       
       
       
       JPanel routeProps = new JPanel(new GridBagLayout());
       routeProps.setBorder(new TitledBorder(new LineBorder(Color.black), "Route Properties"));
       prosContainer.add(routeProps, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, insets10, 0, 0));       
       
       
       JPanel fcProps = new JPanel(new GridBagLayout());
       fcProps.setBorder(new TitledBorder(new LineBorder(Color.black), "Fuel Consumption Settings"));
       prosContainer.add(fcProps, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, insets10, 0, 0));     
       
       // Column 1 widgets
       int gridY = 0;
       nameTxT.setEditable(false);
       routeProps.add(new JLabel("Name:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(nameTxT, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
         
       etdTxT.setEditable(false);
       routeProps.add(new JLabel("Estimated Time of Departure:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(etdTxT, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       etaTxT.setEnabled(false);
       routeProps.add(new JLabel("Estimated Time of Arrival:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(etaTxT, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));

       
       distanceTxT.setEditable(false);
       routeProps.add(new JLabel("Total Distance:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(distanceTxT, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       
       inrouteTxT.setEditable(false);
       routeProps.add(new JLabel("Estimated Time in-route:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(inrouteTxT, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       totalFuel.setEditable(false);
       routeProps.add(new JLabel("Total Fuel Consumprion:"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       routeProps.add(fixSize(totalFuel, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       // Fuel consumption properties
       gridY = 0;
       
       fcProps.add(new JLabel("Ship"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(fixSize(shipsSelector, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));

       fcProps.add(new JLabel("Sail Configuration"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(fixSize(configurationsSelector, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));

       fcProps.add(new JLabel("Propulsion Configuration"), new GridBagConstraints(0, gridY, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(fixSize(propConfigsSelector, 120), new GridBagConstraints(1, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       // TODO:: not yet implemented remove after implementation	
       waveComponents.setEnabled(false);
       skipCurrent.setEnabled(false);
       skipWave.setEnabled(false);
       skipWind.setEnabled(false);
       
       gridY = 0;
       fcProps.add(waveComponents,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(fromToMetoc,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(skipWind,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(skipWave,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       fcProps.add(skipCurrent,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, NONE, insets1, 0, 0));
       
       
       routeProps.add(new JLabel(""), new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets2, 0, 0));
       fcProps.add(new JLabel(""), new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets2, 0, 0));
       
       
       // ********************************
       // ** Route fc panel
       // ********************************
       JTextField defaultCellEditor = new JTextField();
       
       routeTableModel = createRouteTableModel();
       routeDetailTable = new JTable(routeTableModel);
       routeDetailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       routeDetailTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
       routeDetailTable.setDefaultEditor(Object.class, new DefaultCellEditor(defaultCellEditor));
       routeDetailTable.setIntercellSpacing(new Dimension(0, 1));
       routeDetailTable.setShowHorizontalLines(false);
       routeDetailTable.setShowVerticalLines(false);
       routeDetailTable.setFillsViewportHeight(true);
       routeDetailTable.getSelectionModel().addListSelectionListener(this);
       
       
       routeDetailTable.getTableHeader().setReorderingAllowed(false);
       JScrollPane scrollPane = new JScrollPane(routeDetailTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       routeDetailTable.setFont(routeDetailTable.getFont().deriveFont(10.0f));


       // Set the minimum column widths
       for (int x = 0; x < COL_MIN_WIDTHS.length; x++) {
           routeDetailTable.getColumnModel().getColumn(x).setMinWidth(COL_MIN_WIDTHS[x]);
       }
       
       // Configure lock column
       TableColumn col = routeDetailTable.getColumnModel().getColumn(0);
       col.setWidth(COL_MIN_WIDTHS[0]);
       col.setMaxWidth(COL_MIN_WIDTHS[0]);
       col.setMinWidth(COL_MIN_WIDTHS[0]);
       
       col.setCellRenderer(new LockTableCell.CustomBooleanCellRenderer());
       col.setCellEditor(new LockTableCell.CustomBooleanCellEditor());
       
       

       // Configure heading column
       JComboBox<Heading> headingCombo = new JComboBox<>(Heading.values());
       headingCombo.setFont(headingCombo.getFont().deriveFont(10.0f));
       routeDetailTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(headingCombo));

       JPanel routeTablePanel = new JPanel(new BorderLayout());
       routeTablePanel.add(scrollPane, BorderLayout.CENTER);
       routeTablePanel.setBorder(new TitledBorder(new LineBorder(Color.black), "Route Details"));
       content.add(routeTablePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, NORTHWEST, BOTH, insets10, 0, 0));
       
       
       // ********************************
       // ** Button panel
       // ********************************
       
       JPanel btnPanel = new JPanel(new GridBagLayout());
       content.add(btnPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL, insets10, 0, 0));
       
       
       shipsSelector.addActionListener(this);
       configurationsSelector.addActionListener(this);
       propConfigsSelector.addActionListener(this);
       btnZoomToRoute.addActionListener(this);
       btnZoomToWp.addActionListener(this);
       
       skipWave.addItemListener(this);
       skipWind.addItemListener(this);
       skipCurrent.addItemListener(this);
       fromToMetoc.addItemListener(this);
       waveComponents.addItemListener(this);
       
       
       
       
       btnActivate.addActionListener(this);
       btnClose.addActionListener(this);
       cbVisible.addActionListener(this);
       getRootPane().setDefaultButton(btnClose);
       btnPanel.add(btnZoomToRoute, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
       btnPanel.add(btnZoomToWp, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
       
       btnPanel.add(btnActivate, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
       btnPanel.add(cbVisible, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, WEST, NONE, insets5, 0, 0));
       btnPanel.add(btnClose, new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0, EAST, NONE, insets5, 0, 0));
   }
   
   
   /**
    * Initializes the table model used for route details
    * 
    * @return the table model
    */
   private DefaultTableModel createRouteTableModel() {
       return new DefaultTableModel() {
           private static final long serialVersionUID = 1L;

           @Override
           public int getRowCount() {
               return route.getWaypoints().size();
           }

           @Override
           public int getColumnCount() {
               return COL_NAMES.length;
           }

           @Override
           public String getColumnName(int columnIndex) {
               return COL_NAMES[columnIndex];
           }

           @Override
           public Object getValueAt(int rowIndex, int columnIndex) {
               RouteWaypoint wp = route.getWaypoints().get(rowIndex);
               Boolean isLastRow = wp.getOutLeg() == null;
               
               switch (columnIndex) {
	               case  0: return locked[rowIndex];
	               case  1: return wp.getName();
	               case  2: return wp.getPos().getLatitudeAsString();
	               case  3: return wp.getPos().getLongitudeAsString();
	               case  4: return Formatter.formatTime(route.getWpTtg(rowIndex));
	               case  5: return Formatter.formatShortDateTimeNoTz(route.getWpEta(rowIndex));
	               case  6: return Formatter.formatDistNM(route.getWpRng(rowIndex));
	               case  7: return Formatter.formatDegrees(route.getWpBrg(wp), 2);
	               case  8: return isLastRow? "N/A" : wp.getHeading();
	               case  9: return Formatter.formatSpeed(isLastRow ? null : wp.getOutLeg().getSpeed());
	               case 10: return isLastRow? "N/A" : Formatter.formatPropulsion(wp.getOutLeg().getPropulsionConfig());
	               default: return null;
               }
           }

           @Override
           public void setValueAt(Object value, int rowIndex, int columnIndex) {
               try {
                   RouteWaypoint wp = route.getWaypoints().get(rowIndex);
                   switch (columnIndex) {
                   case  0: 
                       locked[rowIndex] = (Boolean) value;
                       checkLockedRows();
                       fireTableRowsUpdated(rowIndex, rowIndex); 
                       break;
                  default:
                   }
                   routeUpdated();
               } catch (Exception ex) {
                   LOG.warn(String.format(
                           "Failed updating field '%s' in row %d: %s", COL_NAMES[columnIndex], rowIndex, ex.getMessage()));
                   JOptionPane.showMessageDialog(RouteFuelConsumptionPropertiesDialogCommon.this, "Input error: " + ex.getMessage(), "Input error", JOptionPane.ERROR_MESSAGE);
               }
           }

           @Override
           public boolean isCellEditable(int rowIndex, int columnIndex) {
               return !readOnlyRoute && columnIndex == 0;
           }   
       };
   }
   
   
   /**
    * Updates the dialog with the value of the current route
    */
   private void initValues() {
	   RouteFuelConsumptionSettings fcSet = route.getRouteFCSettings();
	   if(fcSet == null) {
		   fcSet = new RouteFuelConsumptionSettings();
		   route.setRouteFCSettings(fcSet);
	   }
       // Should not trigger listeners
       quiescent = true;
       
       nameTxT.setText(route.getName());
       
       etdTxT.setText(Formatter.formatLongDateTimeNoTz(route.getStarttime()));
       etaTxT.setText(Formatter.formatLongDateTimeNoTz(route.getEta()));
       
       inrouteTxT.setText(Formatter.formatTime(route.getRouteTtg()));
       distanceTxT.setText(Formatter.formatDistNM(route.getRouteDtg()));
       // Initialize ship selector
       shipsSelector.addShips(Utils.getShips().stream().filter(ship -> ship.isShipValid()).collect(Collectors.toList()));
       shipsSelector.setSelectedIndex(-1);
       ShipData ship = null;
       if( fcSet.getShip() != null) {
    	   ship = shipsSelector.selectByName(fcSet.getShip());
       }
       // Initialize configuration selector and propulsion selector
       if(ship != null) {
    	   configurationsSelector.addConfigurations(ship.getValidShipConfigurations());
    	   configurationsSelector.setSelectedIndex(-1);   
    	   if(fcSet.getConfiguration() != null) {
    			   configurationsSelector.setSelectedItem(ship.serachConfigurationByName(fcSet.getConfiguration().getName()));
    			   propConfigsSelector.addConfigurations(fcSet.getConfiguration().getPropulsions());
    			   propConfigsSelector.setSelectedIndex(-1);
    		   }
    	}
       waveComponents.setSelected(fcSet.isWaveComponents());
       fromToMetoc.setSelected(fcSet.isFromToMetoc());
       skipCurrent.setSelected(fcSet.isSkipCurrent());
       skipWave.setSelected(fcSet.isSkipWave());
       skipWind.setSelected(fcSet.isSkipWind());
       	
       cbVisible.setSelected(route.isVisible());
       
       if (route.getWaypoints().size() > 1) {
    	   propConfigsSelector.setSelectedItem(route.getWaypoints().get(0).getOutLeg().getPropulsionConfig());
       }
       
       updateButtonEnabledState();
       
       // Done
       quiescent = false;
   }
   
   /** 
    * Updates the enabled state of the buttons
    */
   private void updateButtonEnabledState() {
       boolean wpSelected = selectedWp >= 0;
       btnActivate.setEnabled(wpSelected && readOnlyRoute);
       btnZoomToWp.setEnabled(wpSelected && chartPanel != null);
       btnZoomToRoute.setEnabled(chartPanel != null);
       
       boolean allRowsLocked = checkLockedRows();
   }
   
   /***************************************************/
   /** UI listener events                            **/
   /***************************************************/
   
   /**
    * Called when the table selection changes
    * @param evt the event
    */
   @Override 
   public void valueChanged(ListSelectionEvent evt) {
       // Check if we are in a quiescent state
       if (quiescent) {
           return;
       }
       
       if (!evt.getValueIsAdjusting()) {
           selectedWp = routeDetailTable.getSelectedRow();
           updateButtonEnabledState();
       }
   }
   
   /**
    * Handle action events
    * @param evt the action event
    */
   @Override
   public void actionPerformed(ActionEvent evt) {
       // Check if we are in a quiescent state
       if (quiescent) {
           return;
       }
       
       if (evt.getSource() == btnZoomToRoute && chartPanel != null) {
           chartPanel.zoomToWaypoints(route.getWaypoints());
       
       } else if (evt.getSource() == btnZoomToWp && chartPanel != null) {
           chartPanel.goToPosition(route.getWaypoints().get(selectedWp).getPos());
           
       } else if (evt.getSource() == btnActivate) {
           EPD.getInstance().getRouteManager().changeActiveWp(selectedWp);
           routeUpdated();
       
       } else if (evt.getSource() == btnClose) {
           dispose();
       
       } else if (evt.getSource() == cbVisible) {
           route.setVisible(cbVisible.isSelected());
           
           EPD.getInstance().getRouteManager()
               .notifyListeners(RoutesUpdateEvent.ROUTE_VISIBILITY_CHANGED);
           
       }else if(evt.getSource() == shipsSelector) {
    	   	ShipData ship = (ShipData) shipsSelector.getSelectedItem();
			if(ship.getShipName().equals(route.getRouteFCSettings().getShip()))
				return;
			else {
				route.getRouteFCSettings().setShip(ship.getShipName());
				configurationsSelector.addConfigurations(ship.getValidShipConfigurations());
				return; 
				// solo aggiornamento propulsion serializza si potrebbe fare diversamente sospendento update altrimenti chiama tre volte
				
			}
       }else if(evt.getSource() == configurationsSelector) {
			ShipConfiguration config = (ShipConfiguration) configurationsSelector.getSelectedItem();
			if(config == null) {
				return;
			}
			if( config.equals(route.getRouteFCSettings().getConfiguration()) ) {
				return;
			}
			else {
				route.getRouteFCSettings().setConfiguration(config);
				propConfigsSelector.addConfigurations(config.getPropulsions());
				return;
			}
      }else if(evt.getSource() == propConfigsSelector) {
    	  
    	  Path p = (Path) propConfigsSelector.getSelectedItem();
    	  updateLegPropulsionConfig(p);
    	  // TODO lancia calcolo se tutto ok con la configurazione;
      }
       
       EPD.getInstance().getRouteManager()
       .notifyListeners(RoutesUpdateEvent.ROUTE_CHANGED);
   }
   @Override
   public void itemStateChanged(ItemEvent e) {
   	Object s= e.getSource();
   	RouteFuelConsumptionSettings fcCfg = route.getRouteFCSettings();
   	if(s.equals(waveComponents)) 
   		fcCfg.setWaveComponents(waveComponents.isSelected());	
   	else if (s.equals(fromToMetoc))
   		fcCfg.setFromToMetoc(fromToMetoc.isSelected());
   	else if (s.equals(skipCurrent))
   		fcCfg.setSkipCurrent(skipCurrent.isSelected());
   	else if (s.equals(skipWave))
   		fcCfg.setSkipWave(skipWave.isSelected());
   	else if (s.equals(skipWind))
   		fcCfg.setSkipWind(skipWind.isSelected());
   	else {
   		return;
   	}
   	EPD.getInstance().getRouteManager()
    .notifyListeners(RoutesUpdateEvent.ROUTE_CHANGED);
   }

   /***************************************************/
   /** Model update functions                        **/
   /***************************************************/
   
   /**
    * Adds a listener for route updates
    * @param listener the lister to add
    */
   public void addRouteChangeListener(RouteChangeListener listener) {
       listeners.add(listener);
   }

   /**
    * Removes a listener for route updates
    * @param listener the lister to remove
    */
   public void removeRouteChangeListener(RouteChangeListener listener) {
       listeners.remove(listener);
   }
   
   /**
    * Sub-classes can override this to be notified 
    * whenever the route has been updated in the
    * route properties dialog
    */
   protected void routeUpdated() {
       if (!readOnlyRoute) {
           for (RouteChangeListener listener : listeners) {
               listener.routeChanged();
           }
       }
   }

   /**
    * Notifies route listeners that the route has been updated
    * @param event the event to signal
    */
   private void notifyRouteListeners(RoutesUpdateEvent event) {
       EPD.getInstance().getRouteManager().notifyListeners(event);
   }
   
  
   
   
   private void updateLegPropulsionConfig(Path config) {
	   for (int i=0; i < route.getWaypoints().size(); i++) {
           RouteWaypoint wp = route.getWaypoints().get(i);
           if (wp.getOutLeg() != null && !locked[i]) {
               wp.getOutLeg().setPropulsionConfig(config.toString());;
           }
       }
	   routeTableModel.fireTableDataChanged();
	   
   }
   
   /**
    * Called when route values changes and the fields should be refreshed
    * here we should calculate the fuel consumption for each Wp
    */
   private void updateFields() {
//       if (!readOnlyRoute) {
//           route.calcValues(true);
//           route.calcAllWpEta();
//       }
      
       routeTableModel.fireTableDataChanged();        
   }
   
   /**
    * Checks the locked rows. 
    * If all rows except the last one are locked, also lock the last row
    * @return if all rows are locked
    */
   private boolean checkLockedRows() {
       int lockedNo = 0;
       for (int i = 0; i < locked.length; i++) {
           if (locked[i]) {
               lockedNo++;
           }
       }
       if (lockedNo == locked.length - 1 && !locked[locked.length - 1]) {
           locked[locked.length - 1] = true;
           lockedNo++;
           routeTableModel.fireTableRowsUpdated(locked.length - 1, locked.length - 1);
       }
       return lockedNo == locked.length;
   }
   
   /***************************************************/
   /** Utility functions                             **/
   /***************************************************/
   
   /**
    * Parses the text field as a double. Will skip any type suffix.
    * @param str the string to parse as a double
    * @return the resulting value
    */
   private static double parseDouble(String str) throws FormatException {
       str = str.replaceAll(",", ".");
       String[] parts = StringUtils.split(str, " ");
       return ParseUtils.parseDouble(parts[0]);
   }
   
   /**
    * Parses the text, which has the time format hh:mm:ss, into milliseconds.
    * @param str the string to parse
    * @return the time in milliseconds
    */
   private static long parseTime(String str)  throws Exception {
       String[] parts = str.split(":");
       return new Time(TimeType.HOURS, Long.valueOf(parts[0]))
         .add(new Time(TimeType.MINUTES, Long.valueOf(parts[1])))
         .add(new Time(TimeType.SECONDS, Long.valueOf(parts[2])))
         .in(TimeType.MILLISECONDS).longValue();
   }
   
   /***************************************************/
   /** Test method                                   **/
   /***************************************************/
   
   /**
    * Test method
    */
   public static final void main(String... args) throws Exception {

       //=====================
       // Create test data
       //=====================
       final Route route = new Route();
       route.setName("Test route");
       final LinkedList<RouteWaypoint> waypoints = new LinkedList<>();
       route.setWaypoints(waypoints);
       route.setStarttime(new Date());

       int len = 10;
       final boolean[] locked = new boolean[len];
       for (int x = 0; x < len; x++) {
           locked[x] = false;
           RouteWaypoint wp = new RouteWaypoint();
           waypoints.add(wp);

           // Set leg values
           if (x > 0) {
               RouteLeg leg = new RouteLeg();
               leg.setSpeed(12.00 + x);
               leg.setHeading(Heading.RL);
               leg.setXtdPort(185.0);
               leg.setXtdStarboard(185.0);

               wp.setInLeg(leg);
               waypoints.get(x-1).setOutLeg(leg);
               leg.setStartWp(waypoints.get(x-1));
               leg.setEndWp(wp);
           }

           wp.setName("WP_00" + x);
           wp.setPos(Position.create(56.02505 + Math.random() * 2.0, 12.37 + Math.random() * 2.0));    
       }
       for (int x = 1; x < len; x++) {
           waypoints.get(x).setTurnRad(0.5 + x * 0.2);
       }
       route.calcValues(true);
       
       // Launch the route properties dialog
       PntTime.init();
       RouteFuelConsumptionPropertiesDialogCommon dialog = new RouteFuelConsumptionPropertiesDialogCommon(null, null, route, false);
       dialog.setVisible(true);
   }   

   /***************************************************/
   /** Helper classes                                **/
   /***************************************************/

  
   /**
    * Interface to be implemented by clients wishing 
    * to be notified about updates to the route
    */
   public interface RouteChangeListener {
       
       /**
        * Signal that the route has changed
        */
       void routeChanged();

   }


}
