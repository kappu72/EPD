package it.toscana.rete.lamma.prototype.gui;

import com.bbn.openmap.gui.OMComponentPanel;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import dk.dma.epd.common.prototype.model.route.IRoutesUpdateListener;
import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RoutesUpdateEvent;
import dk.dma.epd.common.prototype.route.RouteManagerCommon;
import it.toscana.rete.lamma.prototype.gui.route.RoutesConsumtionTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoutesConsumptionPanelCommon extends OMComponentPanel implements PropertyChangeListener, ItemListener, IRoutesUpdateListener, TableModelListener {
    private JPanel panel1;
    public JTable rcTable;
    private JScrollPane scrollPane;
    private RouteSelector routeSelector;
    public RouteManagerCommon routeManager;
    private RoutesConsumtionTableModel routesTableModel;
    private Boolean disableSelect = false;
    private PopUpMenu tablePopUp;

    public RoutesConsumptionPanelCommon() {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(this.panel1, BorderLayout.CENTER);
        Component c = getRootPane();

    }

    private void createUIComponents() {
        tablePopUp = new PopUpMenu();
        routesTableModel = new RoutesConsumtionTableModel();
        routesTableModel.addTableModelListener(this);
        rcTable = new JTable(routesTableModel);

        // Set the minimum column widthsString()
        for (int x = 0; x < routesTableModel.COL_MIN_WIDTHS.length; x++) {
            rcTable.getColumnModel().getColumn(x).setPreferredWidth(routesTableModel.COL_MIN_WIDTHS[x]);
        }
        rcTable.getColumnModel().getColumn(1).setCellRenderer(new NumberTableCellRenderer());
        rcTable.getColumnModel().getColumn(2).setCellRenderer(new NumberTableCellRenderer());
       //rcTable.setComponentPopupMenu(new PopUpMenu());
        rcTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) maybeShowPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                int rowIdx = rcTable.rowAtPoint(e.getPoint());

                if (rowIdx != -1) {
                    tablePopUp.show(e.getComponent(),
                            e.getX(), e.getY(), rowIdx);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int r = rcTable.rowAtPoint(new Point(e.getPoint()));
                    openFuelConsumption(getSelectedRouteManagerIdx(r));
                }
            }
        });
        scrollPane = new JScrollPane(rcTable);
        routeSelector = new RouteSelector(new ArrayList<Route>());
        routeSelector.addItemListener(this);

    }
    private int getSelectedRouteManagerIdx(int rowIdx) {
        Route route = ((RoutesConsumtionTableModel) rcTable.getModel()).getRoutes().get(rowIdx);
        return routeManager.getRouteIndex(route);
    }
    /**
     * needs to be overwritten
     */
    public void openFuelConsumption(int routeIdx) {

    }

    /**
     * needs to be overwritten
     */
    public void openRouteProperties(int routeIdx) {

    }

    /**
     * needs to be overwritten
     */
    public void openRouteMetocProperties(int routeIdx) {

    }

    double calcTotal(Route route) {
        return route.getWaypoints().stream().filter(wp -> wp != null).map(wp -> wp.getOutLeg())
                .filter(p -> p != null).map(ol -> ol.getFuelConsumption()).filter(fc -> fc != null)
                .mapToDouble(fc -> fc.getFuel()).reduce(0, Double::sum);
    }

    private List<Route> getRoutes() {
        return routeManager.getRoutes().stream()
                .filter(r -> r.getRouteFCSettings() != null)
                .collect(Collectors.toList());

    }

    public int getRouteIdexByName(String name) {
        List<Route> routes = routeManager.getRoutes();
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getName() == name)
                return i;
        }

        return -1;
    }

    @Override
    public void routesChanged(RoutesUpdateEvent e) {
        disableSelect = true;
        if (e == RoutesUpdateEvent.ROUTE_ADDED) {
            List<Route> l = routeSelector.getRoutes();
            routeManager.getRoutes()
                    .stream()
                    .filter(route -> !l.contains(route))
                    .forEach(routeSelector::addRoute);
        }
        if (e == RoutesUpdateEvent.ROUTE_REMOVED) {
            List<Route> l = routeManager.getRoutes();
            routeSelector.getRoutes()
                    .stream()
                    .filter(route -> !l.contains(route))
                    .forEach(route -> {
                        ((RoutesConsumtionTableModel) rcTable.getModel()).removeRoute(route);
                        routeSelector.removeRoute(route);
                    });
        }
        disableSelect = false;

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (!disableSelect && e.getSource() == routeSelector && e.getStateChange() == ItemEvent.SELECTED) {
            Route item = (Route) e.getItem();
            ((RoutesConsumtionTableModel) rcTable.getModel()).addRoute(item);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if (obj instanceof RouteManagerCommon) {
            routeManager = (RouteManagerCommon) obj;
            routeManager.addListener(this);
            disableSelect = true;
            routeSelector.addRoutes(routeManager.getRoutes());
            disableSelect = false;
        }
    }

    @Override
    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);
        if (obj instanceof RouteManagerCommon) {
            routeManager = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == 4) {
            // Visibility has changed
            routeManager.notifyListeners(RoutesUpdateEvent.ROUTE_VISIBILITY_CHANGED);
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setAutoscrolls(false);
        panel1.setBackground(new Color(-4473925));
        panel1.setForeground(new Color(-14930501));
        panel1.setMinimumSize(new Dimension(200, 200));
        panel1.setPreferredSize(new Dimension(600, 600));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        scrollPane.setDoubleBuffered(true);
        scrollPane.setMinimumSize(new Dimension(280, 200));
        scrollPane.setPreferredSize(new Dimension(280, 200));
        scrollPane.setVerticalScrollBarPolicy(20);
        panel1.add(scrollPane, BorderLayout.CENTER);
        rcTable.setAutoCreateColumnsFromModel(true);
        rcTable.setAutoCreateRowSorter(true);
        rcTable.setAutoResizeMode(1);
        rcTable.setAutoscrolls(true);
        rcTable.setDoubleBuffered(false);
        rcTable.setFillsViewportHeight(true);
        rcTable.setOpaque(true);
        rcTable.setRequestFocusEnabled(false);
        scrollPane.setViewportView(rcTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:grow", "center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:d:grow"));
        panel1.add(panel2, BorderLayout.NORTH);
        CellConstraints cc = new CellConstraints();
        panel2.add(routeSelector, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.CENTER));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("Routes Selector");
        panel2.add(label1, cc.xy(1, 1));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.FILL));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    class PopUpMenu extends JPopupMenu implements ActionListener {
        JMenuItem removeItem;
        JMenuItem removeAllItems;
        JMenuItem showFuelConsumption;
        JMenuItem showProperties;
        JMenuItem showMetocProperties;

        int rowIdx;

        public PopUpMenu() {
            removeItem = new JMenuItem("Remove Route");
            showProperties = new JMenuItem("Open Properties");
            showMetocProperties = new JMenuItem("Open Metoc Properties");
            removeAllItems = new JMenuItem("Remove All Routes");
            showFuelConsumption = new JMenuItem("Open Fuel Consumption Properties");
            removeItem.addActionListener(this);
            showProperties.addActionListener(this);
            showMetocProperties.addActionListener(this);
            removeAllItems.addActionListener(this);
            showFuelConsumption.addActionListener(this);
            add(removeItem);
            add(removeAllItems);
            add(showProperties);
            add(showMetocProperties);
            add(showFuelConsumption);
        }

        public void show(Component invoker, int x, int y, int rowIdx) {
            super.show(invoker, x, y);
            this.rowIdx = rowIdx;

        }


        @Override
        public void actionPerformed(ActionEvent e) {
            RoutesConsumtionTableModel m = (RoutesConsumtionTableModel) ((JTable) getInvoker()).getModel();
            if (e.getSource() == removeItem) {
                if (rowIdx != -1) {
                    m.removeRoutes(new int[]{rowIdx});
                }
            } else if (e.getSource() == showMetocProperties) {
                if (rowIdx != -1) {
                    openRouteMetocProperties(getSelectedRouteManagerIdx(rowIdx));
                }
            } else if (e.getSource() == showProperties) {
                if (rowIdx != -1) {
                    openRouteProperties(getSelectedRouteManagerIdx(rowIdx));
                }
            } else if (e.getSource() == removeAllItems) {
                m.removeAll();
            } else if (e.getSource() == showFuelConsumption) {
                if (rowIdx != -1) {
                    openFuelConsumption(getSelectedRouteManagerIdx(rowIdx));
                }
            }
        }


    }

    public class NumberTableCellRenderer extends DefaultTableCellRenderer {

        public NumberTableCellRenderer() {
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Number) {
                value = NumberFormat.getNumberInstance().format(value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    }

}
