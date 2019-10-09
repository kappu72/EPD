package it.toscana.rete.lamma.prototype.model.tables;
import java.util.Map;
import java.lang.Number;
/**
 * Longitudinal wind resistance coefficient C_x 
 * (cruise ship from Fujiwara et al. 2006)
 * @author kappu
 *
 */
public class WindresTable {
	protected OneDimTable<Double> windres;
	protected Parameter paramInfo;
	
	public WindresTable(Parameter paramInfo, Double[] values) {
		super();
		this.paramInfo = paramInfo;
		windres = new OneDimTable<Double>( paramInfo.getMin(), paramInfo.getMax(), paramInfo.getDelta(), values);
	}
	public WindresTable(Parameter paramInfo) {
		windres = new OneDimTable<Double>( paramInfo.getMin(), paramInfo.getMax(),  paramInfo.getDelta());
		this.paramInfo = paramInfo;
	}
	public OneDimTable<Double> getWindres() {
		return windres;
	}
	/**
	 * 
	 * @param angle Domain -180 180
	 * @return
	 */
	public double getCx(double angle) {
		return windres.getWeightedValue(Math.abs(angle));
	}
	public Parameter getWindresInfo() {
		return paramInfo;
	}

}
