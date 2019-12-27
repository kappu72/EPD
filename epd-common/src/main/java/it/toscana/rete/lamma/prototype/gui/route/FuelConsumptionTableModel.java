package it.toscana.rete.lamma.prototype.gui.route;

import java.nio.file.Path;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import dk.dma.epd.common.prototype.model.route.Route;
import dk.dma.epd.common.prototype.model.route.RouteLeg;
import dk.dma.epd.common.prototype.model.route.RouteWaypoint;
import dk.dma.epd.common.text.Formatter;
import dk.dma.epd.common.util.ParseUtils;
import org.apache.commons.lang.StringUtils;
import dk.dma.epd.common.util.TypedValue.Time;
import dk.dma.epd.common.util.TypedValue.TimeType;
import dk.dma.epd.common.FormatException;
import it.toscana.rete.lamma.prototype.model.FuelConsumption;

public class FuelConsumptionTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;
        public static final String[] COL_NAMES = {
            // Info rotta
            "Name", "Latutide", "Longtitude", "TTG", "ETA", "RNG", "BRG", "Head.", "SOG",
            // Dati fuel consumption
            "P.C.", "R.Cur", "Heading", "R.Wind", "MWave", "Fuel", "Time", "F.Rate", "Tot.R,", "R.wa %", "R .wi %" };

        public static final int[] COL_MIN_WIDTHS = { 60, 70, 70, 50, 70, 50, 50, 20, 50, 50, 50, 80, 80, 60, 60, 60, 60,
            60, 60, 60 };
        protected Route route;

        public FuelConsumptionTableModel (final Route route) {
            super();
            this.route = route;
        }

    @Override
    public int getRowCount() {
        return route == null ? 0  : route.getWaypoints().size();
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
        final RouteWaypoint wp = route.getWaypoints().get(rowIndex);
        final Boolean isLastRow = wp.getOutLeg() == null;
        final RouteLeg ol = isLastRow ? null : wp.getOutLeg();
        final FuelConsumption fc = isLastRow ? null : ol.getFuelConsumption();
            switch (columnIndex) {
            /*
             * Info rotta "Name", "Latutide", "Longtitude", "TTG", "ETA", "RNG", "BRG",
             * "Head.", "SOG", // Dati fuel consumption "PSG","R.Cur", "Heading", "R.Wind",
             * "MWave", "F.C.", "D.T","F.C.R", "Tot.R","Rwa%", "Rwi%"
             */
            case 0:
                return wp.getName(); // Name
            case 1:
                return wp.getPos().getLatitudeAsString(); // Latitude
            case 2:
                return wp.getPos().getLongitudeAsString(); // Longitude
            case 3:
                return Formatter.formatTime(route.getWpTtg(rowIndex)); // Time to target, quanto ci metto a
                                                                       // raggiungere il punto
            case 4:
                return Formatter.formatShortDateTimeNoTz(route.getWpEta(rowIndex)); // Expected time of arrival
                                                                                    // momento che sono sul punto
            case 5:
                return Formatter.formatDistNM(route.getWpRng(rowIndex)); // Lunghezza del tratto
            case 6:
                return Formatter.formatDegrees(route.getWpBrg(wp), 2); // Heading nel tratto
            case 7:
                return isLastRow ? "N/A" : wp.getHeading(); // Tipo di rotta
            case 8:
                return Formatter.formatSpeed(isLastRow ? null : ol.getSpeed()); // Speed over ground kn
            case 9:
                return isLastRow ? "N/A" : Formatter.formatPropulsion(ol.getPropulsionConfig()); // propulsion
                                                                                                 // configuration
            case 10:
                return (fc == null) ? "N/A" : Formatter.formatCurrent(fc.getCurrent_rel()); // Relative current only
                                                                                            // speed
            case 11:
                return (fc == null) ? "N/A" : Formatter.formatDegrees(fc.getHeading(), 2); // Heading considering
                                                                                           // current
            case 12:
                return (fc == null) ? "N/A" : Formatter.formatWind(fc.getWind_rel(), fc.getHeading()); // Wind
                                                                                                       // relative
            case 13:
                return (fc == null) ? "N/A" : Formatter.formatWave(fc); // Mean Wave component;
            case 14:
                return (fc == null) ? "N/A" : Formatter.formatDouble(fc.getFuel(), 3) + " t"; // Total fuel
                                                                                              // consumption for the
                                                                                              // leg
            case 15:
                return (fc == null) ? "N/A" : Formatter.formatTime(ol.calcTtg()); // durata del tratto è la durata
                                                                                  // del tratto successivo a questo
            case 16:
                return (fc == null) ? "N/A" : Formatter.formatDouble(fc.getFuelRate(), 2) + " t/h"; // Fuel rate for
                                                                                                    // the leg in
                                                                                                    // t/h
            case 17:
                return (fc == null) ? "N/A" : Formatter.formatDouble(fc.getTotalResistance(), 2) + "kN";// Total
                                                                                                        // resistance,
                                                                                                        // (Added
                                                                                                        // wave
                                                                                                        // wind) +
                                                                                                        // carena
            case 18:
                return (fc == null) ? "N/A"
                        : Formatter.formatDouble((fc.getWave_resistance() / fc.getTotalResistance()) * 100, 2)
                                + " %"; // Wave added resustance in percentuale del totale
            case 19:
                return (fc == null) ? "N/A"
                        : Formatter.formatDouble((fc.getWind_resistance() / fc.getTotalResistance()) * 100, 2)
                                + " %"; // Wind added resustance in percentuale del totale

            default:
                return null;
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            try {
                RouteWaypoint wp = route.getWaypoints().get(rowIndex);
                RouteLeg ol = wp.getOutLeg();
                
                switch (columnIndex) {
                case 9:
                    String path = value != null ? ((Path) value).toString() : null;
                    if(path != ol.getPropulsionConfig()) {
                        ol.setPropulsionConfig(path);
                        fireTableCellUpdated(rowIndex, columnIndex);
                    }
                    break;
                default:
                }
                
            } catch (Exception ex) {
                
                JOptionPane.showMessageDialog(null,
                        "Input error: " + ex.getMessage(), "Input error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 9 && rowIndex < route.getWaypoints().size() - 1;
        }
    /***************************************************/
    /** Utility functions **/
    /***************************************************/

    /**
     * Parses the text field as a double. Will skip any type suffix.
     * 
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
     * 
     * @param str the string to parse
     * @return the time in milliseconds
     */
    private static long parseTime(String str) throws Exception {
        String[] parts = str.split(":");
        return new Time(TimeType.HOURS, Long.valueOf(parts[0])).add(new Time(TimeType.MINUTES, Long.valueOf(parts[1])))
                .add(new Time(TimeType.SECONDS, Long.valueOf(parts[2]))).in(TimeType.MILLISECONDS).longValue();
    }
}
