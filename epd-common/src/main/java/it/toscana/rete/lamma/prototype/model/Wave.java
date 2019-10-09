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
    private double heigth;
    private double direction;
    private double period;


	public Wave() {
        super();
        heigth=0;
        direction=0;
        period=0;
    }

    /**
     * @param heigth
     * @param direction
     * @param period
     */

    public Wave(double heigth, double direction, double period) {
        this.heigth = heigth;
        this.direction = direction;
        this.period = period;
    }

    /**
     * @return the heigth
     */
    public double getHeigth() {
        return heigth;
    }

    /**
     * @param heigth the heigth to set
     */
    public void setHeigth(double heigth) {
        this.heigth = heigth;
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

