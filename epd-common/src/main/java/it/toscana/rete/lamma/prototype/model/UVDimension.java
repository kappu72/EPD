package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

/**
 * Model Metoc parm in U V vector component
 * 
 * @author kappu
 *
 */
public class UVDimension extends ParamDimension implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double V;
	
	public UVDimension(double u, double v) {
		super(u);
		V = v;
	}
	/**
	 * Create a copy 
	 * @return
	 */
	public UVDimension clone() {
		return new UVDimension(U, V);
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
