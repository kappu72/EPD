package it.toscana.rete.lamma.prototype.model.tables;

public class FuelRateTable {
	protected Parameter spd;
	protected OneDimTable<OneDimDouble> fuelRateValues;
	protected Parameter rAdd;
	protected OneDimDouble rHullValues;
	
	
	public FuelRateTable(Parameter spd, Parameter fuelRate ) {
		super();
		this.spd = spd;
		this.rAdd= fuelRate;
		fuelRateValues = new OneDimTable<OneDimDouble>((int) spd.getMin() , (int) spd.getMax(), (int) spd.getDelta(), spd.getSize(), OneDimDouble.class);
	}
	public FuelRateTable(Parameter spd, Parameter fuelRate, Parameter rHull ) {
		super();
		this.spd = spd;
		this.rAdd= fuelRate;
		fuelRateValues = new OneDimTable<OneDimDouble>((int) spd.getMin() , (int) spd.getMax(), (int) spd.getDelta(), spd.getSize(), OneDimDouble.class);
		rHullValues = new OneDimDouble((int) spd.getMin() , (int) spd.getMax(), (int) spd.getDelta());
	}
	
	
	public void addfuelRateValue(OneDimTable<Double> fuelRateValue, int idx) {
		fuelRateValues.setValue((OneDimDouble) fuelRateValue, idx);
	}
	public void addRhullTValue( Double rHullValue, int idx){
		rHullValues.setValue(rHullValue, idx);
	}
	public double getFuelRate(float speed, float rAdd) {
		return fuelRateValues.getValue(speed).getWeightedValue(rAdd);
	}
	public double getRhull(float speed) {
		return rHullValues.getValue(speed);
	}
	public Parameter getSpd() {
		return spd;
	}
	public Parameter getrAdd() {
		return rAdd;
	}
	
}
