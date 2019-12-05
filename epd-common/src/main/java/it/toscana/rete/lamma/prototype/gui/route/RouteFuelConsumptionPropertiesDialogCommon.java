package it.toscana.rete.lamma.prototype.gui.route;

import static dk.dma.epd.common.graphics.GraphicsUtil.fixSize;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.VERTICAL;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.enav.model.geometry.Position;
import dk.dma.epd.common.FormatException;
import dk.dma.epd.common.Heading;
import dk.dma.epd.common.prototype.EPD;
import dk.dma.epd.common.prototype.gui.views.ChartPanelCommon;
import dk.dma.epd.common.prototype.model.route.IRoutesUpdateListener;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteLoadException;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.sensor.pnt.PntTime;
import dk.dma.epd.common.text.Formatter;
import dk.dma.epd.common.util.ParseUtils;
import dk.dma.epd.common.util.TypedValue.Time;
import dk.dma.epd.common.util.TypedValue.TimeType;
import dk.frv.enav.common.xml.metoc.MetocForecast;
import dk.frv.enav.common.xml.metoc.MetocForecastPoint;
import dk.frv.enav.common.xml.metoc.MetocForecastTriplet;
import it.toscana.rete.lamma.prototype.gui.shipsdata.ShipConfigurationsSelector;
import it.toscana.rete.lamma.prototype.gui.shipsdata.ShipPropulsionConfigurationsSelector;
import it.toscana.rete.lamma.prototype.gui.shipsdata.ShipsSelector;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import it.toscana.rete.lamma.prototype.model.MetocPointForecast;
import it.toscana.rete.lamma.prototype.model.RouteFuelConsumptionSettings;
import it.toscana.rete.lamma.prototype.model.ShipConfiguration;
import it.toscana.rete.lamma.prototype.model.ShipData;
import it.toscana.rete.lamma.prototype.model.ThetaUDimension;
import it.toscana.rete.lamma.prototype.model.UVDimension;
import it.toscana.rete.lamma.prototype.model.Wave;
import it.toscana.rete.lamma.prototype.model.tables.FuelRateTable;
import it.toscana.rete.lamma.prototype.model.tables.HullresTable;
import it.toscana.rete.lamma.prototype.model.tables.TableLoader;
import it.toscana.rete.lamma.prototype.model.tables.WaveresGenericTable;
import it.toscana.rete.lamma.prototype.model.tables.WindresTable;
import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;
import it.toscana.rete.lamma.utils.Utils;


/**
* Dialog used for viewing and editing route fuel consumption properties
* Freely inspired by RoutePropertiedDialogCommon
*/
public class RouteFuelConsumptionPropertiesDialogCommon extends JDialog implements IRoutesUpdateListener, ActionListener, ListSelectionListener, ItemListener {

   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(RouteFuelConsumptionPropertiesDialogCommon.class);
   
   private static final String[] COL_NAMES = {
           // Info rotta
           "Name", "Latutide", "Longtitude", "TTG",
           "ETA", "RNG", "BRG", "Head.", "SOG", 
           // Dati fuel consumption
           "P.C.","R.Cur", "Heading",  "R.Wind",
           "MWave", "Fuel", "Time","F.Rate",
           "Tot.R,","R.wa %", "R .wi %"
            };

   private static final int[] COL_MIN_WIDTHS = {
       60, 70, 70,50,
       70, 50, 50, 20, 50,
       50, 50, 80, 80,
       60, 60, 60,60,
       60,60,60
   };
   
   

   private Window parent;
   private ChartPanelCommon chartPanel;
   protected Route route = new Route();
   protected MetocForecast metoc;
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

   private JButton btnCalcConsumption = new JButton("Calc Consumption");
   
   

   
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
               // Not needed will be removed
                EPD.getInstance().getRouteManager().validateMetoc(RouteFuelConsumptionPropertiesDialogCommon.this.route);
           }});
           
        
        // check if it has metoc and add a listner to now when metoc changes
        // we doesn't condider if are ecpired
        metoc = this.route.getMetocForecast();
        EPD.getInstance().getRouteManager().addListener(this);
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
       prosContainer.add(routeProps, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, NORTHWEST, VERTICAL, insets10, 0, 0));       
       
       
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

    //    btnCalcConsumption.setEnabled(false);
       btnCalcConsumption.addActionListener(this);
       fcProps.add(fixSize(btnCalcConsumption, -1, 20), new GridBagConstraints(4, gridY, 1, 1, 0.0, 0.0, SOUTHEAST, NONE, insets2, 0, 0));


       fcProps.add(skipCurrent,new GridBagConstraints(3, gridY++, 1, 1, 0.0, 0.0, WEST, VERTICAL, insets1, 0, 0));
       
       
       
       routeProps.add(new JLabel(""), new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, insets2, 0, 0));
       
       
       
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
       routeDetailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
       routeDetailTable.getTableHeader().setReorderingAllowed(false);
       
       routeDetailTable.setFont(routeDetailTable.getFont().deriveFont(10.0f));


       // Set the minimum column widthsString()
       for (int x = 0; x < COL_MIN_WIDTHS.length; x++) {
           routeDetailTable.getColumnModel().getColumn(x).setMinWidth(COL_MIN_WIDTHS[x]);
       }
       
       // Configure lock column
    //    TableColumn col = routeDetailTable.getColumnModel().getColumn(0);
    //    col.setWidth(COL_MIN_WIDTHS[0]);
    //    col.setMaxWidth(COL_MIN_WIDTHS[0]);
    //    col.setMinWidth(COL_MIN_WIDTHS[0]);
       
    //    col.setCellRenderer(new LockTableCell.CustomBooleanCellRenderer());
    //    col.setCellEditor(new LockTableCell.CustomBooleanCellEditor());
       
       

       // Configure heading column
       JComboBox<Heading> headingCombo = new JComboBox<>(Heading.values());
       headingCombo.setFont(headingCombo.getFont().deriveFont(10.0f));
       routeDetailTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(headingCombo));
       
       JScrollPane scrollPane = new JScrollPane(routeDetailTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       
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
               RouteLeg ol = isLastRow? null : wp.getOutLeg();
               FuelConsumption fc = isLastRow? null : ol.getFuelConsumption();
               switch (columnIndex) {
                /* Info rotta
           "Name", "Latutide", "Longtitude", "TTG",
           "ETA", "RNG", "BRG", "Head.", "SOG", 
           // Dati fuel consumption
           "PSG","R.Cur", "Heading",  "R.Wind",
           "MWave", "F.C.", "D.T","F.C.R",
           "Tot.R","Rwa%", "Rwi%"*/
	               case  0: return wp.getName(); // Name
	               case  1: return wp.getPos().getLatitudeAsString(); // Latitude
	               case  2: return wp.getPos().getLongitudeAsString(); // Longitude
	               case  3: return Formatter.formatTime(route.getWpTtg(rowIndex)); // Time to target, quanto ci metto a raggiungere il punto
	               case  4: return Formatter.formatShortDateTimeNoTz(route.getWpEta(rowIndex)); // Expected time of arrival momento che sono sul punto
                   case  5: return Formatter.formatDistNM(route.getWpRng(rowIndex)); // Lunghezza del tratto
	               case  6: return Formatter.formatDegrees(route.getWpBrg(wp), 2); // Heading nel tratto
	               case  7: return isLastRow? "N/A" : wp.getHeading(); // Tipo di rotta
	               case  8: return Formatter.formatSpeed(isLastRow ? null : ol.getSpeed()); // Speed over ground kn
                   case 9: return isLastRow? "N/A" : Formatter.formatPropulsion(ol.getPropulsionConfig()); //  propulsion configuration
                   case 10: return (fc == null)? "N/A" : Formatter.formatCurrent(fc.getCurrent_rel()); // Relative current only speed 
                   case 11: return (fc == null)? "N/A" : Formatter.formatDegrees(fc.getHeading(), 2); // Heading considering current
                   case 12: return (fc == null)? "N/A" : Formatter.formatWind(fc.getWind_rel(), fc.getHeading()); // Wind relative
                   case 13: return (fc == null)? "N/A" : Formatter.formatWave(fc); // Mean Wave component;
                   case 14: return (fc == null)? "N/A" : Formatter.formatDouble(fc.getFuel(), 3) + " t"; // Total fuel consumption for the leg
                   case 15: return (fc == null)? "N/A" : Formatter.formatTime(ol.calcTtg()) ; // durata del tratto è la durata del tratto successivo a questo 
                   case 16: return (fc == null)? "N/A" : Formatter.formatDouble(fc.getFuelRate(), 2) + " t/h"; // Fuel rate for the leg in t/h
                   case 17: return (fc == null)? "N/A" : Formatter.formatDouble(fc.getTotalResistance(), 2) + "kN";// Total resistance, (Added wave wind) + carena
                   case 18: return (fc == null)? "N/A" : Formatter.formatDouble((fc.getWave_resistance() / fc.getTotalResistance()) * 100, 2) + " %"; // Wave added resustance in percentuale del totale
                   case 19: return (fc == null)? "N/A" : Formatter.formatDouble((fc.getWind_resistance() / fc.getTotalResistance()) * 100, 2) + " %"; // Wind added resustance in percentuale del totale
                   
                   default: return null;
               }
           }

           @Override
           public void setValueAt(Object value, int rowIndex, int columnIndex) {
               try {
                   RouteWaypoint wp = route.getWaypoints().get(rowIndex);
                   switch (columnIndex) {
                   case  90: 
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
               return !readOnlyRoute && columnIndex == 90;
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
       
       btnCalcConsumption.setEnabled(this.route.getMetocForecast() != null);
       
    //    boolean allRowsLocked = checkLockedRows();

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
          return;
          
      }else if (evt.getSource() == btnCalcConsumption) {
        /**
         * TODO Lancia il calcolo vengono recuperati tutti i parametri necessari ed i metoc
         * e si lancia il calcolo dei consumi
         */
            caluclateConsumption();        

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
    * @param listener the listner to add
    */
   public void addRouteChangeListener(RouteChangeListener listener) {
       listeners.add(listener);
   }

   /**
    * Removes a listener for route updates
    * @param listener the listner to remove
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
       String path = config != null ? config.toString() : null;
       route.getWaypoints().forEach(wp -> {
            if (wp.getOutLeg() != null) {
                wp.getOutLeg().setPropulsionConfig(path);
            }
       });
       routeTableModel.fireTableDataChanged();
       btnCalcConsumption.setEnabled(true);
	   
   }


   private void cleanFuelConsumption(Boolean forceTable) {
    
    route.getWaypoints().forEach(wp -> {
        if (wp.getOutLeg() != null) {
             wp.getOutLeg().setFuelConsumption(null);
             wp.getOutLeg().setInnerPointsConsumption(new ArrayList<FuelConsumption>());
        }
    });
    if(forceTable){
        routeTableModel.fireTableDataChanged();
    }
    
}

   private void caluclateConsumption() {

    /** 
     * Elementi necessari al calcolo :
     * WayPoint con informazioni di rotta (outleg)
     * Metoc
     * Tablle nave minimali (windres waveres propulsionconfig)
     */

    // first of all clean old values
    cleanFuelConsumption(false);
     // Sulla base delle options selezionate carichiamo i file 
     ShipConfiguration config = (ShipConfiguration) configurationsSelector.getSelectedItem();
     // qui andrebbe fatta prima distinzione nel caso che fossero disponibili dati meteo con swell e wind sea!!
     try {
        WindresTable cxRes = TableLoader.laodWindres(config.getWindres()); 
        WaveresGenericTable cawRes = TableLoader.laodWaveGenericTable(config.getWaveres());
        
        // If hullresistance table is configured load it
        HullresTable hullResTable = null; 
        if(config.getHullres() != null) {
            hullResTable = TableLoader.laodHullres(config.getHullres());
        }
        
        HashMap<String, FuelRateTable> propulsionTables = ( HashMap<String, FuelRateTable> ) config.getPropulsions().stream().map( p -> {
                        try {
                            return TableLoader.laodFuelRateTable(p);
                        } catch (RouteLoadException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toMap(FuelRateTable::getId, t -> t));
       
        // Squash i metoc nei waypoints    
        Iterator<MetocForecastPoint> iter = metoc.getForecasts().iterator();
        

        List<Date> etas = route.getEtas();
    
        LinkedList<RouteWaypoint> wps = route.getWaypoints();
        double totalConsumption = 0;
        
        if(!iter.hasNext()) {
            JOptionPane.showMessageDialog(null,
                        "Impossibile calcolare il consumo, nessun metoc presente",
                        "Fuel Consumption Calculator error",
                        JOptionPane.WARNING_MESSAGE);
            return;
        }
        // I don't like this code is to difficult to read!!
        // I have to group the metoc point by leg. It uses time as predicate
        MetocPointForecast me = (MetocPointForecast) iter.next();
        Date meDate = etas.get(0);

        long timeStep = 60000L * route.getRouteMetocSettings().getInterval(); // comes in minutes to ms


         // vanno messi dei check per i metoc e le tabelle
        
        for (int i=0; i < route.getWaypoints().size() - 1 ; i++) {
            RouteWaypoint wp = wps.get(i);
            RouteLeg outleg = wp.getOutLeg(); 
            Date nextEta = etas.get(i + 1);
            Date eta = etas.get(i);
            
            // Genera fc e metoc per il leg con media pesata dei singoli pezzi
            FuelConsumption legFc = new FuelConsumption();
            MetocPointForecast legMetoc = this.getEmptyMetoc();
            while (me.getTime().getTime() >= eta.getTime() && me.getTime().getTime() < nextEta.getTime()) {
                // Sono metoc validi per questo leg
                long distance = i == 0 ? timeStep :  me.getTime().getTime() - meDate.getTime();
                meDate = me.getTime();
                // Calcola il peso del singolo tratto
                double weight = ((double) distance) / timeStep;
                
                if(outleg != null) {
                    
                    // Sommatoria metoc
                    this.weightedSumMetocPoints(legMetoc, me, weight);
                    FuelRateTable fuelRateTable = propulsionTables.get(outleg.getPropulsionConfig());    
		            ThetaUDimension SOG = new ThetaUDimension(outleg.getSpeed(), outleg.calcBrg());    
                    try {
                        Wave mwave = me.getMeanWave();
                        FuelConsumption c = FuelConsumptionCalculator.CalculateAllKinematical(SOG, me.getCurrent(), me.getWind(), mwave.getDirection(), true);
                        FuelConsumption r = FuelConsumptionCalculator.CalculateResistance(c, mwave.getHeight(), mwave.getPeriod(), cxRes, cawRes, 850); // occhio a 850 è fisso
                        if(hullResTable != null) {
                            r.setHull_resistance(hullResTable.getRes(r.getCurrent_rel().getU()));
                        }
                        
                        r.setWeight(weight);
                        double fuelRate = fuelRateTable.getFuelRate((float) r.getCurrent_rel().getU(), (float) r.getTotalAddedResistance());                        
                        r.setFuelRate(fuelRate);
                        r.setMetoc(me);
                        
                        if(fuelRate != -1) {
                            double fuel = fuelRate * (distance  / 3600000.0);
                            r.setFuel(fuel);
                            totalConsumption += r.getFuel();
                        }else {
                            JOptionPane.showMessageDialog(null,
                        "Try to modify speed for this leg " + wp.getName(),
                        "Spedd error",
                        JOptionPane.WARNING_MESSAGE);
                        }
                        outleg.getInnerPointsConsumption().add(r);
                        // Setting vaules on fuel consumption of the outer leg
                        
                        this.weightdSumConsumption(legFc, r, weight);
                        
                    }catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                         e.getMessage() ,
                        "Error: " + wp.getName(),
                        JOptionPane.WARNING_MESSAGE);
                    }
                
                }
                // TODO METTICI TUTTI I CALCOLI CHE VUOI
                if(!iter.hasNext()) {
                    break;
                }
                me = (MetocPointForecast) iter.next();
            }
            // end outelg calculation 
            int size = outleg.getInnerPointsConsumption().size();
            if( size > 0) {  
                this.averageLegFuelConsumptionValue(legFc, legMetoc, size);
                outleg.setFuelConsumption(legFc);
            }



        };
       
        
        totalFuel.setText(Formatter.formatDouble(totalConsumption, 2));
        routeTableModel.fireTableDataChanged();
     } catch ( Exception e ) {
        JOptionPane.showMessageDialog(null,
            e.getMessage() ,
            "Load table error",
            JOptionPane.WARNING_MESSAGE);
     }
    
    }
    /**
     * Wighted sum of inner metoc fuel consumpiton values
     * 
     */

    private void weightdSumConsumption(FuelConsumption acc, FuelConsumption val, double weight) {
        
        acc.setFuelRate(acc.getFuelRate() + (val.getFuelRate() / weight));
        acc.setFuel(acc.getFuel() + val.getFuel());
        acc.setWave_resistance(acc.getWave_resistance() + (val.getWave_resistance() / weight));
        acc.setWind_resistance(acc.getWind_resistance() + (val.getWind_resistance() / weight));
        acc.setHull_resistance(acc.getHull_resistance() + (val.getHull_resistance() / weight));
        acc.setWave_polar(acc.getWave_polar() + (val.getWave_polar() / weight));
        acc.setWind_polar(acc.getWind_polar() + (val.getWind_polar() / weight));
        acc.setHeading(acc.getHeading() + (val.getHeading() / weight));
        
        UVDimension current = acc.getCurrent_rel_uv();
        UVDimension newCurrent = val.getCurrent_rel_uv();
        current.setV(current.getV() + newCurrent.getV() / weight);
        current.setU(current.getU() + newCurrent.getU() / weight);
        UVDimension wind = acc.getWind_rel_uv();
        UVDimension newWind = val.getWind_rel_uv();
        wind.setV(wind.getV() + newWind.getV() / weight);
        wind.setU(wind.getU() + newWind.getU() / weight);
    }
    /**
     * Weighted sum of inner metoc MetocPointForecast values
     */
    private void weightedSumMetocPoints(MetocPointForecast acc , MetocPointForecast val, double weight) {
        
        UVDimension wind = acc.getWind();
        wind.setU(wind.getU() + val.getWind().getU() / weight);
        wind.setV(wind.getV() + val.getWind().getV() / weight);
        
        UVDimension current = acc.getCurrent();
        current.setU(current.getU() + val.getCurrent().getU() / weight);
        current.setV(current.getV() + val.getCurrent().getV() / weight);

        Wave wave = acc.getMeanWave();
        wave.setHeight(wave.getHeight() + val.getMeanWave().getHeight() / weight);
        wave.setPeriod(wave.getPeriod() + val.getMeanWave().getPeriod() / weight);
        wave.setDirection(wave.getDirection() + val.getMeanWave().getDirection() / weight);
    }
    /**
     * Calculate the average value of FuelConsumption and MetopointForecas summetions
     */
    private void averageLegFuelConsumptionValue (FuelConsumption legFc, MetocPointForecast legMetoc, int size ) {
         // Media pesata metoc per visualizzazione
         UVDimension wind = legMetoc.getWind();
         wind.setU(wind.getU() / size);
         wind.setV(wind.getV() / size);
         legMetoc.setWind(wind);

         UVDimension current = legMetoc.getCurrent();
         current.setU(current.getU() / size);
         current.setV(current.getV() / size);
         legMetoc.setCurrent(current);

         Wave wave = legMetoc.getMeanWave();
         wave.setHeight(wave.getHeight() / size);
         wave.setPeriod(wave.getPeriod() / size);
         wave.setDirection(wave.getDirection() / size);
         legMetoc.setMeanWave(wave);

         legFc.setMetoc(legMetoc);
         //Media pesata fuel consumption values
         
        legFc.setFuelRate(legFc.getFuelRate() / size);
        legFc.setFuel(legFc.getFuel());
        legFc.setWave_resistance(legFc.getWave_resistance() / size);
        legFc.setWind_resistance(legFc.getWind_resistance() / size);
        legFc.setHull_resistance(legFc.getHull_resistance() / size);
        legFc.setWave_polar(legFc.getWave_polar() / size);
        legFc.setWind_polar(legFc.getWind_polar() / size);
        legFc.setHeading(legFc.getHeading() / size);
        legFc.getCurrent_rel_uv().setV(legFc.getCurrent_rel_uv().getV() / size);
        legFc.getCurrent_rel_uv().setU(legFc.getCurrent_rel_uv().getU() / size);
        legFc.getWind_rel_uv().setV(legFc.getWind_rel_uv().getV() / size);
        legFc.getWind_rel_uv().setU(legFc.getWind_rel_uv().getU() / size);
        legFc.currentThetaFromUV();
        legFc.windThetaFromUV();

    }
    private MetocPointForecast getEmptyMetoc() {
        MetocPointForecast empty = new MetocPointForecast();
        empty.setCurrentDirection(new MetocForecastTriplet(0.));
        empty.setCurrentSpeed(new MetocForecastTriplet(0.));
        empty.setWindDirection(new MetocForecastTriplet(0.));
        empty.setWindSpeed(new MetocForecastTriplet(0.));
        empty.setMeanWaveDirection(new MetocForecastTriplet(0.));
        empty.setMeanWaveHeight(new MetocForecastTriplet(0.));
        empty.setMeanWavePeriod(new MetocForecastTriplet(0.));
        return empty;

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
   // Recive a message if metoc changes
    @Override
    public void routesChanged(RoutesUpdateEvent e) {
        if(e.is(RoutesUpdateEvent.ROUTE_METOC_CHANGED)){
            metoc = route.getMetocForecast();
        }
    }


}
