package it.toscana.rete.lamma.prototype.model.tables;

/**
 * Hull resistance  f(speed) = kN
 * 
 * @author kappu
 *
 */
public class HullresTable {
	protected OneDimTable<Double> hullres;
	protected Parameter paramInfo;
	
	public HullresTable(Parameter paramInfo, Double[] values) {
		super();
		this.paramInfo = paramInfo;
		hullres = new OneDimTable<Double>( paramInfo.getMin(), paramInfo.getMax(), paramInfo.getDelta(), values);
	}
	public HullresTable(Parameter paramInfo) {
		hullres = new OneDimTable<Double>( paramInfo.getMin(), paramInfo.getMax(),  paramInfo.getDelta());
		this.paramInfo = paramInfo;
	}
	public OneDimTable<Double> getHullres() {
		return hullres;
	}
	/**
	 * 
	 * @param speed
	 * @return
	 */
	public double getRes(double speed) {
		return hullres.getWeightedValue(speed);
	}
	public Parameter getHullresInfo() {
		return paramInfo;
	}

}
