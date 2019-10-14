package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

/**
 * Wave parameters 
 * heigth m 
 * period s
 * direction °
 * 
 * @author kappu
 *
 * 
 */



public class Wave implements Serializable {

    private static final long serialVersionUID = 1L;
    private double height;
    private double direction;
    private double period;


	public Wave() {
        super();
        height=0;
        direction=0;
        period=0;
    }

    /**
     * @param height
     * @param direction
     * @param period
     */

    public Wave(double height, double direction, double period) {
        this.height = height;
        this.direction = direction;
        this.period = period;
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
    




    
}

