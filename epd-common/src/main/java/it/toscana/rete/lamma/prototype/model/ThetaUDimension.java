package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

/**
 * Model a Metoc param using speed (U) direction (Theta) form
 * @author kappu
 *
 */
public class ThetaUDimension extends ParamDimension implements Serializable{
	private double Theta;
	
	public ThetaUDimension(double u, double theta) {
		super(u);
		Theta = theta;
	}
	public ThetaUDimension(ThetaUDimension v) {
		super(v.getU());
		Theta = v.getTheta();
	}
	public ThetaUDimension clone() {
		return new ThetaUDimension(U, Theta);
	}
	
	public double getTheta() {
		return Theta;
	}

	public void setTheta(double theta) {
		Theta = theta;
	}
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof ThetaUDimension) {
			return U == ((ThetaUDimension) obj).getU() && Theta == ((ThetaUDimension) obj).getTheta();
		}
		return false;
	}
	

}
