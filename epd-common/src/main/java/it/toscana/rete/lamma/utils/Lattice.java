package it.toscana.rete.lamma.utils;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;

import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.time.CalendarDate;
import ucar.ma2.Range;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

/**
 * This class models a cube of data x -> lat y -> lon z -> time around a metoc
 * point It can do a trilinear interpolation to get metoc point value The
 * interpolation firsts squashes the data on x axis then on y and finally on z
 * 
 * 011+--------+111 + + /| /| /| / / | / | / | / + 010 +--------+110 + | + |
 * |001 | | Y Z | | | +-----|--+101 | / | + | / | / | / | / |/ |/ |/ |/ 000
 * +--------+ 100 +-----X +
 */
public class Lattice {

    private GridCoordSystem gcs;

    // X axis coordinate
    CoordinateAxis1D xAxis;
    double x_lon, x0_lon, x1_lon, dX, aX0, aX1;
    int x0_idx, x1_idx;

    // Y axis coordinate
    CoordinateAxis1D yAxis;
    double y_lat, y0_lat, y1_lat, dY, aY0, aY1;
    int y0_idx, y1_idx;

    // z axis (time) coordinate
    CoordinateAxis1DTime tAxis;
    CalendarDate z_time, z0_time, z1_time;
    int z0_idx, z1_idx;
    double dZ, aZ0, aZ1;

    public Lattice(GridCoordSystem gcs, double lat, double lon, Date time) {

        this.gcs = gcs;
        x_lon = lon;
        y_lat = lat;
        z_time = CalendarDate.of(time);
        this.initLattice();
    }

    public class LatticeValues {
        double c000, c100, c010, c110, c001, c101, c011, c111;

        public LatticeValues(double c000, double c100, double c010, double c110, double c001, double c101, double c011,
                double c111) {
            this.c000 = c000;
            this.c100 = c100;
            this.c010 = c010;
            this.c110 = c110;
            this.c001 = c001;
            this.c101 = c101;
            this.c011 = c011;
            this.c111 = c111;
        }

        public boolean isValid() {
            if (this.isValueValid(this.c000) && this.isValueValid(this.c100) && this.isValueValid(this.c010)
                    && this.isValueValid(this.c110) && this.isValueValid(this.c001) && this.isValueValid(this.c101)
                    && this.isValueValid(this.c011) && this.isValueValid(this.c111)) {
                return true;
            }
            return false;
        }

        private boolean isValueValid(double val) {
            return Double.isFinite(val);
        }

    }

    public double interpolateValue(GridDatatype param) throws IOException {
        return interpolateValue(getLatticeValue(param));
    }

    public double interpolateValue(LatticeValues v) throws IOException {
        // squash on x
        double x00 = (v.c000 * aX0) + (v.c100 * aX1);
        double x10 = (v.c010 * aX0) + (v.c110 * aX1);
        double x01 = (v.c001 * aX0) + (v.c101 * aX1);
        double x11 = (v.c011 * aX0) + (v.c111 * aX1);
        // squash on y
        double xy0 = (x00 * aY0) + (x10 * aY1);
        double xy1 = (x01 * aY0) + (x11 * aY1);

        // squash on z
        double xyz = (xy0 * aZ0) + (xy1 * aZ1);
        if (!Double.isFinite(xyz)) {
            throw new IOException("Value not valid");
        }

        return xyz;

    }

    // Extract the data from the grid (todo: consider if it has or not height
    // dimension)
    public LatticeValues getLatticeValue(GridDatatype param) throws IOException {
        // fixed dimension time, lat, long
        /**
         * TODO: To improve speed  with opendap iosp will have to find a way to request
         * all the variables in one request.
         */
        Array r;
        try {
            ArrayList<Range> lr = new ArrayList<Range>();
            lr.add(new Range(AxisType.Time.name(), z0_idx, z1_idx)); // Time range
            lr.add(new Range(AxisType.GeoY.name(), y0_idx, y1_idx)); // Latitude range
            lr.add(new Range(AxisType.GeoX.name(), x0_idx, x1_idx)); // Longitude range
            r = param.readSubset(lr);
        } catch (InvalidRangeException e) {
            System.out.println(e.getMessage());
            throw new IOException(e.getMessage());

        }

        // old code single call for each point
        // double c000 = param.readDataSlice(z0_idx, 0, y0_idx, x0_idx).getDouble(0);
        // double c100 = param.readDataSlice(z0_idx, 0, y0_idx, x1_idx).getDouble(0);
        // double c010 = param.readDataSlice(z0_idx, 0, y1_idx, x0_idx).getDouble(0);
        // double c110 = param.readDataSlice(z0_idx, 0, y1_idx, x1_idx).getDouble(0);
        // double c001 = param.readDataSlice(z1_idx, 0, y0_idx, x0_idx).getDouble(0);
        // double c101 = param.readDataSlice(z1_idx, 0, y0_idx, x1_idx).getDouble(0);
        // double c011 = param.readDataSlice(z1_idx, 0, y1_idx, x0_idx).getDouble(0);
        // double c111 = param.readDataSlice(z1_idx, 0, y1_idx, x1_idx).getDouble(0);

        // check if the values are valid
        LatticeValues values = new LatticeValues(r.getDouble(0), r.getDouble(1), r.getDouble(2), r.getDouble(3),
                r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7));
        if (!values.isValid()) {
            throw new IOException("Value not valid");
        }
        return values;

    }

    /**
     * inti all indexes and coefficients of lattice
     */
    private void initLattice() {

        xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
        x0_idx = this.findCoordElement(xAxis, x_lon);
        x1_idx = x0_idx + 1;
        x0_lon = xAxis.getCoordValue(x0_idx);
        x1_lon = xAxis.getCoordValue(x1_idx);

        // System.out.println(x0_lon);
        // System.out.println(x1_lon);

        dX = (x1_lon - x0_lon);

        aX0 = (x1_lon - x_lon) / dX;
        aX1 = (x_lon - x0_lon) / dX;

        yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();

        y0_idx = this.findCoordElement(yAxis, y_lat);
        y1_idx = y0_idx + 1;
        y0_lat = yAxis.getCoordValue(y0_idx);
        y1_lat = yAxis.getCoordValue(y1_idx);

        // System.out.println(y0_lat);
        // System.out.println(y1_lat);

        dY = (y1_lat - y0_lat);

        aY0 = (y1_lat - y_lat) / dY;
        aY1 = (y_lat - y0_lat) / dY;

        tAxis = gcs.getTimeAxis1D();
        z0_idx = tAxis.findTimeIndexFromCalendarDate(z_time);
        z1_idx = z0_idx + 1;

        z0_time = tAxis.getCalendarDate(z0_idx);
        z1_time = tAxis.getCalendarDate(z1_idx);

        // System.out.println(z0_time.toString());
        // System.out.println(z1_time.toString());

        dZ = (z1_time.getMillis() - z0_time.getMillis());

        aZ0 = (z1_time.getMillis() - z_time.getMillis()) / dZ;
        aZ1 = (z_time.getMillis() - z0_time.getMillis()) / dZ;
    }

    private int findCoordElement(CoordinateAxis1D axis, double coordValue) {
        int n = (int) axis.getSize();

        double distance = coordValue - axis.getStart();
        double exactNumSteps = distance / axis.getIncrement();
        int index = (int) exactNumSteps;
        if (index < 0)
            return -1;
        else if (index >= n)
            return -1;
        return index;
    }
}