package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

/**
 * Model Metoc parm in U V vector component
 * 
 * @author kappu
 *
 */
public class UVDimension implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double U;
	protected double V;
	
	public UVDimension(double u, double v) {
		super();
		U = u;
		V = v;
	}

	public double getU() {
		return U;
	}

	public void setU(double u) {
		U = u;
	}

	public double getV() {
		return V;
	}

	public void setV(double v) {
		V = v;
	}
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof UVDimension) {
			return U == ((UVDimension) obj).getU() && V == ((UVDimension) obj).getV();
		}
		return false;
	}
	
	
}
