package it.toscana.rete.lamma.prototype.model;

import it.toscana.rete.lamma.utils.FuelConsumptionCalculator;

import java.io.Serializable;

/**
 * Wave parameters 
 * height m
 * period s
 * direction °
 * uvDirection used for average calculus with module = 1;
 * @author kappu
 *
 * 
 */



public class Wave implements Serializable {

    private static final long serialVersionUID = 1L;
    private double height;
    private double direction;
    private double period;
    private UVDimension uvDir;

	public Wave() {
        super();
        height=0;
        direction=0;
        period=0;
        uvDir = new UVDimension(0,0);
    }

    /**
     * @param height
     * @param direction
     * @param period
     */

    public Wave(double height, double direction, double period) {
        this(height, direction, period, FuelConsumptionCalculator.dirToVector(direction));
    }
    /**
     * @param height
     * @param direction
     * @param period
     * @parma uvDirection
     */

    public Wave(double height, double direction, double period, UVDimension uvDir) {
        this.height = height;
        this.direction = direction;
        this.period = period;
        this.uvDir = uvDir;
    }
    /**
     * Clone
     */
    public Wave clone() {
        return new Wave(height, direction, period, uvDir);
    }
    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return the direction
     */
    public double getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(double direction) {

        this.direction = direction;
        this.uvDir = FuelConsumptionCalculator.dirToVector(direction);
    }

    /**
     * @return the period
     */
    public double getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(double period) {
        this.period = period;
    }
    


    public UVDimension getUvDir() {
        return uvDir;
    }

    public void setUvDir(UVDimension uvDir) {
        this.uvDir = uvDir;
        this.direction = FuelConsumptionCalculator.uvDirToDir(uvDir);
    }

    public void sanitize() {
        period = period < 0.00001 ? 0.00001 : period;
    }

    public boolean isValid() {
        return Double.isFinite(period) && period > 0 && (direction >= 0 && direction <= 360) && Double.isFinite(height) && height > 0;
    }

}

