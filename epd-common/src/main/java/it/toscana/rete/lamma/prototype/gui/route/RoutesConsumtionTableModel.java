package it.toscana.rete.lamma.prototype.gui.route;

import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.text.Formatter;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class RoutesConsumtionTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RoutesConsumtionTableModel.class);
    public static final String[] COL_NAMES = {
            "Name",
            "Ton",
            "Length",
            "Time",
            "Visible"};

    public static final int[] COL_MIN_WIDTHS = { 80, 40, 60, 50, 40};
    protected List<Route> routes;
    public RoutesConsumtionTableModel(){
        this(new ArrayList<Route>());
    }
    public RoutesConsumtionTableModel (List<Route> routes) {
        this.routes = routes;
    }
    public void addRoutes(java.util.List<Route> routes){
        this.routes=routes;
        fireTableDataChanged();
    }
    @Override
    public int getRowCount() {
        return routes != null ? routes.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return COL_NAMES[columnIndex];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if(routes.size() > 0) {
            Route route = routes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return Formatter.formatString(route.getName());
                case 1:
                    return Formatter.formatDouble(calcTotal(route), 2);
                case 2:
                    return Formatter.formatDistNM(route.getRouteDtg());
                case 3:
                    return Formatter.formatTime(route.getRouteTtg());
                case 4:
                    return route.isVisible();
                default:
                    LOG.error("Unknown column " + columnIndex);
                    return new String("");
            }
        }
        return "";
    }

    double calcTotal(Route route){
        return route.getWaypoints().stream().filter(wp -> wp != null).map(wp -> wp.getOutLeg())
                .filter(p -> p != null).map(ol -> ol.getFuelConsumption()).filter(fc -> fc != null)
                .mapToDouble(fc -> fc.getFuel()).reduce(0, Double::sum);
    }
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 4;
    }
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Route route = routes.get(rowIndex);
        switch (columnIndex) {
            case 4:
                route.setVisible((Boolean)aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
                break;
            default:
                break;
        }
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

}
