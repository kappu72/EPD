package it.toscana.rete.lamma.prototype.model;

import java.io.Serializable;

public abstract class ParamDimension implements Serializable{
	double U;
	public ParamDimension() {
		
	}
	public ParamDimension(double u) {
		U=u;
	}
	public double getU() {
		return U;
	}
	public void setU(double u) {
		U = u;
	}
}
